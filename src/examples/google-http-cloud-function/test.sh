npm install @google-cloud/functions-framework
OPENAPI_KEY=$(echo $OPENAPI_KEY) npx @google-cloud/functions-framework --target=runAgent --source=../../../build/js/packages/aigentic-src-examples-google-http-cloud-function
