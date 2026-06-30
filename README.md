# RAG Chatbot

A document-aware question answering assistant built in Java. Upload a PDF or TXT file and the application answers questions grounded strictly in that document, using a Retrieval-Augmented Generation (RAG) pipeline.

The project runs fully on a local machine with no paid API required, using a local embedding model and a locally hosted LLM through Ollama. It can be switched to Anthropic Claude with a single configuration change.

## What it does

1. A document is uploaded through a REST endpoint.
2. The text is extracted, split into overlapping chunks, and converted into vector embeddings.
3. The embeddings are stored in a PostgreSQL database with the pgvector extension.
4. When a question is asked, the most relevant chunks are retrieved by vector similarity and passed to the language model as context.
5. The model answers using only the retrieved content, and states when the answer is not present in the documents.

## Tech stack

**Language and build:** Java 17, Maven

**Framework:** Spring Boot 3.5.16

**LLM orchestration:** LangChain4j (Spring Boot starter)

**Vector database:** PostgreSQL with pgvector, running in Docker

**Embeddings:** all-MiniLM-L6-v2, running in-process (384 dimensions, no external API)

**Document parsing:** Apache Tika (PDF and TXT)

**Language model:** Ollama with llama3.2:3b for local use, Anthropic Claude as an optional alternative

## Architecture

```
Document (PDF/TXT)
      |
   Apache Tika (text extraction)
      |
   Recursive splitter (chunks of ~300 chars, 30 overlap)
      |
   all-MiniLM-L6-v2 (embedding)
      |
   pgvector (storage)
      |
   ----- question -----
      |
   Vector similarity retrieval
      |
   LLM (Ollama / Claude) + retrieved context
      |
   Grounded answer
```

## Prerequisites

* Java 17 or higher
* Docker Desktop
* Ollama, with the `llama3.2:3b` model pulled

## Getting started

**1. Start the vector database**

```bash
docker compose up -d
```

This starts a PostgreSQL instance with pgvector on port 5432. The embedding table is created automatically on first run.

**2. Pull the local model**

```bash
ollama pull llama3.2:3b
```

**3. Run the application**

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

## API usage

**Upload a document**

```bash
curl -X POST http://localhost:8080/api/upload -F "file=@document.txt"
```

**Ask a question**

```bash
curl -X POST http://localhost:8080/api/chat -F "question=What is the document about?"
```

The assistant answers based only on the uploaded documents. If the answer is not found in them, it says so rather than inventing one.

## Configuration

Database and model settings are defined in `src/main/resources/application.yaml`. The local development setup uses Ollama:

```yaml
langchain4j:
  ollama:
    chat-model:
      base-url: http://localhost:11434
      model-name: llama3.2:3b
```

### Switching to Anthropic Claude

To use Claude instead of the local model, provide an Anthropic API key as an environment variable (it should never be committed to the repository) and configure the Anthropic chat model in `application.yaml`. The rest of the pipeline, including document ingestion and retrieval, stays unchanged. This is one of the benefits of the LangChain4j abstraction: the model can be swapped without touching the application logic.

## Project structure

```
src/main/java/com/berkay/rag_chatbot/
├── RagChatbotApplication.java      Application entry point
├── Assistant.java                  AI service interface (declarative RAG)
├── DocumentIngestionService.java   Parses, splits, embeds and stores documents
├── EmbeddingStoreConfig.java       Embedding model and pgvector configuration
└── ChatController.java             REST endpoints for upload and chat
```

## Notes

The default local model (llama3.2:3b) is small and intended for development and demonstration. Answer quality improves significantly when switching to Anthropic Claude or a larger model. The retrieval pipeline itself is identical in both cases.
