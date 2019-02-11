#!/usr/bin/env bash

cd /manager/web/html/src

NOYARNPOSTINSTALL=1 yarn install

# execute eslint
./node_modules/eslint/bin/eslint.js -f codeframe .
