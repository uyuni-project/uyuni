#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

mkdir -p /tmp/testing/ssl

# Generate the SSL certificates
$PODMAN_CMD run --cap-add AUDIT_CONTROL \
    --tmpfs /run \
    -v root:/root:z \
    -v ${src_dir}/testsuite:/testsuite \
    -v /tmp/testing:/tmp \
    -v /tmp/testing/ssl:/ssl:z \
    --name=ssl-generator \
    --network network \
    --pull newer \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION \
    bash -xc "/testsuite/podman_runner/generate_certificates.sh"

# Generate the Secret for the SSL certificates and the DB credentials
$PODMAN_CMD secret create uyuni-ca /tmp/testing/ssl/ca.crt
$PODMAN_CMD secret create uyuni-db-ca /tmp/testing/ssl/ca.crt
$PODMAN_CMD secret create uyuni-cert /tmp/testing/ssl/server.crt
$PODMAN_CMD secret create uyuni-key /tmp/testing/ssl/server.key
$PODMAN_CMD secret create uyuni-db-cert /tmp/testing/ssl/reportdb.crt
$PODMAN_CMD secret create uyuni-db-key /tmp/testing/ssl/reportdb.key
echo -n "admin" | $PODMAN_CMD secret create uyuni-db-user -
echo -n "spacewalk" | $PODMAN_CMD secret create uyuni-db-pass -
echo -n "dbadmin" | $PODMAN_CMD secret create uyuni-db-admin-user -
echo -n "dbpass" | $PODMAN_CMD secret create uyuni-db-admin-pass -
echo -n "pythia_susemanager" | $PODMAN_CMD secret create uyuni-reportdb-user -
echo -n "pythia_susemanager" | $PODMAN_CMD secret create uyuni-reportdb-pass -

# Start the Database container
$PODMAN_CMD run \
    --cgroups=no-conmon \
    -d \
    --shm-size=0 \
    --name uyuni-db \
    --hostname uyuni-db.mgr.internal \
    --network-alias db \
    --network-alias reportdb \
    --pull newer \
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
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-postgresql:$UYUNI_VERSION

# Wait for postgresql to be up
max_iterations=12
iteration=0

while [ "$iteration" -lt "$max_iterations" ]; do
  if $PODMAN_CMD exec -ti uyuni-db pg_isready -U pgadmin -h localhost -p 5432; then
    if [ $? -eq 0 ]; then
      echo "uyuni-db up and running."
      break
    else
      echo "uyuni-db pg_isready failed, retrying..."
    fi
  else
    echo "Error running podman exec -ti uyuni-db pg_isready, retrying..."
  fi

  iteration=$((iteration + 1))
  sleep 5
done

if [ "$iteration" -eq "$max_iterations" ]; then
  echo "Timeout: uyuni-db ps_isready did not pass within 1 minute."
  exit 1
fi


# Run the setup container
setup_pm_path=`$PODMAN_CMD run -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql spacewalk-setup | grep Setup.pm' | tr -d '\r'`
certs_py_path=`$PODMAN_CMD run -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql python3-spacewalk-certs-tools | grep mgr_ssl_cert_setup.py' | tr -d '\r'`
python_path=${certs_py_path%%certs*}

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
    -v ca-cert:/etc/pki/trust/anchors/ \
    -v run-salt-master:/run/salt/master \
    -v etc-rhn:/etc/rhn \
    -v etc-apache2:/etc/apache2 \
    -v etc-systemd-multi:/etc/systemd/system/multi-user.target.wants \
    -v etc-systemd-sockets:/etc/systemd/system/sockets.target.wants \
    -v etc-salt:/etc/salt \
    -v etc-tomcat:/etc/tomcat \
    -v etc-cobbler:/etc/cobbler \
    -v etc-sysconfig:/etc/sysconfig \
    -v etc-postfix:/etc/postfix \
    -v etc-sssd:/etc/sssd \
    -v ${src_dir}:/manager \
    -v ${src_dir}/schema/spacewalk/spacewalk-schema-upgrade:/usr/bin/spacewalk-schema-upgrade \
    -v ${src_dir}/testsuite:/testsuite \
    -v ${src_dir}/schema/reportdb/upgrade/:/usr/share/susemanager/db/reportdb-schema-upgrade/ \
    -v ${src_dir}/web:/web \
    -v ${src_dir}/.npmrc:/.npmrc \
    -v ${src_dir}/package.json:/package.json \
    -v ${src_dir}/package-lock.json:/package-lock.json \
    -v ${src_dir}/branding:/branding \
    -v ${src_dir}/java:/java \
    -v ${src_dir}/client:/client \
    -v ${src_dir}/susemanager-utils:/susemanager-utils \
    -v ${src_dir}/susemanager:/susemanager \
    -v ${src_dir}/containers/server-image/root/docker-entrypoint-init.d/00-mgrSetup.sh:/docker-entrypoint-init.d/00-mgrSetup.sh \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts.conf:/etc/tomcat/conf.d/tomcat_java_opts.conf \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts_suse.conf:/etc/tomcat/conf.d/tomcat_java_opts_suse.conf \
    -v ${src_dir}/java/conf/default/rhn_taskomatic_daemon.conf:/usr/share/rhn/config-defaults/rhn_taskomatic_daemon.conf \
    -v ${src_dir}/spacewalk/setup/bin/spacewalk-setup:/usr/bin/spacewalk-setup \
    -v ${src_dir}/spacewalk/setup/lib/Spacewalk/Setup.pm:${setup_pm_path} \
    -v ${src_dir}/spacewalk/certs-tools/mgr_ssl_cert_setup.py:${python_path}/certs/mgr_ssl_cert_setup.py \
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    -v /tmp/testing/ssl:/ssl \
    --secret uyuni-db-user,type=env,target=MANAGER_USER \
    --secret uyuni-db-pass,type=env,target=MANAGER_PASS \
    --secret uyuni-reportdb-user,type=env,target=REPORT_DB_USER \
    --secret uyuni-reportdb-pass,type=env,target=REPORT_DB_PASS \
    --secret uyuni-ca,type=mount,target=/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-cert,type=mount,target=/etc/pki/tls/certs/spacewalk.crt \
    --secret uyuni-key,type=mount,target=/etc/pki/tls/private/spacewalk.key \
    --secret uyuni-db-ca,type=mount,target=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT \
    -e UYUNI_FQDN="server"  \
    -e MANAGER_ADMIN_EMAIL="a@b.com"  \
    -e MANAGER_MAIL_FROM="a@b.com"  \
    -e MANAGER_ENABLE_TFTP="n"  \
    -e MANAGER_DB_NAME="susemanager"  \
    -e MANAGER_DB_HOST="db"  \
    -e MANAGER_DB_PORT="5432"  \
    -e REPORT_DB_HOST="reportdb"  \
    -e REPORT_DB_PORT="5432"  \
    -e REPORT_DB_NAME="reportdb"  \
    -e EXTERNALDB_PROVIDER=""  \
    -e ISS_PARENT=""  \
    -e SCC_USER="test"  \
    -e SCC_PASS="test"  \
    -e ORG_NAME='SUSE Test'  \
    -e ADMIN_USER="admin"  \
    -e ADMIN_PASS="admin"  \
    -e ADMIN_FIRST_NAME="Admin"  \
    -e ADMIN_LAST_NAME="Admin"  \
    -e NO_SSL="N"  \
    --cgroupns=host \
    -h server \
    --name=server-setup \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION \
    bash -xc "/testsuite/podman_runner/provide-db-schema.sh && \
             cp /manager/spacewalk/config/var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf && \
             /docker-entrypoint-init.d/00-mgrSetup.sh && \
             /usr/bin/spacewalk-schema-upgrade -y && \
             /testsuite/podman_runner/run_db_migrations.sh susemanager-schema && \
             /testsuite/podman_runner/run_db_migrations.sh uyuni-reportdb-schema && \
             /testsuite/podman_runner/setup_missing_folders.sh" 

${src_dir}/testsuite/podman_runner/setup-nginx-proxy-for-docker-registries.sh

sudo -i rm -rf /tmp/testing/ssl
