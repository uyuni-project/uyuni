#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo rhminionproductuuid > /tmp/rh_product_uuid

sudo -i podman run --privileged --rm -d --network network -p 9092:9090 -v /tmp/rh_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name rhlike_minion -h rhlike_minion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-rocky-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-RH-LIKE-KEY"

# sleep 10
sudo -i podman exec rhlike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
sudo -i podman exec rhlike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
sudo -i podman exec -d rhlike_minion node_exporter
sudo -i podman exec -d rhlike_minion prometheus-apache_exporter
sudo -i podman exec -d -e DATA_SOURCE_NAME="postgresql://user:passwd@localhost:5432/database?sslmode=disable" rhlike_minion prometheus-postgres_exporter

sudo -i podman exec rhlike_minion bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/yum.repos.d/*"
sudo -i podman exec rhlike_minion bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/yum.repos.d/*"
