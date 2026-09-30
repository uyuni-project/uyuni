set -euxo pipefail

npm --prefix web run clean;
npm --prefix web ci --ignore-scripts --save=false --omit=dev;
npm --prefix web run zip;
echo "node_modules.tar.gz"
