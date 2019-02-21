set -euxo pipefail
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; rm -rf node_modules)
yarn install --frozen-lockfile
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn zip)
yarn autoclean --force
echo "susemanager-nodejs-modules.tar.gz"
