#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

echo opensusesshproductuuid > /tmp/testing/opensuse_ssh_product_uuid
$PODMAN_CMD run --pull newer --privileged --rm -d --network network -v /tmp/testing/opensuse_ssh_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp --name opensusessh -h opensusessh ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION
$PODMAN_CMD exec opensusessh bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
$PODMAN_CMD exec opensusessh bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
$PODMAN_CMD exec opensusessh bash -c "echo 'root:linux' | chpasswd && echo 123456789 > /etc/machine-id"
$PODMAN_CMD exec opensusessh bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec opensusessh bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
