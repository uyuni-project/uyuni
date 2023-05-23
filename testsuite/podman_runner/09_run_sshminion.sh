#!/bin/bash
set -xe
echo opensusesshproductuuid > /tmp/opensuse_ssh_product_uuid
sudo -i podman run --privileged --rm -d --network uyuni-network-1 -v /tmp/opensuse_ssh_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/test-all-in-one:/tmp --name opensusessh -h opensusessh ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION

