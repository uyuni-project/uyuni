#!/bin/bash
set -xe

sudo -i podman exec uyuni-server-all-in-one-test bash -c "ssh-keygen -A && /usr/sbin/sshd -e && eval \$(ssh-agent) && ssh-add /etc/ssh/ssh_host_rsa_key && cp /etc/ssh/ssh_host_rsa_key.pub /tmp"
sudo -i podman exec uyuni-server-all-in-one-test bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"
sudo -i podman exec opensusessh bash -c "echo 'root:linux' | chpasswd && echo 123456789 > /etc/machine-id"
sudo -i podman exec opensusessh bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/"

if [ ! -d $HOME/.ssh ]; then
    mkdir $HOME/.ssh/
    chmod 700 $HOME/.ssh
fi

cp /tmp/test-all-in-one/authorized_keys $HOME/.ssh/
cat <<EOF > /tmp/test-all-in-one/config
Host $(hostname -I | cut -d " " -f1)
    User $USER
    StrictHostKeyChecking no
EOF

sudo -i podman exec controller-test bash -c "if [ ! -d /root/.ssh ];then mkdir /root/.ssh/;chmod 700 /root/.ssh;fi;cp /tmp/authorized_keys /root/.ssh/;cp /tmp/config /root/.ssh/"
