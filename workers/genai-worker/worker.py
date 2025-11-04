import pika
import time
import os
import sys
import json
import requests
from google import genai

# --- Config ---
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'user')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASS', 'password')
API_URL = os.getenv('API_URL', 'http://paperless-rest-api:8080')
GOOGLE_API_KEY = os.getenv('GOOGLE_API_KEY')

if not GOOGLE_API_KEY:
    print("Error: GOOGLE_API_KEY environment variable not set.")
    sys.exit(1)

client = genai.Client()

# --- RabbitMQ ---
GENAI_QUEUE = 'genai-queue'
RESULT_EXCHANGE = 'document-exchange'
RESULT_ROUTING_KEY = 'document.genai.result'

def get_document_content(doc_id):
    try:
        response = requests.get(f"{API_URL}/api/documents/{doc_id}")
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error fetching metadata for doc ID {doc_id}: {e}")
        return None

def generate_summary(text_content):
    prompt = f"Please provide a concise, one-paragraph summary of the following document content:\n\n{text_content}"
    try:
        # Use the new client.models.generate_content() syntax
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt
        )
        return response.text
    except Exception as e:
        print(f"Error generating summary: {e}")
        raise

def publish_result(channel, doc_id, summary, status, error_details=""):
    message = {
        "documentId": doc_id,
        "summary": summary,
        "status": status,
        "errorDetails": str(error_details)
    }
    channel.basic_publish(
        exchange=RESULT_EXCHANGE,
        routing_key=RESULT_ROUTING_KEY,
        body=json.dumps(message),
        properties=pika.BasicProperties(content_type='application/json')
    )
    print(f"Published {status} summary for document ID {doc_id}")

def callback(ch, method, properties, body):
    try:
        # 1. Parse incoming message
        doc_id = int(json.loads(body.decode()))
        print(f"GenAI Worker: [x] Received document ID for summary: {doc_id}")
    except Exception as e:
        print(f"Error decoding message body: {e}")
        ch.basic_ack(delivery_tag=method.delivery_tag) # Ack and discard bad message
        return

    try:
        # 2. Fetch metadata to get the full OCR content
        metadata = get_document_content(doc_id)
        if not metadata or 'content' not in metadata or not metadata['content']:
            raise ValueError(f"Could not fetch valid content for doc ID {doc_id}.")
        
        # 3. Generate summary
        summary = generate_summary(metadata['content'])

        # 4. Publish success result
        publish_result(ch, doc_id, summary, "COMPLETED")

    except Exception as e:
        # 5. Publish failure result
        print(f"An error occurred summarizing document {doc_id}: {e}")
        publish_result(ch, doc_id, "", "FAILED", str(e))
    
    finally:
        # 6. Acknowledge the original message
        ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    connection = None
    
    while not connection:
        try:
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
            print('GenAI Worker: Successfully connected to RabbitMQ.')
        except pika.exceptions.AMQPConnectionError:
            print('GenAI Worker: Failed to connect to RabbitMQ. Retrying in 5 seconds...')
            time.sleep(5)

    channel = connection.channel()
    channel.queue_declare(queue=GENAI_QUEUE, durable=False)
    channel.basic_consume(queue=GENAI_QUEUE, on_message_callback=callback)
    
    print('GenAI Worker: [*] Waiting for messages.')
    channel.start_consuming()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print('Interrupted')
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)