set -euxo pipefail

cd web/html/src;
rm -rf node_modules;
yarn install --force --ignore-optional --production=true --frozen-lockfile;
yarn autoclean --force;
yarn zip;
mv node_modules.tar.gz ../../node_modules.tar.gz;
echo "node_modules.tar.gz"
