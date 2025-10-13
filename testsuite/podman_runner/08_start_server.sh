#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

setup_pm_path=`sudo -i podman run --rm -ti ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION sh -c 'rpm -ql spacewalk-setup | grep Setup.pm' | tr -d '\r'`

sudo -i podman run --cap-add AUDIT_CONTROL --rm \
    --tmpfs /run \
    -v var-cobbler:/var/lib/cobbler \
    -v var-search:/var/lib/rhn/search \
    -v var-salt:/var/lib/salt \
    -v var-cache:/var/cache \
    -v var-spacewalk:/var/spacewalk \
    -v var-log:/var/log \
    -v srv-salt:/srv/salt \
    -v srv-www:/srv/www/ \
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
    -e TZ=${TZ} \
    --secret uyuni-ca,type=mount,target=/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/usr/share/susemanager/salt/certs/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-ca,type=mount,target=/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT \
    --secret uyuni-cert,type=mount,target=/etc/pki/tls/certs/spacewalk.crt \
    --secret uyuni-key,type=mount,target=/etc/pki/tls/private/spacewalk.key \
    --secret uyuni-db-ca,type=mount,target=/etc/pki/trust/anchors/DB-RHN-ORG-TRUSTED-SSL-CERT \
    -v ${src_dir}/schema/spacewalk/spacewalk-schema-upgrade:/usr/bin/spacewalk-schema-upgrade \
    -v ${src_dir}/testsuite:/testsuite \
    -v ${src_dir}/schema/spacewalk/upgrade/:/usr/share/susemanager/db/schema-upgrade/ \
    -v ${src_dir}/schema/reportdb/upgrade/:/usr/share/susemanager/db/reportdb-schema-upgrade/ \
    -v ${src_dir}/spacewalk/uyuni-setup-reportdb/bin/uyuni-setup-reportdb-user:/usr/bin/uyuni-setup-reportdb-user \
    -v ${src_dir}/web:/web \
    -v ${src_dir}/.npmrc:/.npmrc \
    -v ${src_dir}/package.json:/package.json \
    -v ${src_dir}/package-lock.json:/package-lock.json \
    -v ${src_dir}/branding:/branding \
    -v ${src_dir}/java:/java \
    -v ${src_dir}/client:/client \
    -v ${src_dir}/susemanager-utils:/susemanager-utils \
    -v ${src_dir}/susemanager:/susemanager \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts.conf:/etc/tomcat/conf.d/tomcat_java_opts.conf \
    -v ${src_dir}/spacewalk/setup/share/tomcat_java_opts_suse.conf:/etc/tomcat/conf.d/tomcat_java_opts_suse.conf \
    -v ${src_dir}/java/conf/default/rhn_taskomatic_daemon.conf:/usr/share/rhn/config-defaults/rhn_taskomatic_daemon.conf \
    -v ${src_dir}/python/billingdataservice/billing-data-service.service:/usr/lib/systemd/system/billing-data-service.service \
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    --cgroupns=host \
    -h server \
    -p 8443:443 \
    -p 8080:80 \
    -p 9090:9090 \
    -p 4505:4505 \
    -p 4506:4506 \
    -d --name=server \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION
sudo -i podman exec -d server prometheus

echo "Setting SCC mirror to /mirror"
sudo -i podman exec server bash -c "echo \"server.susemanager.fromdir = /mirror\" >> /etc/rhn/rhn.conf"
sudo -i podman exec server bash -c "rctomcat restart"
echo "Syncing with latest changes"
sudo -i podman exec server bash -c "rsync -av /testsuite/dockerfiles/server-all-in-one-dev/mirror/ /mirror/"

# mgrctl should not be installed in this container
sudo -i podman exec server bash -c "rm -f /usr/bin/mgrctl"

# publish mirrors in apache
sudo -i podman exec server bash -c "cd /srv/www/htdocs/pub && ln -s /mirror . && chown root:root mirror && chown -R root:root /mirror"

