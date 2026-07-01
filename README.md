# RAG Chatbot

A document-aware question answering assistant built in Java. Upload a PDF or TXT file and the application answers questions grounded strictly in that document, using a Retrieval-Augmented Generation (RAG) pipeline.

The language model provider is selectable: the application ships with a fully local setup (no paid API required) and can be switched to Anthropic Claude or OpenAI through a single configuration value.

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

**Language model (selectable):** Ollama for local use, Anthropic Claude, or OpenAI

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
   LLM (Ollama / Claude / OpenAI) + retrieved context
      |
   Grounded answer
```

The active language model is chosen at startup. A single property (`app.llm.provider`) selects the provider, and Spring conditionally creates only the matching model instance. This keeps the application logic, document ingestion, and retrieval identical across all providers, so a provider can be swapped without changing any code.

## Prerequisites

* Java 17 or higher
* Docker Desktop
* Ollama with the `llama3.2:3b` model pulled (only required for the default local setup)

## Getting started

**1. Start the vector database**

```bash
docker compose up -d
```

This starts a PostgreSQL instance with pgvector on port 5432. The embedding table is created automatically on first run.

**2. Pull the local model (for the default Ollama setup)**

```bash
ollama pull llama3.2:3b
```

**3. Run the application**

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080` and uses Ollama by default, so no API key is needed to run it.


## Web interface

Once the application is running, open `http://localhost:8080` in your browser to upload a document and ask questions through a simple web UI. The REST API below is also available for programmatic use.
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

## Choosing your LLM provider

The provider is controlled by `app.llm.provider` in `src/main/resources/application.yaml`. Three values are supported:

| Provider    | Value        | API key required | Notes                              |
|-------------|--------------|------------------|------------------------------------|
| Ollama      | `ollama`     | No               | Runs locally, default              |
| Anthropic   | `anthropic`  | Yes              | Uses Claude models                 |
| OpenAI      | `openai`     | Yes              | Uses GPT models                    |

API keys are read from environment variables and are never stored in the repository.

**To use Anthropic Claude:**

1. Set `app.llm.provider` to `anthropic`.
2. Provide the key as an environment variable:

```bash
# Windows (PowerShell)
$env:ANTHROPIC_API_KEY="your-key-here"

# macOS / Linux
export ANTHROPIC_API_KEY="your-key-here"
```

**To use OpenAI:**

1. Set `app.llm.provider` to `openai`.
2. Provide the key:

```bash
# Windows (PowerShell)
$env:OPENAI_API_KEY="your-key-here"

# macOS / Linux
export OPENAI_API_KEY="your-key-here"
```

Model names for each provider are also configurable in `application.yaml`.

## Configuration

The relevant section of `application.yaml`:

```yaml
app:
  llm:
    provider: ollama          # ollama | anthropic | openai
    ollama:
      base-url: http://localhost:11434
      model-name: llama3.2:3b
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:}
      model-name: claude-sonnet-4-6
    openai:
      api-key: ${OPENAI_API_KEY:}
      model-name: gpt-4o-mini
```

## Project structure

```
src/main/java/com/berkay/rag_chatbot/
├── RagChatbotApplication.java      Application entry point
├── Assistant.java                  AI service interface (declarative RAG)
├── DocumentIngestionService.java   Parses, splits, embeds and stores documents
├── EmbeddingStoreConfig.java       Embedding model and pgvector configuration
├── ChatModelConfig.java            Selects the active LLM provider at startup
└── ChatController.java             REST endpoints for upload and chat

src/main/resources/static/
└── index.html                      Simple web interface
```

## Notes

The default local model (llama3.2:3b) is small and intended for development and demonstration. Answer quality improves significantly when switching to Anthropic Claude or OpenAI. The retrieval pipeline itself is identical across all providers.
