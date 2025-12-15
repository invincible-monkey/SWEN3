# Paperless

Paperless is a modern, event-driven Document Management System (DMS) designed for archiving, searching, and analyzing documents. It features automatic OCR, AI-powered summarization, and full-text fuzzy search.

## 🚀 Features

* **📄 Document Archiving:** Securely store PDFs in MinIO Object Storage.
* **👀 Auto-OCR:** Automatically extracts text from uploaded PDFs using a Python worker (Tesseract).
* **GenAI Summaries:** Uses Google Gemini (via Python worker) to generate concise summaries of document content.
* **🔍 Full-Text Search:** Fuzzy search across titles, content, summaries, and tags using Elasticsearch.
* **🏷️ Tagging System:** Organize documents with custom tags.
* **📂 Automated Import:** Monitors a local folder (`import/`) and automatically ingests new files.
* **⚡ Reactive UI:** Fast, modern frontend built with SvelteKit 5 and Tailwind CSS.

## 🏗️ Architecture

The system follows a microservices-oriented architecture using Docker Compose:

| Service | Technology | Role |
| :--- | :--- | :--- |
| **REST API** | Java (Spring Boot 3) | Core business logic, API endpoints, orchestration. |
| **Frontend** | TypeScript (SvelteKit) | Reactive user interface. |
| **OCR Worker** | Python (Tesseract) | Listens to RabbitMQ, performs OCR on PDFs. |
| **GenAI Worker** | Python (Google GenAI) | Listens to RabbitMQ, generates content summaries. |
| **PostgreSQL** | Database | Stores relational metadata (Documents, Tags). |
| **Elasticsearch** | Search Engine | Stores indexed text for fuzzy search. |
| **MinIO** | Object Storage | Stores the raw PDF files (S3 compatible). |
| **RabbitMQ** | Message Broker | Handles async communication between services. |
| **Nginx** | Reverse Proxy | Routes traffic to API and UI via port 80. |

## 🛠️ Prerequisites

* **Docker** & **Docker Compose** installed on your machine.
* *(Optional)* Java 21 & Maven (for local backend development).
* *(Optional)* Node.js 20+ (for local frontend development).

## 🚀 Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/invincible-monkey/SWEN3
    cd paperless
    ```

2.  **Configure Environment Variables:**
    Create a `.env` file in the root directory (or ensure `docker-compose.yml` has the correct values).
    * **Crucial:** You must set `GOOGLE_API_KEY` for the GenAI worker to function.

3.  **Start the System:**
    ```bash
    docker-compose up --build -d
    ```
    *Wait for all containers to report "healthy". The first run may take a few minutes to download images.*

4.  **Access the Application:**
    Open your browser and navigate to: **http://localhost**

## 🧪 Usage

### Uploading Documents
* **Web UI:** Click "Browse..." on the dashboard, select a PDF, and click "Upload".
* **Folder Drop:** Drop any PDF file into the `import/` folder in the project root. It will be automatically detected and processed within 10 seconds.

### Searching
Type any keyword in the search bar. The system uses "fuzzy matching", so searching for "recipt" will correctly find "receipt".

## 🧪 Running Tests

We strictly follow the Testing Pyramid with Unit and Integration tests.

### Backend Tests
Run the Spring Boot integration and unit tests:
```bash
cd backend
mvn test