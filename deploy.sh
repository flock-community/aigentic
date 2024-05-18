gcloud functions deploy aigentic-google-cloud-http-function \
  --project flock-signal \
  --region europe-west2 \
  --trigger-http \
  --allow-unauthenticated \
  --runtime=nodejs20\
  --max-instances=1 \
  --source build/js/packages/aigentic-src-cloud-google-http-cloud-function \
  --entry-point helloGET
