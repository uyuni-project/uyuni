set -euxo pipefail

rm -rf node_modules;
npm ci --ignore-scripts --save=false --omit=dev;
npm run zip;
echo "node-modules.tar.gz"
