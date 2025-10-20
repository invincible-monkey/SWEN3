import pika
import time
import os
import sys
import json
import requests
from minio import Minio
from pdf2image import convert_from_bytes
import pytesseract

# --- Config ---
RABBITMQ_HOST = os.getenv('RABBITMQ_HOST', 'localhost')
RABBITMQ_USER = os.getenv('RABBITMQ_USER', 'user')
RABBITMQ_PASS = os.getenv('RABBITMQ_PASS', 'password')
MINIO_URL = os.getenv('MINIO_URL', 'localhost:9000')
MINIO_ACCESS_KEY = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
MINIO_SECRET_KEY = os.getenv('MINIO_SECRET_KEY', 'minioadmin')

API_URL = os.getenv('API_URL', 'http://paperless-rest-api:8080')
MINIO_BUCKET = 'documents'

# --- RabbitMQ ---
OCR_QUEUE = 'ocr-queue'
RESULT_EXCHANGE = 'document-exchange'
RESULT_ROUTING_KEY = 'document.ocr.result'

minio_client = Minio(
    MINIO_URL,
    access_key=MINIO_ACCESS_KEY,
    secret_key=MINIO_SECRET_KEY,
    secure=False
)

def get_document_metadata(doc_id):
    """Fetches document metadata from the REST API."""
    try:
        url = f"{API_URL}/api/documents/{doc_id}"
        print(f"Fetching metadata from: {url}")
        response = requests.get(url)
        response.raise_for_status()  # raise an HTTPError for bad responses (4xx or 5xx)
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"Error fetching metadata for doc ID {doc_id}: {e}")
        return None

def download_file_from_minio(object_name):
    """Downloads file from MinIO and returns content as bytes."""
    try:
        response = minio_client.get_object(MINIO_BUCKET, object_name)
        return response.read()
    except Exception as e:
        print(f"Error downloading file {object_name} from MinIO: {e}")
        return None

def perform_ocr(pdf_bytes):
    """Converts PDF bytes to images and extracts text using Tesseract."""
    try:
        images = convert_from_bytes(pdf_bytes)
        if not images:
            raise ValueError("PDF could not be converted to images. Maybe empty or corrupted?")
            
        full_text = ""
        for image in images:
            # add error handling for Tesseract itself
            try:
                full_text += pytesseract.image_to_string(image) + "\n"
            except pytesseract.TesseractError as ocr_error:
                print(f"Tesseract failed on an image: {ocr_error}")
                full_text += "[OCR failed for this page]\n"
        return full_text
    except Exception as e:
        print(f"Error during OCR processing: {e}")
        raise

def publish_result(channel, doc_id, content, status, error_details=""):
    """Publishes the OCR result back to RabbitMQ."""
    message = {
        "documentId": doc_id,
        "contentText": content,
        "status": status,
        "errorDetails": str(error_details)
    }
    channel.basic_publish(
        exchange=RESULT_EXCHANGE,
        routing_key=RESULT_ROUTING_KEY,
        body=json.dumps(message),
        properties=pika.BasicProperties(content_type='application/json')
    )
    print(f"Published {status} result for document ID {doc_id}")


def callback(ch, method, properties, body):
    # decode bytes to a string, then parse it as JSON
    message_content = json.loads(body.decode())
    doc_id = int(message_content)
    print(f"OCR Worker: [x] Received document ID for processing: {doc_id}")

    try:
        # Step 1: fetch metadata to get storagepath
        metadata = get_document_metadata(doc_id)
        if not metadata or 'storagePath' not in metadata:
            raise ValueError(f"Could not fetch valid metadata or storagePath for doc ID {doc_id}.")

        # Step 2: download from MinIO
        pdf_bytes = download_file_from_minio(metadata['storagePath'])
        if not pdf_bytes:
            raise ValueError(f"Could not download file {metadata['storagePath']} from MinIO.")

        # Step 3: Perform OCR
        extracted_text = perform_ocr(pdf_bytes)
        
        # Step 4: Publish success result
        publish_result(ch, doc_id, extracted_text, "SUCCESS")

    except Exception as e:
        # Step 5: If any step fails, publish a failure result
        print(f"An error occurred processing document {doc_id}: {e}")
        publish_result(ch, doc_id, "", "FAILED", str(e))
    
    finally:
        # Step 6: ALWAYS ack the og message
        ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    credentials = pika.PlainCredentials(RABBITMQ_USER, RABBITMQ_PASS)
    connection = None
    
    while not connection:
        try:
            connection = pika.BlockingConnection(pika.ConnectionParameters(host=RABBITMQ_HOST, credentials=credentials))
            print('OCR Worker: Successfully connected to RabbitMQ.')
        except pika.exceptions.AMQPConnectionError:
            print('OCR Worker: Failed to connect to RabbitMQ. Retrying in 5 seconds...')
            time.sleep(5)

    channel = connection.channel()
    # Ensure the queue exists
    channel.queue_declare(queue=OCR_QUEUE, durable=False)

    channel.basic_consume(queue=OCR_QUEUE, on_message_callback=callback)

    print('OCR Worker: [*] Waiting for messages. To exit press CTRL+C')
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