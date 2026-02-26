#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

echo buildhostproductuuid > /tmp/buildhost_product_uuid

sudo -i podman run --pull newer --rm -d --network network -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION
cat <<EOF | sudo -i podman exec -i controller bash --login -c 'cat > /etc/profile.local'
# Generated /etc/profile.local for testsuite environment
export SCC_CREDENTIALS="test|test"
export PUBLISH_CUCUMBER_REPORT=${PUBLISH_CUCUMBER_REPORT}
export PROVIDER=podman
export SERVER=server
export HOSTNAME=controller
export SSH_MINION=opensusessh
export MINION=sle_minion
export RHLIKE_MINION=rhlike_minion
export DEBLIKE_MINION=deblike_minion
export BUILD_SOURCES="downloadcontent.opensuse.org"
export CONTAINER_RUNTIME="podman"
export IS_USING_BUILD_IMAGE="False"
export IS_USING_PAYGO_SERVER="False"
export IS_USING_SCC_REPOSITORIES="False"
export CATCH_TIMEOUT_MESSAGE="False"
export SERVER_INSTANCE_ID="None"
export BETA_ENABLED="False"
export GITPROFILES="https://github.com/uyuni-project/uyuni.git#:testsuite/features/profiles/github_runner"
export PXEBOOT_IMAGE=sles15sp6
export TAGS="\"not @flaky\""
EOF

sudo -i podman exec controller bash --login -c 'source /etc/profile.local'
sudo -i podman run --privileged --rm -d --pull newer --network network -v ${src_dir}/testsuite:/testsuite -v /tmp/buildhost_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -v /var/run/docker.sock:/var/run/docker.sock --name buildhost -h buildhost ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
sudo -i podman exec -d buildhost dockerd

sudo -i podman exec buildhost bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo -i podman exec buildhost bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
sudo podman ps
