#!/bin/bash
set -ex

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c "ssh-keygen -f /root/.ssh/id_rsa -t rsa -N \"\" && cp /root/.ssh/id_rsa.pub /tmp/authorized_keys"

