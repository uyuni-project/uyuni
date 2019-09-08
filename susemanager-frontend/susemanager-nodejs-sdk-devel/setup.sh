set -euxo pipefail
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; rm -rf node_modules)
yarn install --frozen-lockfile
yarn autoclean --force
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn run remove-packages-before-obs-zip)
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn zip)
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn run restore-packages-after-obs-zip)
echo "susemanager-nodejs-modules.tar.gz"
