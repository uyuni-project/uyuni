#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

# declare /manager as "safe"
git config --global --add safe.directory /manager

cd /manager/web/html/src

yarn install
yarn build
yarn flow stop
yarn lint
yarn test
echo "Javascript linting ran successfully"
