#!/usr/bin/env bash

### TODO remove this section before merging PR. it's just for testing in jenkins
zypper ar -f http://download.suse.de/ibs/home:/mateialbu:/branches:/Devel:/Galaxy:/Manager:/Head/SLE_12_SP3 "nodejs-sdk"

zypper --non-interactive in --repo "nodejs-sdk" susemanager-nodejs-sdk-devel

### end section

# TODO uncomment when section above is removed
#zypper --non-interactive in susemanager-nodejs-sdk-devel

cd /manager/web/html/src

# link to node_modules from susemanager-nodejs-sdk-devel
ln -s /usr/lib/node_modules/ node_modules

# execute eslint
./node_modules/eslint/bin/eslint.js -f codeframe .
echo "Linting done."