#!/usr/bin/env bash

zypper --non-interactive in susemanager-nodejs-sdk-devel

cd /manager/web/html/src

# link to node_modules from susemanager-nodejs-sdk-devel
ln -s /usr/lib/node_modules/ node_modules

# execute eslint
./node_modules/eslint/bin/eslint.js -f codeframe .
