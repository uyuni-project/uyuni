#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

wait_for_server_ready() {
    local timeout=600
    local podman_cmd="${PODMAN_CMD}"

    local start_time
    start_time=$(date +%s)

    echo "Waiting for server container to start and initialize..."

    # Wait for container to be running and not starting/none
    while true; do
        local current_time
        current_time=$(date +%s)
        if [ $((current_time - start_time)) -ge "${timeout}" ]; then
            echo "Timeout reached while waiting for server initialization."
            return 1
        fi

        # Check if container exists and is running
        local running
        running=$(${podman_cmd} inspect --format '{{.State.Running}}' server 2>/dev/null || echo "false")
        if [ "${running}" != "true" ]; then
            echo "Error: Server container is not running!"
            ${podman_cmd} logs server | tail -n 100
            return 1
        fi

        # Inspect health status
        local health
        health=$(${podman_cmd} inspect --format '{{.State.Health.Status}}' server 2>/dev/null || echo "none")
        if [ "${health}" = "healthy" ]; then
            echo "Server container is healthy!"
            break
        elif [ "${health}" = "unhealthy" ]; then
            echo "Error: Server container became unhealthy!"
            ${podman_cmd} logs server | tail -n 100
            return 1
        fi

        printf "."
        sleep 10
    done
    echo "Server is ready!"
    return 0
}

# Ensure no stale server container is left
$PODMAN_CMD rm -f server || true

src_dir=$(cd "$(dirname "$0")/../.." && pwd -P)
SCHEMA_VERSION=$(grep -i "^Version:" "${src_dir}/schema/spacewalk/susemanager-schema.spec" | awk '{print $2}')
SCHEMA_RELEASE=$(grep -i "^Release:" "${src_dir}/schema/spacewalk/susemanager-schema.spec" | awk '{print $2}')
REPORTDB_VERSION=$(grep -i "^Version:" "${src_dir}/schema/reportdb/uyuni-reportdb-schema.spec" | awk '{print $2}')
REPORTDB_RELEASE=$(grep -i "^Release:" "${src_dir}/schema/reportdb/uyuni-reportdb-schema.spec" | awk '{print $2}')

if [[ "${TEST_IMAGE}" == *@sha256:* ]]; then
    START_IMAGE="${TEST_IMAGE}"
else
    START_IMAGE="${TEST_IMAGE}:${UYUNI_VERSION}"
fi

$PODMAN_CMD run --cap-add AUDIT_CONTROL \
    --tmpfs /run \
    -v var-cobbler:/var/lib/cobbler \
    -v var-search:/var/lib/rhn/search \
    -v var-salt:/var/lib/salt \
    -v var-cache:/var/cache \
    -v var-spacewalk:/var/spacewalk \
    -v var-log:/var/log \
    -v srv-salt:/srv/salt \
    -v srv-www:/srv/www \
    -v srv-tftpboot:/srv/tftpboot \
    -v srv-formulametadata:/srv/formula_metadata \
    -v srv-pillar:/srv/pillar \
    -v srv-susemanager:/srv/susemanager \
    -v srv-spacewalk:/srv/spacewalk \
    -v root:/root \
    -v ca-certs:/etc/pki/trust/anchors/ \
    -v run-salt-master:/run/salt/master \
    -v etc-apache2:/etc/apache2 \
    -v etc-systemd-multi:/etc/systemd/system/multi-user.target.wants \
    -v etc-systemd-sockets:/etc/systemd/system/sockets.target.wants \
    -v etc-salt:/etc/salt \
    -v etc-tomcat:/etc/tomcat \
    -v etc-cobbler:/etc/cobbler \
    -v etc-sysconfig:/etc/sysconfig \
    -v etc-postfix:/etc/postfix \
    -v etc-sssd:/etc/sssd \
    -v etc-rhn:/etc/rhn \
    -e TZ="${TZ}" \
    -e SUMA_TEST_SCHEMA_NAME="susemanager-schema" \
    -e SUMA_TEST_SCHEMA_VERSION="${SCHEMA_VERSION}" \
    -e SUMA_TEST_SCHEMA_RELEASE="${SCHEMA_RELEASE}" \
    -e SUMA_TEST_REPORT_SCHEMA_NAME="uyuni-reportdb-schema" \
    -e SUMA_TEST_REPORT_SCHEMA_VERSION="${REPORTDB_VERSION}" \
    -e SUMA_TEST_REPORT_SCHEMA_RELEASE="${REPORTDB_RELEASE}" \
    --secret uyuni-ca,type=mount,target=/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-cert,type=mount,target=/etc/pki/tls/certs/spacewalk.crt \
    --secret uyuni-key,type=mount,target=/etc/pki/tls/private/spacewalk.key \
    --secret uyuni-db-ca,type=mount,target=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-db-user,type=env,target=MANAGER_USER \
    --secret uyuni-db-pass,type=env,target=MANAGER_PASS \
    --secret uyuni-reportdb-user,type=env,target=REPORT_DB_USER \
    --secret uyuni-reportdb-pass,type=env,target=REPORT_DB_PASS \
    -e UYUNI_HOSTNAME="server" \
    -e MANAGER_ADMIN_EMAIL="a@b.com" \
    -e MANAGER_MAIL_FROM="a@b.com" \
    -e MANAGER_ENABLE_TFTP="n" \
    -e MANAGER_DB_NAME="susemanager" \
    -e MANAGER_DB_HOST="db" \
    -e MANAGER_DB_PORT="5432" \
    -e REPORT_DB_HOST="reportdb" \
    -e REPORT_DB_PORT="5432" \
    -e REPORT_DB_NAME="reportdb" \
    -e EXTERNALDB_PROVIDER="" \
    -e SCC_USER="test" \
    -e SCC_PASS="test" \
    -e ORG_NAME='SUSE Test' \
    -e ADMIN_USER="admin" \
    -e ADMIN_PASS="admin" \
    -e ADMIN_FIRST_NAME="Admin" \
    -e ADMIN_LAST_NAME="Admin" \
    -e NO_SSL="N" \
    -e MIRROR_PATH="/mirror" \
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    --cgroupns=host \
    --privileged \
    -h server \
    -p 8443:443 \
    -p 8080:80 \
    -p 9090:9090 \
    -p 4505:4505 \
    -p 4506:4506 \
    -d --name=server \
    --network network \
    --pull missing \
    "${START_IMAGE}"

wait_for_server_ready || exit 1
$PODMAN_CMD exec -d server prometheus
