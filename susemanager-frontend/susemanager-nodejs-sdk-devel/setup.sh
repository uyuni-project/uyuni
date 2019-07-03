set -euxo pipefail
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; rm -rf node_modules)
yarn install --frozen-lockfile
yarn autoclean --force
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn zip)
echo "susemanager-nodejs-modules.tar.gz"
