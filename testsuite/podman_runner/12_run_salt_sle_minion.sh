#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo opensuseminionproductuuid > /tmp/opensuse_product_uuid

sudo -i podman run --privileged --rm -d --network network -v /tmp/opensuse_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp_testing -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -p 9091:9090 --name sleminion -h sleminion ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-opensuse-minion:$UYUNI_VERSION /usr/lib/systemd/systemd
sudo -i podman exec sleminion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
sudo -i podman exec -d sleminion bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
sudo -i podman exec sleminion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp_testing/authorized_keys /root/.ssh/"
sudo -i podman exec -d sleminion prometheus
sudo -i podman exec -d sleminion node_exporter
sudo -i podman exec -d sleminion prometheus-apache_exporter
sudo -i podman exec -d -e DATA_SOURCE_NAME="postgresql://user:passwd@localhost:5432/database?sslmode=disable" sleminion prometheus-postgres_exporter
sudo -i podman exec -d sleminion bash -c "exporter_exporter -config.file /etc/exporter_exporter.yaml -config.dirs /etc/exporter_exporter.d"

sudo -i podman exec sleminion bash -c "sed -e 's/http:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo -i podman exec sleminion bash -c "sed -e 's/https:\/\/download.opensuse.org/http:\/\/server\/pub\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo -i podman exec sleminion bash -c "echo 'root:linux' | chpasswd"
sudo -i podman exec sleminion bash -c "mkdir -p /srv/www/htdocs/pub/bootstrap && cd /srv/www/htdocs/pub/bootstrap && curl http://server/pub/bootstrap/bootstrap.sh > bootstrap.sh"
