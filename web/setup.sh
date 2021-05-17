set -euxo pipefail
# This lock is shared with susemanager-frontend/susemanager-nodejs-sdk-devel/setup.sh
(
    # Only run the below block if we're the first to acquire the lock
    if flock -n 200 ; then
        cd susemanager-frontend/susemanager-nodejs-sdk-devel;
        # This is a very loaded use of yarn install, please be careful when changing it
        # TODO: remove any previously installed optionalDependencies, was --force
        # --ignore-scripts see https://github.com/vercel/next.js/pull/23056 and https://github.com/yarnpkg/yarn/issues/696
        # --ignore-optional to ensure optionalDependencies are not installed
        # --frozen-lockfile to ensure consistent state
        # --prefer-offline to prefer cache when it's available
        yarn install --force --ignore-optional --ignore-scripts --frozen-lockfile;
        yarn autoclean --force;
    else
        # Wait for the lock to be released and then continue, timeout on 600s
        flock -x -w 600 200;
    fi
) 200>/tmp/setup_yarn.lock
(cd web/html/src; yarn build:novalidate)
echo ""
