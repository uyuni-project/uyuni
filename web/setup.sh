set -euxo pipefail
(cd web/html/src; yarn install --frozen-lockfile)
(cd web/html/src; yarn build)
echo ""
