#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo ubuntuminionproductuuid > /tmp/ubuntu_product_uuid

sudo -i podman run --privileged --rm -d --network network -v /tmp/ubuntu_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name deblike_minion -h deblike_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-ubuntu-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-DEBLIKE-KEY"
sudo -i podman exec deblike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
sudo -i podman exec deblike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
