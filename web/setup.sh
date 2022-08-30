set -euxo pipefail

cd web/html/src;
rm -rf node_modules;
yarn install --force --ignore-optional --production=true --frozen-lockfile;
yarn autoclean --force;
yarn zip;
mv node-modules.tar.gz ../../node-modules.tar.gz;
echo "node-modules.tar.gz"
