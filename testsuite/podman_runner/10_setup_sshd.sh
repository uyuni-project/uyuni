#!/bin/bash
set -xe

podman exec uyuni-server-all-in-one-test bash -c "ssh-keygen -A && /usr/sbin/sshd -e"
podman exec uyuni-server-all-in-one-test bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
podman exec opensusessh bash -c "echo 'root:linux' | chpasswd && echo 123456789 > /etc/machine-id"
podman exec opensusessh bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
podman exec controller-test bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"

