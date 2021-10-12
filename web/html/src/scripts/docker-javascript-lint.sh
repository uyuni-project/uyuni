#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

cd /manager/web/html/src

yarn install
yarn build
yarn lint:production
yarn test
yarn tsc
echo "Javascript linting ran successfully"
