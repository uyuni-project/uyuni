#! /bin/sh

set -e

git config --global --add safe.directory /manager

echo -e "### Server helm chart tests\n"
cd /manager/containers/server-helm
./test.sh

echo -e "\n### Proxy helm chart tests\n"
cd /manager/containers/proxy-helm
./test.sh
