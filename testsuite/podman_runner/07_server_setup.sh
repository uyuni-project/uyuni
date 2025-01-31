#!/bin/bash
set -xe

src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

setup_pm_path=`sudo -i podman run --rm -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql spacewalk-setup | grep Setup.pm' | tr -d '\r'`

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
    -v var-pgsql:/var/lib/pgsql:z \
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
    -v ${src_dir}/schema/spacewalk/spacewalk-schema-upgrade:/usr/bin/spacewalk-schema-upgrade \
    -v ${src_dir}/testsuite:/testsuite \
    -v ${src_dir}/schema/spacewalk/upgrade/:/usr/share/susemanager/db/schema-upgrade/ \
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
    -v ${src_dir}/spacewalk/uyuni-setup-reportdb/bin/uyuni-setup-reportdb:/usr/bin/uyuni-setup-reportdb \
    -v ${src_dir}/spacewalk/setup/bin/spacewalk-setup:/usr/bin/spacewalk-setup \
    -v ${src_dir}/spacewalk/setup/lib/Spacewalk/Setup.pm:${setup_pm_path} \
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    -e CERT_O="test_org"  \
    -e CERT_OU="test_ou"  \
    -e CERT_CITY="test_city"  \
    -e CERT_STATE="test_state"  \
    -e CERT_COUNTRY="DE"  \
    -e CERT_EMAIL="a@b.com"  \
    -e CERT_CNAMES="server"  \
    -e CERT_PASS="spacewalk"  \
    -e UYUNI_FQDN="server"  \
    -e MANAGER_USER="admin"  \
    -e MANAGER_PASS="spacewalk"  \
    -e MANAGER_ADMIN_EMAIL="a@b.com"  \
    -e MANAGER_MAIL_FROM="a@b.com"  \
    -e MANAGER_ENABLE_TFTP="n"  \
    -e MANAGER_DB_NAME="manager"  \
    -e MANAGER_DB_HOST="localhost"  \
    -e MANAGER_DB_PORT="5432"  \
    -e MANAGER_DB_USER="manager"  \
    -e MANAGER_DB_PASS="manager"  \
    -e MANAGER_DB_PROTOCOL="TCP"  \
    -e REPORT_DB_NAME="reportdb"  \
    -e REPORT_DB_USER="pythia_susemanager"  \
    -e REPORT_DB_PASS="pythia_susemanager"  \
    -e EXTERNALDB_ADMIN_USER=""  \
    -e EXTERNALDB_ADMIN_PASS=""  \
    -e EXTERNALDB_PROVIDER=""  \
    -e ISS_PARENT=""  \
    -e ACTIVATE_SLP=""  \
    -e SCC_USER=""  \
    -e SCC_PASS=""  \
    --cgroupns=host \
    -h server \
    --name=server-setup \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION \
    bash -xc "/testsuite/podman_runner/provide-db-schema.sh && \
             /usr/lib/susemanager/bin/mgr-setup && \
             /usr/bin/spacewalk-schema-upgrade -y && \
             /testsuite/podman_runner/run_db_migrations.sh susemanager-schema && \
             /testsuite/podman_runner/run_db_migrations.sh uyuni-reportdb-schema" 
