set -euxo pipefail
# FIXME HACK. This sets no concurrency for yarn but it will set this into ~/.yarnrc, overwriting any value you may have...
export CHILD_CONCURRENCY=1
yarn config set child-concurrency 1
while [ -f /tmp/uyuni-yarn-install.lock ];do
    sleep 30s
done
touch /tmp/uyuni-yarn-install.lock
(cd web/html/src; yarn install --frozen-lockfile)
(cd web/html/src; yarn build:novalidate)
rm /tmp/uyuni-yarn-install.lock
echo ""
