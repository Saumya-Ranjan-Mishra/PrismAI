# PrismAI

Local Ollama setup for serving Phi-3 over Kubernetes with OpenAI-compatible and native Ollama endpoints.

## Base URL

- `http://localhost:11434`

Use `kubectl port-forward` to map local port 11434 to your Ollama service.

```
kubectl port-forward svc/phi3-cpu-ollama-svc 11434:11434
```

## Health And Model Commands (PowerShell)

### Health check

```powershell
Invoke-WebRequest -Uri "http://localhost:11434/" -Method Head
```

### List installed models

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -Method Get
```

### Show model details

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/show" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"name":"phi3:mini"}'
```

### Pull model

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/pull" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"name":"phi3:mini"}'
```

## Endpoint Reference

### `POST /v1/chat/completions`

OpenAI-compatible chat endpoint.

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:11434/v1/chat/completions" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"model":"phi3:mini","messages":[{"role":"user","content":"Hello!"}],"max_tokens":64}'
$response.choices[0].message.content
```

### `POST /api/chat`

Native Ollama chat endpoint.

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/chat" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"model":"phi3:mini","messages":[{"role":"user","content":"Hello!"}]}'
```

### `POST /api/generate`

Native Ollama generate endpoint (single prompt style).

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/generate" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"model":"phi3:mini","prompt":"Write a one-line hello message."}'
```

### `POST /api/show`

Returns metadata/capabilities for a model name.

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/show" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"name":"phi3:mini"}'
```

### `GET /api/tags`

Lists locally available models.

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -Method Get
```

## Troubleshooting

### `model 'phi3:mini' not found`

- Model is not yet downloaded in Ollama.
- Check with:

```powershell
Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -Method Get
```

### `400` from `/v1/chat/completions`

Usually invalid JSON or missing required fields.

Required fields:

- `model`
- `messages` (array of objects with `role` and `content`)

### Response looked empty in PowerShell

PowerShell table formatting hides nested values. Print the content explicitly:

```powershell
$response = Invoke-RestMethod -Uri "http://localhost:11434/v1/chat/completions" -Method Post -Headers @{"Content-Type"="application/json"} -Body '{"model":"phi3:mini","messages":[{"role":"user","content":"Reply with exactly: Hello from phi3"}]}'
$response.choices[0].message.content
```

### Zscaler/corporate proxy blocks model pull

If logs show a Zscaler HTML caution page when pulling from `registry.ollama.ai`, ask network/security to allow:

- `registry.ollama.ai`
- `r.ollama.ai`
- `ollama.com`
- `*.ollama.ai`

Without allowlisting, model pulls may fail intermittently or permanently.
