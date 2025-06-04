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
