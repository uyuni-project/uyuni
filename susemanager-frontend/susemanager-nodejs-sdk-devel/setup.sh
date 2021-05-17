set -euxo pipefail
# This lock is shared with web/setup.sh
(
    # Timeout on 600s
    flock -x -w 600 200;
    cd susemanager-frontend/susemanager-nodejs-sdk-devel;
    # This is a very loaded use of yarn install, please be careful when changing it
    # TODO: remove any previously installed optionalDependencies, was --force
    # --ignore-scripts see https://github.com/vercel/next.js/pull/23056 and https://github.com/yarnpkg/yarn/issues/696
    # --ignore-optional to ensure optionalDependencies are not installed
    # --frozen-lockfile to ensure consistent state
    # --prefer-offline to prefer cache when it's available
    yarn install --ignore-optional --ignore-scripts --frozen-lockfile --prefer-offline;
) 200>/tmp/setup_yarn.lock
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn run remove-packages-before-obs-zip)
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn zip)
(cd susemanager-frontend/susemanager-nodejs-sdk-devel; yarn run restore-packages-after-obs-zip)
echo "susemanager-nodejs-modules.tar.gz"
