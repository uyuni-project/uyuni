set -euxo pipefail
# This lock is shared with web/setup.sh
(
    # Only run the below block if we're the first to acquire the lock
    if flock -x -n 200 ; then
        cd susemanager-frontend/susemanager-nodejs-sdk-devel;
        yarn install --force --ignore-optional --frozen-lockfile;
        yarn autoclean --force;
    else
        # Wait for the lock to be released and then continue
        flock -x 200;
    fi
) 200>/tmp/setup_yarn.lock
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn zip)
echo "susemanager-nodejs-modules.tar.gz"
