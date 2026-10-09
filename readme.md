test data link: https://huggingface.co/datasets/lmsys/chatbot_arena_conversations

## Runtime configuration

The backend requires environment configuration; it has no embedded database credentials, local-service addresses, model paths, or document-store path. Set these values in your IDE run configuration or the environment that starts Maven:

| Environment variables                                                                         | Purpose                                                   |
| --------------------------------------------------------------------------------------------- | --------------------------------------------------------- |
| `SPRING_APPLICATION_NAME`, `SERVER_PORT`                                                      | Application name and HTTP port                            |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`           | PostgreSQL connection; the database must provide pgvector |
| `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`                                            | Redis connection                                          |
| `PRISM_OLLAMA_BASE_URL`                                                                       | Ollama base URL, without an API path                      |
| `PRISM_OLLAMA_CHAT_MODEL`, `PRISM_OLLAMA_INTENT_MODEL`                                        | Ollama models used for chat and intent classification     |
| `PRISM_OLLAMA_KEEP_ALIVE`, `PRISM_OLLAMA_CHAT_TEMPERATURE`, `PRISM_OLLAMA_INTENT_TEMPERATURE` | Ollama generation options                                 |
| `PRISM_EMBEDDING_TOKENIZER_RESOURCE`, `PRISM_EMBEDDING_MODEL_RESOURCE`                        | Tokenizer and embedding model resource locations          |
| `PRISM_EMBEDDING_QUANTIZED_TOKENIZER_RESOURCE`, `PRISM_EMBEDDING_QUANTIZED_MODEL_RESOURCE`    | Quantized tokenizer and model resource locations          |
| `PRISM_DOC_STORAGE_PATH`                                                                      | Writable directory for document files                     |

All listed values are required. Model paths must use Spring resource syntax, such as `file:C:/models/tokenizer.json` on Windows or `file:/models/tokenizer.json` in a container. Keep credentials out of source control.

## Docker and Kubernetes

For GHCR publishing and local Kubernetes deployment, see [the Kubernetes guide](k8s/README.md). Copy `k8s/prismai-config.env.example` to `k8s/prismai-config.env` and enter your environment-specific values; the resulting file is ignored by Git.
