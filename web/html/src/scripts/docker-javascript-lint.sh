#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

# declare /manager as "safe"
git config --global --add safe.directory /manager

cd /manager

npm ci --ignore-scripts --save=false --omit=dev
npm run build -- --check-spec=false
npm run lint:production
npm run test
npm run tsc
echo "All frontend checks completed successfully"
