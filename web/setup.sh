set -euxo pipefail
# This lock is shared with susemanager-frontend/susemanager-nodejs-sdk-devel/setup.sh
(
    # Timeout on 600s
    flock -x -w 600 200;
    cd web/html/src;
    yarn install --ignore-optional --ignore-scripts --frozen-lockfile --prefer-offline;
) 200>/tmp/setup_yarn.lock
(cd web/html/src; yarn build:novalidate)
echo ""
