set -euxo pipefail
# FIXME HACK. This sets no concurrency for yarn but it will set this into ~/.yarnrc, overwriting any value you may have...
export CHILD_CONCURRENCY=1
yarn config set child-concurrency 1
(cd web/html/src; yarn install --frozen-lockfile)
(cd web/html/src; yarn build:novalidate)
echo ""
