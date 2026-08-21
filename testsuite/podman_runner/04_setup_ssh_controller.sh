#!/bin/bash
set -ex
source "$(dirname "$0")/helpers.sh"

if [[ "$(uname)" == "Darwin" ]]; then PODMAN_CMD="podman"; fi

# Skip ssh-keygen on retry if the key already exists to avoid the "Overwrite?" interactive prompt.
$PODMAN_CMD exec controller bash -c \
  '[ -f /root/.ssh/id_rsa ] || ssh-keygen -f /root/.ssh/id_rsa -t rsa -N "" && cp /root/.ssh/id_rsa.pub /tmp/authorized_keys'

