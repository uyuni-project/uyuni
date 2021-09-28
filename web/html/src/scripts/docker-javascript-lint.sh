#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

cd /manager/web/html/src

yarn install
yarn build
yarn lint
yarn test
yarn tsc
echo "Javascript linting ran successfully"
