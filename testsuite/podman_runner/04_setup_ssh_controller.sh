#!/bin/bash
set -ex
sudo -i podman exec controller-test bash -c "ssh-keygen -f /root/.ssh/id_rsa -t rsa -N \"\" && cp /root/.ssh/id_rsa.pub /tmp/authorized_keys"

