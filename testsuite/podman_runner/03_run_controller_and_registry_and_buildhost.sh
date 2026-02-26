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
