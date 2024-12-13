#!/bin/bash
set -xe
echo opensusesshproductuuid > /tmp/opensuse_ssh_product_uuid
sudo -i podman run --privileged --rm -d --network network -v /tmp/opensuse_ssh_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp --name opensusessh -h opensusessh ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION

