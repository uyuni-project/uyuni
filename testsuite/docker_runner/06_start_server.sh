#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

docker run --rm --tmpfs /run -v ${src_dir}/schema/spacewalk/upgrade/:/etc/sysconfig/rhn/schema-upgrade/ -v ${src_dir}/schema/reportdb/upgrade/:/etc/sysconfig/rhn/reportdb-schema-upgrade/ -v ${src_dir}/web:/web -v ${src_dir}/branding:/branding -v ${src_dir}/java:/java -v /sys/fs/cgroup:/sys/fs/cgroup:rw -v /tmp/test-all-in-one:/tmp --cgroupns=host --add-host=download.opensuse.org:195.135.221.134 -h uyuni-server-all-in-one-test-1 -p 8443:443 -p 8080:80 -p 4505:4505 -p 4506:4506 -d --name=uyuni-server-all-in-one-test-1 --network uyuni-network-1 ghcr.io/$UYUNI_PROJECT/uyuni/ci-test-server-all-in-one-dev:$UYUNI_VERSION
# sleep 10

