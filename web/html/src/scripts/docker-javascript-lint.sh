#!/usr/bin/env bash
set -e
set -o pipefail

zypper --non-interactive install git
npm install -g flow-bin@0.93.0

cd /manager/web/html/src

NOYARNPOSTINSTALL=1 yarn install
yarn build
yarn lint
