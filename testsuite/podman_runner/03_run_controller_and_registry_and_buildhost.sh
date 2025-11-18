#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo buildhostproductuuid > /tmp/buildhost_product_uuid

AUTH_REGISTRY_USER=$(echo "$AUTH_REGISTRY_CREDENTIALS"| cut -d\| -f1)
AUTH_REGISTRY_PASSWD=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d\| -f2)
sudo -i podman run --rm -d --network network -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION
sudo -i podman run --rm -d --network network --name $AUTH_REGISTRY -h $AUTH_REGISTRY -e AUTH_REGISTRY=${AUTH_REGISTRY} -e AUTH_REGISTRY_USER=${AUTH_REGISTRY_USER} -e AUTH_REGISTRY_PASSWD={$AUTH_REGISTRY_USER} -p 5001:5000 ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry-auth:$UYUNI_VERSION
sudo -i podman run --rm -d --network network --name $NO_AUTH_REGISTRY -h $NO_AUTH_REGISTRY -e NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY} -p 5002:5000 ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry:$UYUNI_VERSION
sudo -i podman run --privileged --rm -d --network network -v ${src_dir}/testsuite:/testsuite -v /tmp/buildhost_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -v /var/run/docker.sock:/var/run/docker.sock --name buildhost -h buildhost ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
sudo -i podman exec -d buildhost dockerd

sudo -i podman exec buildhost bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo -i podman exec buildhost bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo podman ps

sudo docker pull ghcr.io/$UYUNI_PROJECT/uyuni/opensuse/leap/15.6:master
sudo docker tag ghcr.io/$UYUNI_PROJECT/uyuni/opensuse/leap/15.6:master localhost:5002/opensuse/leap:15.6
sudo docker push localhost:5002/opensuse/leap:15.6

sudo docker pull ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:master
sudo docker tag ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:master localhost:5002/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
sudo docker push localhost:5002/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite

sudo docker login -u ${AUTH_REGISTRY_USER} -p ${AUTH_REGISTRY_PASSWD} localhost:5001
sudo docker tag ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:master localhost:5001/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
sudo docker push localhost:5001/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite
