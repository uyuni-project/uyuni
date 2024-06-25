#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

# declare /manager as "safe"
git config --global --add safe.directory /manager

cd /manager/web/html/src

yarn install
yarn build:novalidate
yarn lint:production
yarn test
yarn tsc
echo "Javascript linting ran successfully"
