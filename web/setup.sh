set -euxo pipefail

cd web/html/src;
yarn install --force --ignore-optional --frozen-lockfile;
yarn autoclean --force;
yarn zip;
mv node-modules.tar.gz ../../node-modules.tar.gz;
echo "node-modules.tar.gz"
