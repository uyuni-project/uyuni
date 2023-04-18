#!/bin/bash
set -xe
export CR=docker
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo ubuntuminionproductuuid > /tmp/ubuntu_product_uuid

${CR} run --privileged --rm -d --network uyuni-network-1 -v /tmp/ubuntu_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/test-all-in-one:/tmp -v ${src_dir}/testsuite/${CR}_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name deblike_minion -h deblike_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-ubuntu-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh uyuni-server-all-in-one-test 1-DEBLIKE-KEY"
${CR} exec deblike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
${CR} exec deblike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
