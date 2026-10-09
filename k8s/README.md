# Local Kubernetes deployment

The manifests target Docker Desktop Kubernetes with an `ingress-nginx` controller. PostgreSQL with the `vector` extension, Redis, and Ollama remain external services. The frontend is served by Nginx; browser requests stay on the ingress origin, and Nginx forwards `/chat` to `prismai-backend.prismai.svc.cluster.local:8080`. The backend Service is `ClusterIP` only.

## Build and publish images

The GitHub Actions workflow publishes `prismai-backend` and `prismai-frontend` to GHCR on pushes to `main` and version tags (`v*`). Pull requests build both images without publishing. It uses the repository's `GITHUB_TOKEN`; ensure Actions has permission to write packages. If the packages are private, configure an `imagePullSecret` in the `prismai` namespace before deploying.

For a local build from the repository root:

```powershell
docker build -f PrismAI/Dockerfile -t prismai-backend:local .
docker build -f PrismUI/Dockerfile -t prismai-frontend:local .
```

The Kubernetes manifest defaults to the `main` GHCR tags. Update the two `image` fields in `10-app.yaml` to the tags you intend to deploy.

## Prepare dependencies

Make sure PostgreSQL has the `vector` extension available and accepts connections from Docker Desktop Kubernetes. PrismAI's schema initialization creates the extension and tables. Redis and Ollama must also be reachable from the cluster. The configured Ollama models must already be available to your Ollama server.

Copy the example configuration and replace every `REPLACE_WITH_...` value with the addresses, ports, model names, generation options, and application settings for your environment:

```powershell
Copy-Item k8s/prismai-config.env.example k8s/prismai-config.env
notepad k8s/prismai-config.env
```

The `/models` and `/var/lib/prismai/documents` paths are container mount paths. Keep them aligned with the volume mounts in `10-app.yaml`. Create storage and the namespace, then create the ConfigMap and database Secret from your local values:

```powershell
kubectl apply -f k8s/00-storage.yaml
kubectl -n prismai create configmap prismai-config --from-env-file=k8s/prismai-config.env
kubectl -n prismai create secret generic prismai-db `
  --from-literal=SPRING_DATASOURCE_USERNAME='replace-with-your-db-user' `
  --from-literal=SPRING_DATASOURCE_PASSWORD='replace-with-your-password'
```

When configuration changes, update the ConfigMap with:

```powershell
kubectl -n prismai create configmap prismai-config `
  --from-env-file=k8s/prismai-config.env `
  --dry-run=client -o yaml | kubectl apply -f -
```

Update the database Secret using the same `create secret generic` command with `--dry-run=client -o yaml | kubectl apply -f -` appended.

## Load embedding models

The ONNX model and tokenizer are not included in the image. The storage step above creates a PVC and a small loader pod for copying your local files into it:

```powershell
kubectl -n prismai wait --for=condition=Ready pod/prismai-model-loader --timeout=180s
kubectl cp "$env:USERPROFILE\Downloads\tokenizer.json" prismai/prismai-model-loader:/models/tokenizer.json
kubectl cp "$env:USERPROFILE\Downloads\embedding_model.onnx" prismai/prismai-model-loader:/models/embedding_model.onnx
```

Change the source paths if your files are elsewhere. The backend mounts these files read-only at `/models`. The document-store PVC is mounted separately and retains files written by the backend.

## Install ingress and deploy

Install the ingress-nginx controller in Docker Desktop Kubernetes if it is not already installed. One option is Helm:

```powershell
helm upgrade --install ingress-nginx ingress-nginx `
  --repo https://kubernetes.github.io/ingress-nginx `
  --namespace ingress-nginx --create-namespace
```

Then apply the application resources and map the local hostname to loopback in the Windows hosts file (`C:\Windows\System32\drivers\etc\hosts`):

```text
127.0.0.1 prismai.local
```

```powershell
kubectl apply -f k8s/10-app.yaml
kubectl -n prismai get pods,services,ingress
```

Open `http://prismai.local`. If the ingress controller is exposed on a different local address, map `prismai.local` to that address instead. The frontend's internal Nginx proxy and the Ingress both disable response buffering so SSE tokens reach the browser incrementally.
