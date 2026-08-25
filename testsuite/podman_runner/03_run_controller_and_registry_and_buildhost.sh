#!/bin/bash
set -ex
source "$(dirname "$0")/helpers.sh"
PODMAN_CMD="${PODMAN_CMD:-sudo -i podman}"
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

#echo buildhostproductuuid > /tmp/buildhost_product_uuid

AUTH_REGISTRY_USER=$(echo "$AUTH_REGISTRY_CREDENTIALS"| cut -d\| -f1)
AUTH_REGISTRY_PASSWD=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d\| -f2)
# Start controller with tail as PID 1 — same fix as buildhost.
# Image default CMD is 'ssh-keygen -A && sshd -De'; sshd spawns threads that
# hold cgroup_mutex during init, which can block the profile.local exec call
# that follows immediately. Fix: use tail as PID 1 (zero cgroup churn), run
# all exec calls, generate host keys, then start sshd last via exec -d.
# --systemd=false skips podman's sd_notify cgroup integration (extra mutex ops).
# See docs/adr/0001-ci-queue-jam-podman-exec-hang.md.
sudo -i podman run --pull missing --rm -d --network network --systemd=false -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION bash -c "exec tail -f /dev/null"
cat <<EOF | $PODMAN_CMD exec -i controller bash -c 'cat > /etc/profile.local'
# Generated /etc/profile.local for testsuite environment
export SCC_CREDENTIALS="test|test"
export AUTH_REGISTRY=${AUTH_REGISTRY}
export AUTH_REGISTRY_CREDENTIALS="${AUTH_REGISTRY_CREDENTIALS}"
export NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY}
export PUBLISH_CUCUMBER_REPORT=${PUBLISH_CUCUMBER_REPORT}
export PROVIDER=podman
export SERVER=server
export HOSTNAME=controller
export MINION=sle_minion
export RHLIKE_MINION=rhlike_minion
export DEBLIKE_MINION=deblike_minion
export BUILD_SOURCES="downloadcontent.opensuse.org"
export CONTAINER_RUNTIME="podman"
export IS_USING_BUILD_IMAGE="False"
export IS_USING_PAYGO_SERVER="False"
export IS_USING_SCC_REPOSITORIES="False"
export SERVER_INSTANCE_ID="None"
export BETA_ENABLED="False"
export GITPROFILES="https://github.com/uyuni-project/uyuni.git#:testsuite/features/profiles/github_runner"
export PXEBOOT_IMAGE=sles15sp6
export TAGS="\"not @flaky\""
EOF

# Do all controller exec calls before starting registries — registry worker
# threads hold cgroup_mutex briefly during init, which can block exec into the
# controller (same global-mutex issue as salt-minion/buildhost).
# See docs/adr/0001-ci-queue-jam-podman-exec-hang.md.
$PODMAN_CMD exec controller ssh-keygen -A
$PODMAN_CMD exec -d controller /usr/sbin/sshd -D

# Registries start after all controller exec calls are done.
sudo -i podman run --rm -d --pull missing --network network --name $AUTH_REGISTRY -h $AUTH_REGISTRY -e AUTH_REGISTRY=${AUTH_REGISTRY} -e AUTH_REGISTRY_USER=${AUTH_REGISTRY_USER} -e AUTH_REGISTRY_PASSWD=${AUTH_REGISTRY_PASSWD} -p 5001:5000 ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry-auth:$UYUNI_VERSION
sudo -i podman run --rm -d --pull missing --network network --name $NO_AUTH_REGISTRY -h $NO_AUTH_REGISTRY -e NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY} -p 5002:5000 ghcr.io/$UYUNI_PROJECT/uyuni/ci-container-registry:$UYUNI_VERSION
# Buildhost disabled — not used in current test runs.
#sudo -i podman run --privileged --rm -d --pull missing --network network -v ${src_dir}/testsuite:/testsuite -v /tmp/buildhost_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh -v /var/run/docker.sock:/var/run/docker.sock --name buildhost -h buildhost ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION bash -c "exec tail -f /dev/null"
#sudo -i podman exec buildhost bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
#sudo -i podman exec buildhost bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
#sudo -i podman exec -d buildhost bash -c 'dockerd > /var/log/dockerd.log 2>&1'
#sudo -i podman exec -d buildhost bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"

sudo podman ps
