#!/usr/bin/env bash
set -euxo pipefail

zypper --non-interactive install git

# declare /manager as "safe"
git config --global --add safe.directory /manager

cd /manager

npm --prefix web ci --ignore-scripts --save=false --omit=dev
npm --prefix web run build -- --check-spec=false
npm --prefix web run lint:production
npm --prefix web run test
npm --prefix web run tsc
echo "All frontend checks completed successfully"
