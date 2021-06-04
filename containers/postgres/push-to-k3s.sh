#!/bin/bash

# Prerequisite:
# silvio@k3s:~$ sudo echo "silvio ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/silvio

K3S_HOST=k3s.local
K3S_USER=silvio

docker save opensuse/uyuni/server/postgres:1.0 | ssh $K3S_HOST "sudo k3s ctr images import -"
scp -r ./helm $K3S_HOST://home/$K3S_USER