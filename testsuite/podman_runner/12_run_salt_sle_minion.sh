#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo opensuseminionproductuuid > /tmp/testing/opensuse_product_uuid

$PODMAN_CMD run --pull newer --privileged -d --network network -v /tmp/testing/opensuse_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -p 9091:9090 --name sle_minion -h sle_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
$PODMAN_CMD exec sle_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec sle_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec -d sle_minion prometheus
$PODMAN_CMD exec -d sle_minion node_exporter
$PODMAN_CMD exec -d sle_minion prometheus-apache_exporter
$PODMAN_CMD exec -d -e DATA_SOURCE_NAME="postgresql://user:passwd@localhost:5432/database?sslmode=disable" sle_minion prometheus-postgres_exporter
$PODMAN_CMD exec -d sle_minion bash -c "exporter_exporter -config.file /etc/exporter_exporter.yaml -config.dirs /etc/exporter_exporter.d"

$PODMAN_CMD exec sle_minion bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
$PODMAN_CMD exec sle_minion bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
