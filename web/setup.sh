set -euxo pipefail
touch /tmp/uyuni-yarn-install.lock
(cd web/html/src; yarn install --frozen-lockfile)
(cd web/html/src; yarn build:novalidate)
rm /tmp/uyuni-yarn-install.lock
echo ""
