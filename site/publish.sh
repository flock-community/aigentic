npm install
npm run build
docker buildx build --platform linux/amd64 -t europe-docker.pkg.dev/aigentic-458115/aigentic-docker/aigentic-docs .
docker push europe-docker.pkg.dev/aigentic-458115/aigentic-docker/aigentic-docs
gcloud run deploy aigentic-docs --image=europe-docker.pkg.dev/aigentic-458115/aigentic-docker/aigentic-docs --project=aigentic-458115 --region=europe-west4
