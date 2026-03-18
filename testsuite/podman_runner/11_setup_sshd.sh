#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec server bash -c 'mkdir -p /root/.ssh && chmod 700 /root/.ssh && printf "Host *\n    AddressFamily inet\n    GSSAPIAuthentication no\n    StrictHostKeyChecking no\n    UserKnownHostsFile /dev/null\n" >> /root/.ssh/config'

$PODMAN_CMD exec server bash -c "echo 'UseDNS no' >> /etc/ssh/sshd_config && ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec server bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec controller bash --login -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
$PODMAN_CMD exec buildhost bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
$PODMAN_CMD exec buildhost bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
