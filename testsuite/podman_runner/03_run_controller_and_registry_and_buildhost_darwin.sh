#!/bin/bash
set -ex
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

echo buildhostproductuuid > /tmp/testing/buildhost_product_uuid

AUTH_REGISTRY_USER=$(echo "$AUTH_REGISTRY_CREDENTIALS"| cut -d\| -f1)
AUTH_REGISTRY_PASSWD=$(echo "$AUTH_REGISTRY_CREDENTIALS" | cut -d\| -f2)

# --- Registry Hostname/Port Definitions ---
# Host push addresses (using ports mapped to the Podman host/VM)
NO_AUTH_REGISTRY_HOST_ADDR="127.0.0.1:5002"
AUTH_REGISTRY_HOST_ADDR="localhost:5001"
REGISTRY_PORT=":5000"

# --- Run Containers ---

$PODMAN_CMD run -d --security-opt seccomp=unconfined --network network -v /tmp/testing:/tmp --name controller -h controller -v ${src_dir}/testsuite:/testsuite ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-controller-dev:$UYUNI_VERSION
cat <<EOF | $PODMAN_CMD exec -i controller bash --login -c 'cat > /etc/profile.local'
# Generated /etc/profile.local for testsuite environment
export SCC_CREDENTIALS="test|test"
export AUTH_REGISTRY=${AUTH_REGISTRY}
export AUTH_REGISTRY_CREDENTIALS="${AUTH_REGISTRY_CREDENTIALS}"
export NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY}
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

$PODMAN_CMD exec controller bash --login -c 'source /etc/profile.local'
$PODMAN_CMD run -d --network network --name ${AUTH_REGISTRY} -h ${AUTH_REGISTRY} -e AUTH_REGISTRY=${AUTH_REGISTRY} -e AUTH_REGISTRY_USER=${AUTH_REGISTRY_USER} -e AUTH_REGISTRY_PASSWD=${AUTH_REGISTRY_PASSWD} -p 5001:5000 docker.io/library/registry:2
$PODMAN_CMD run -d --network network --name ${NO_AUTH_REGISTRY} -h ${NO_AUTH_REGISTRY} -e NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY} -e REGISTRY_HTTP_ADDR="0.0.0.0:5000" -p 5002:5000 docker.io/library/registry:2

$PODMAN_CMD run --privileged -d --network network -v ${src_dir}/testsuite/dockerfiles/server-all-in-one-dev/mirror:/mirror -v ${src_dir}/testsuite:/testsuite -v /tmp/testing/buildhost_product_uuid:/sys/class/dmi/id/product_uuid -v /tmp/testing:/tmp -v ${src_dir}/testsuite/podman_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --volume /run/dbus/system_bus_socket:/run/dbus/system_bus_socket:ro -v /var/run/docker.sock:/var/run/docker.sock --name buildhost -h buildhost ghcr.io/$UYUNI_PROJECT/uyuni/ci-buildhost:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh server 1-SUSE-KEY-x86_64"
$PODMAN_CMD exec buildhost bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"
$PODMAN_CMD exec buildhost bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/zypp/repos.d/*"

$PODMAN_CMD ps

# -----------------------------------------------------------------------
## 1. WAIT FOR REGISTRIES TO BE UP AND LISTENING
# -----------------------------------------------------------------------

REGISTRIES_TO_CHECK=("${AUTH_REGISTRY_HOST_ADDR}" "${NO_AUTH_REGISTRY_HOST_ADDR}")
TIMEOUT=30
for REGISTRY_ADDR in "${REGISTRIES_TO_CHECK[@]}"; do
    echo "Waiting for registry ${REGISTRY_ADDR} to respond..."
    start_time=$(date +%s)
    # Check for 200/401 response over HTTP (using the same protocol the push will use)
    while ! curl -s -o /dev/null -w "%{http_code}" "http://${REGISTRY_ADDR}/v2/" | grep -E '200|401' &>/dev/null; do
        current_time=$(date +%s)
        elapsed=$((current_time - start_time))
        if [ "$elapsed" -ge "$TIMEOUT" ]; then
            echo "Error: Registry ${REGISTRY_ADDR} failed to start after ${TIMEOUT} seconds."
            exit 1
        fi
        sleep 1
    done
    echo "Registry ${REGISTRY_ADDR} is up."
done

# -----------------------------------------------------------------------
## 2. CONFIGURE HOST/VM PODMAN FOR INSECURE REGISTRIES (V2 Format)
# -----------------------------------------------------------------------

if [[ "$(uname)" == "Darwin" ]]; then
    HOST_REGISTRIES_CONF_NAME="99-insecure-lab-registry.conf"
    HOST_REGISTRIES_CONF_PATH="/tmp/${HOST_REGISTRIES_CONF_NAME}"
    TARGET_DIR="/etc/containers/registries.conf.d"

    echo "Configuring Podman environment for insecure registry ${NO_AUTH_REGISTRY_HOST_ADDR} in V2 format..."

    CONFIG_CONTENT=$(cat <<EOF
[[registry]]
location = "${NO_AUTH_REGISTRY_HOST_ADDR}"
insecure = true
EOF
)

    echo "${CONFIG_CONTENT}" > ${HOST_REGISTRIES_CONF_PATH}
    podman machine ssh "sudo mkdir -p ${TARGET_DIR}"
    podman machine ssh "cat > /tmp/${HOST_REGISTRIES_CONF_NAME}" < ${HOST_REGISTRIES_CONF_PATH}
    podman machine ssh "sudo mv /tmp/${HOST_REGISTRIES_CONF_NAME} ${TARGET_DIR}/${HOST_REGISTRIES_CONF_NAME}"
    rm -f ${HOST_REGISTRIES_CONF_PATH}
fi

# -----------------------------------------------------------------------
## 3. IMAGE PUSH OPERATIONS (Host Podman Push with --tls-verify=false)
# -----------------------------------------------------------------------

# --- First Image: opensuse/leap:15.6 (No Auth Registry) ---
IMAGE_NAME_1="ghcr.io/$UYUNI_PROJECT/uyuni/opensuse/leap/15.6:master"
REGISTRY_TAG_1="${NO_AUTH_REGISTRY_HOST_ADDR}/opensuse/leap:15.6"
echo "Pulling ${IMAGE_NAME_1} and pushing from host Podman..."
$PODMAN_CMD pull ${IMAGE_NAME_1}
$PODMAN_CMD tag ${IMAGE_NAME_1} ${REGISTRY_TAG_1}

NO_AUTH_CONTAINER_STATUS=$($PODMAN_CMD inspect -f '{{.State.Running}}' "${NO_AUTH_REGISTRY}" 2>/dev/null || echo "false")

if [ "${NO_AUTH_CONTAINER_STATUS}" != "true" ]; then
    echo "ERROR: The No-Auth Registry container (${NO_AUTH_REGISTRY}) is not running!"
    echo "--- ${NO_AUTH_REGISTRY} Logs ---"
    $PODMAN_CMD logs "${NO_AUTH_REGISTRY}" || true
    echo "---------------------------"
    exit 1
fi

$PODMAN_CMD push --tls-verify=false ${REGISTRY_TAG_1}

# --- Second Image: uyuni-master-testsuite (No Auth) ---
IMAGE_NAME_2="ghcr.io/$UYUNI_PROJECT/uyuni/uyuni-master-testsuite:master"
REGISTRY_TAG_2_NO_AUTH="${NO_AUTH_REGISTRY_HOST_ADDR}/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite"
echo "Pulling ${IMAGE_NAME_2} and pushing to No-Auth registry from host Podman..."
$PODMAN_CMD pull ${IMAGE_NAME_2}
$PODMAN_CMD tag ${IMAGE_NAME_2} ${REGISTRY_TAG_2_NO_AUTH}
$PODMAN_CMD push --tls-verify=false ${REGISTRY_TAG_2_NO_AUTH}

# --- Second Image: uyuni-master-testsuite (Auth Registry) ---
REGISTRY_TAG_2_AUTH="${AUTH_REGISTRY_HOST_ADDR}/cucutest/systemsmanagement/uyuni/master/docker/containers/uyuni-master-testsuite"
echo "Pushing ${IMAGE_NAME_2} to Auth registry from host Podman..."
$PODMAN_CMD login --tls-verify=false -u ${AUTH_REGISTRY_USER} -p ${AUTH_REGISTRY_PASSWD} ${AUTH_REGISTRY_HOST_ADDR}
$PODMAN_CMD tag ${IMAGE_NAME_2} ${REGISTRY_TAG_2_AUTH}
$PODMAN_CMD push --tls-verify=false ${REGISTRY_TAG_2_AUTH}
