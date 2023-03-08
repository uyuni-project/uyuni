#!/bin/bash
set -xe
src_dir=$(cd $(dirname "$0")/../.. && pwd -P)

docker run --rm -d --network uyuni-network-1 -v /tmp/test-all-in-one:/tmp -v ${src_dir}/testsuite/docker_runner/salt-minion-entry-point.sh:/salt-minion-entry-point.sh --name rhlike_minion -h rhlike_minion ghcr.io/$UYUNI_PROJECT/uyuni/rocky-minion:$UYUNI_VERSION bash -c "/salt-minion-entry-point.sh uyuni-server-all-in-one-test-1 1-RH-LIKE-KEY"
# sleep 10
docker exec rhlike_minion bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
docker exec rhlike_minion bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
