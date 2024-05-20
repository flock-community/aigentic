../../../gradlew build

gcloud functions deploy aigentic-google-cloud-http-function-example \
  --project flock-signal \
  --region europe-west2 \
  --trigger-http \
  --allow-unauthenticated \
  --runtime=nodejs20\
  --max-instances=1 \
  --source ../../../build/js/packages/aigentic-src-examples-google-http-cloud-function \
  --entry-point runAgent \
  --set-secrets 'OPENAI_KEY=OPENAI_KEY:latest'
