#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo buildhostproductuuid > /tmp/buildhost_product_uuid

AUTH_REGISTRY_USER=$(echo "$AUTH_REGISTRY_CREDENTIALS"| cut -d\| -f1)
AUTH_REGISTRY_PASSWD=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d\| -f2)
sudo -i podman run --rm -d --network network -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION
sudo -i podman run --rm -d --network network --name $AUTH_REGISTRY -h $AUTH_REGISTRY -e AUTH_REGISTRY=${AUTH_REGISTRY} -e AUTH_REGISTRY_USER=${AUTH_REGISTRY_USER} -e AUTH_REGISTRY_PASSWD={$AUTH_REGISTRY_USER} ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry-auth:$UYUNI_VERSION
sudo -i podman run --privileged --rm -d --network network -v ${src_dir}/testsuite:/testsuite -v /tmp/buildhost_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -v /var/run/docker.sock:/var/run/docker.sock --name buildhost -h buildhost ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
sudo -i podman exec -d buildhost dockerd
echo "JORDI DEBUG**************************************************"
sleep 60
sudo -i podman exec -ti buildhost docker ps
sudo -i podman exec -ti buildhost docker version
sudo -i podman exec -ti buildhost docker build -t test /testsuite/dockerfiles/buildhost
echo "JORDI DEBUG**************************************************"
sudo podman ps
