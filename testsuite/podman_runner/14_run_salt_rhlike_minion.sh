#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo rhminionproductuuid > /tmp/rh_product_uuid

sudo -i podman run --privileged --rm -d --network uyuni-network-1 -v /tmp/rh_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/test-all-in-one:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name rhlike_minion -h rhlike_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-rocky-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh uyuni-server-all-in-one-test 1-RH-LIKE-KEY"

# sleep 10
sudo -i podman exec rhlike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
sudo -i podman exec rhlike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
