#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git
npm install -g flow-bin@0.93.0

cd /manager/web/html/src

yarn install
yarn build
yarn lint
yarn test
