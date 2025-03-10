#!/bin/bash
set -xe

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

mkdir -p /tmp/ssl

# Generate the SSL certificates
sudo -i podman run --cap-add AUDIT_CONTROL --rm \
    --tmpfs /run \
    -v root:/root:z \
    -v ${src_dir}/testsuite:/testsuite \
    -v /tmp/ssl:/ssl:z \
    --name=ssl-generator \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION \
    bash -xc "/testsuite/podman_runner/generate_certificates.sh"

# Generate the Secret for the SSL certificates and the DB credentials
sudo -i podman secret create uyuni-ca /tmp/ssl/ca.crt
sudo -i podman secret create uyuni-db-cert /tmp/ssl/reportdb.crt
sudo -i podman secret create uyuni-db-key /tmp/ssl/reportdb.key
echo -n "admin" | sudo -i podman secret create uyuni-db-user -
echo -n "spacewalk" | sudo -i podman secret create uyuni-db-pass -
echo -n "dbadmin" | sudo -i podman secret create uyuni-db-admin-user -
echo -n "dbpass" | sudo -i podman secret create uyuni-db-admin-pass -
echo -n "pythia_susemanager" | sudo -i podman secret create uyuni-reportdb-user -
echo -n "pythia_susemanager" | sudo -i podman secret create uyuni-reportdb-pass -

# Start the Database container
sudo -i podman run \
    --cgroups=no-conmon \
    -d \
    --shm-size=0 \
    --name uyuni-db \
    --hostname uyuni-db.mgr.internal \
    --network-alias db \
    --network-alias reportdb \
    --secret uyuni-ca,type=mount,target=/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-db-key,type=mount,mode=0400,target=/etc/pki/tls/private/pg-spacewalk.key \
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
  if sudo -i podman exec -ti uyuni-db pg_isready -U pgadmin -h localhost -p 5432; then
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
setup_pm_path=`sudo -i podman run --rm -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql spacewalk-setup | grep Setup.pm' | tr -d '\r'`
certs_py_path=`sudo -i podman run --rm -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql python3-spacewalk-certs-tools | grep mgr_ssl_cert_setup.py' | tr -d '\r'`
python_path=${certs_py_path%%certs*}

sudo -i podman run --cap-add AUDIT_CONTROL --rm \
    --tmpfs /run \
    -v var-cobbler:/var/lib/cobbler:z \
    -v var-search:/var/lib/rhn/search:z \
    -v var-salt:/var/lib/salt:z \
    -v var-cache:/var/cache:z \
    -v var-spacewalk:/var/spacewalk:z \
    -v var-log:/var/log:z \
    -v srv-salt:/srv/salt:z \
    -v srv-www:/srv/www/:z \
    -v srv-tftpboot:/srv/tftpboot:z \
    -v srv-formulametadata:/srv/formula_metadata:z \
    -v srv-pillar:/srv/pillar:z \
    -v srv-susemanager:/srv/susemanager:z \
    -v srv-spacewalk:/srv/spacewalk:z \
    -v root:/root:z \
    -v ca-cert:/etc/pki/trust/anchors/:z \
    -v run-salt-master:/run/salt/master:z \
    -v etc-tls:/etc/pki/tls:z \
    -v etc-rhn:/etc/rhn:z \
    -v tls-key:/etc/pki/spacewalk-tls:z \
    -v etc-apache2:/etc/apache2:z \
    -v etc-systemd-multi:/etc/systemd/system/multi-user.target.wants:z \
    -v etc-systemd-sockets:/etc/systemd/system/sockets.target.wants:z \
    -v etc-salt:/etc/salt:z \
    -v etc-tomcat:/etc/tomcat:z \
    -v etc-cobbler:/etc/cobbler:z \
    -v etc-sysconfig:/etc/sysconfig:z \
    -v etc-postfix:/etc/postfix:z \
    -v etc-sssd:/etc/sssd:z \
    -v ${src_dir}:/manager \
    -v ${src_dir}/schema/spacewalk/spacewalk-schema-upgrade:/usr/bin/spacewalk-schema-upgrade \
    -v ${src_dir}/testsuite:/testsuite \
    -v ${src_dir}/schema/reportdb/upgrade/:/usr/share/susemanager/db/reportdb-schema-upgrade/ \
    -v ${src_dir}/web:/web \
    -v ${src_dir}/branding:/branding \
    -v ${src_dir}/java:/java \
    -v ${src_dir}/client:/client \
    -v ${src_dir}/susemanager-utils:/susemanager-utils \
    -v ${src_dir}/susemanager:/susemanager \
    -v ${src_dir}/susemanager/bin/mgr-setup:/usr/lib/susemanager/bin/mgr-setup \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts.conf:/etc/tomcat/conf.d/tomcat_java_opts.conf \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts_suse.conf:/etc/tomcat/conf.d/tomcat_java_opts_suse.conf \
    -v ${src_dir}/java/conf/default/rhn_taskomatic_daemon.conf:/usr/share/rhn/config-defaults/rhn_taskomatic_daemon.conf \
    -v ${src_dir}/spacewalk/setup/bin/spacewalk-setup:/usr/bin/spacewalk-setup \
    -v ${src_dir}/spacewalk/setup/lib/Spacewalk/Setup.pm:${setup_pm_path} \
    -v ${src_dir}/spacewalk/certs-tools/mgr_ssl_cert_setup.py:${python_path}/certs/mgr_ssl_cert_setup.py \
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    -v /tmp/ssl:/ssl \
    --secret uyuni-db-user,type=env,target=MANAGER_USER \
    --secret uyuni-db-pass,type=env,target=MANAGER_PASS \
    --secret uyuni-reportdb-user,type=env,target=REPORT_DB_USER \
    --secret uyuni-reportdb-pass,type=env,target=REPORT_DB_PASS \
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
    --cgroupns=host \
    -h server \
    --name=server-setup \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION \
    bash -xc "/testsuite/podman_runner/provide-db-schema.sh && \
             cp /manager/spacewalk/config/var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf && \
             /usr/lib/susemanager/bin/mgr-setup && \
             /usr/bin/spacewalk-schema-upgrade -y && \
             /testsuite/podman_runner/run_db_migrations.sh susemanager-schema && \
             /testsuite/podman_runner/run_db_migrations.sh uyuni-reportdb-schema" 

${src_dir}/testsuite/podman_runner/setup-nginx-proxy-for-docker-registries.sh

sudo -i rm -rf /tmp/ssl
