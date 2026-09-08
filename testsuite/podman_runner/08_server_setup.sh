#!/bin/bash
set -xe

set -u
: "${TEST_IMAGE?TEST_IMAGE not set}"
set +u

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

src_dir=$(cd "$(dirname "$0")/../.." && pwd -P)

mkdir -p /tmp/testing/ssl

# Generate the SSL certificates
$PODMAN_CMD run --rm --cap-add AUDIT_CONTROL \
    --tmpfs /run \
    -v root:/root:z \
    -v "${src_dir}/testsuite:/testsuite" \
    -v /tmp/testing:/tmp \
    -v /tmp/testing/ssl:/ssl:z \
    --name=ssl-generator \
    --network network \
    --pull missing \
    "${SSL_GEN_IMAGE}" \
    bash -xc "/testsuite/podman_runner/generate_certificates.sh"

set +x
# Generate the Secret for the SSL certificates and the DB credentials
$PODMAN_CMD secret create uyuni-ca /tmp/testing/ssl/ca.crt
$PODMAN_CMD secret create uyuni-db-ca /tmp/testing/ssl/ca.crt
$PODMAN_CMD secret create uyuni-cert /tmp/testing/ssl/server.crt
$PODMAN_CMD secret create uyuni-key /tmp/testing/ssl/server.key
$PODMAN_CMD secret create uyuni-db-cert /tmp/testing/ssl/reportdb.crt
$PODMAN_CMD secret create uyuni-db-key /tmp/testing/ssl/reportdb.key
echo -n "admin" | $PODMAN_CMD secret create uyuni-db-user -
echo -n "spacewalk" | $PODMAN_CMD secret create uyuni-db-pass -
echo -n "postgres" | $PODMAN_CMD secret create uyuni-db-admin-user -
echo -n "dbpass" | $PODMAN_CMD secret create uyuni-db-admin-pass -
echo -n "pythia_susemanager" | $PODMAN_CMD secret create uyuni-reportdb-user -
echo -n "pythia_susemanager" | $PODMAN_CMD secret create uyuni-reportdb-pass -
set -x

# Start the Database container
$PODMAN_CMD run \
    --cgroups=no-conmon \
    -d \
    --shm-size=0 \
    --name uyuni-db \
    --hostname uyuni-db.mgr.internal \
    --network-alias db \
    --network-alias reportdb \
    --pull missing \
    --secret uyuni-db-ca,type=mount,target=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-db-key,type=mount,uid=999,mode=0400,target=/etc/pki/tls/private/pg-spacewalk.key \
    --secret uyuni-db-cert,type=mount,target=/etc/pki/tls/certs/spacewalk.crt \
    --secret uyuni-db-admin-user,type=env,target=POSTGRES_USER \
    --secret uyuni-db-admin-pass,type=env,target=POSTGRES_PASSWORD \
    --secret uyuni-db-user,type=env,target=MANAGER_USER \
    --secret uyuni-db-pass,type=env,target=MANAGER_PASS \
    --secret uyuni-reportdb-user,type=env,target=REPORT_DB_USER \
    --secret uyuni-reportdb-pass,type=env,target=REPORT_DB_PASS \
    -v var-pgsql:/var/lib/pgsql/data \
    --network network \
    "ghcr.io/${UYUNI_PROJECT}/uyuni/ci-postgresql:${UYUNI_VERSION}"

# Wait for postgresql to be up
max_iterations=12
iteration=0

while [ "$iteration" -lt "$max_iterations" ]; do
  if $PODMAN_CMD exec -ti uyuni-db pg_isready -U pgadmin -h localhost -p 5432; then
    echo "uyuni-db up and running."
    break
  else
    echo "uyuni-db pg_isready failed or database starting up, retrying..."
  fi

  iteration=$((iteration + 1))
  sleep 5
done

if [ "$iteration" -eq "$max_iterations" ]; then
  echo "Timeout: uyuni-db pg_isready did not pass within 1 minute."
  exit 1
fi

$PODMAN_CMD exec uyuni-db bash -c "echo host all all all scram-sha-256 > /var/lib/pgsql/data/pg_hba_custom.conf"
$PODMAN_CMD exec uyuni-db su postgres -c "/usr/bin/pg_ctl reload"

# Run the setup container is no longer needed. The PR payload and image build takes care of this.

"${src_dir}/testsuite/podman_runner/setup-nginx-proxy-for-docker-registries.sh"

sudo -i rm -rf /tmp/testing/ssl
