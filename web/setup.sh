set -euxo pipefail
# This lock is shared with susemanager-frontend/susemanager-nodejs-sdk-devel/setup.sh
(
    # Only run the below block if we're the first to acquire the lock
    if flock -x -n 200 ; then
        cd susemanager-frontend/susemanager-nodejs-sdk-devel;
        yarn install --force --ignore-optional --frozen-lockfile;
        yarn autoclean --force;
    else
        # Wait for the lock to be released and then continue, timeout on 600s
        flock -x -w 600 200;
    fi
) 200>/tmp/setup_yarn.lock
(cd web/html/src; yarn build:novalidate)
echo ""
