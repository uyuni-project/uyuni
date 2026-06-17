set -euxo pipefail

npm --prefix web run clean;
npm --prefix web ci --ignore-scripts --save=false --omit=dev;
# leftovers and 0 byte tar files create problems in gitea with LFS
# remove them
rm -f web/node_modules/moment-timezone/blah.tar.gz
rm -f web/node_modules/moment-timezone/curlxx.tar.gz
npm --prefix web run zip;
echo "node_modules.tar.gz"
