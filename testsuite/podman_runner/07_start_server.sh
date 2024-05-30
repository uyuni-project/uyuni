#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

sudo -i podman run --cap-add AUDIT_CONTROL --rm \
    --tmpfs /run \
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
    -v /sys/fs/cgroup:/sys/fs/cgroup:rw \
    -v /tmp/testing:/tmp \
    --cgroupns=host \
    -h server \
    -p 8443:443 \
    -p 8080:80 \
    -p 4505:4505 \
    -p 4506:4506 \
    -d --name=server \
    --network network \
    ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION
sudo -i podman exec -d server prometheus

