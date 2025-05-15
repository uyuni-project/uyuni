#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo ubuntuminionproductuuid > /tmp/ubuntu_product_uuid

sudo -i podman run --privileged --rm -d --network network -p 9093:9090 -v /tmp/ubuntu_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp_testing -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name deblikeminion -h deblikeminion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-ubuntu-minion:$UYUNI_VERSION /usr/lib/systemd/systemd
sudo -i podman exec deblikeminion bash -c "echo \"PermitRootLogin yes\" >> /etc/ssh/sshd_config"
sudo -i podman exec deblikeminion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
sudo -i podman exec -d deblikeminion bash -c "/salt-minion-entry-point.sh server 1-DEBLIKE-KEY"
sudo -i podman exec deblikeminion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp_testing/authorized_keys /root/.ssh/"
sudo -i podman exec -d deblikeminion prometheus-node-exporter
sudo -i podman exec -d deblikeminion prometheus-apache-exporter
sudo -i podman exec -d -e DATA_SOURCE_NAME="postgresql://user:passwd@localhost:5432/database?sslmode=disable" deblikeminion prometheus-postgres-exporter
sudo -i podman exec -d deblikeminion bash -c "prometheus-exporter-exporter -config.file /etc/exporter_exporter.yaml -config.dirs /etc/exporter_exporter.d"

sudo -i podman exec deblikeminion bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server.test.lan\/pub\/mirror\/download.opensuse.org/g' -i /etc/apt/sources.list.d/*"
sudo -i podman exec deblikeminion bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server.test.lan\/pub\/mirror\/download.opensuse.org/g' -i /etc/apt/sources.list.d/*"
sudo -i podman exec deblikeminion bash -c "echo 'root:linux' | chpasswd"
