../../../gradlew build --no-configuration-cache
npm install @google-cloud/functions-framework
cd ../../../build/js/packages/aigentic-src-examples-google-http-cloud-function
npx @google-cloud/functions-framework --target=runAgent
