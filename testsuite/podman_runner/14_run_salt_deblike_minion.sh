#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo ubuntuminionproductuuid > /tmp/testing/ubuntu_product_uuid

$PODMAN_CMD run --pull newer --privileged -d --network network -p 9093:9090 -v /tmp/testing/ubuntu_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name deblike_minion -h deblike_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-ubuntu-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-DEBLIKE-KEY"
$PODMAN_CMD exec deblike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec deblike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec -d deblike_minion prometheus-node-exporter
$PODMAN_CMD exec -d deblike_minion prometheus-apache-exporter
$PODMAN_CMD exec -d -e DATA_SOURCE_NAME="postgresql://user:passwd@localhost:5432/database?sslmode=disable" deblike_minion prometheus-postgres-exporter
$PODMAN_CMD exec -d deblike_minion bash -c "prometheus-exporter-exporter -config.file /etc/exporter_exporter.yaml -config.dirs /etc/exporter_exporter.d"

$PODMAN_CMD exec deblike_minion bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/apt/sources.list.d/*"
$PODMAN_CMD exec deblike_minion bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/apt/sources.list.d/*"
