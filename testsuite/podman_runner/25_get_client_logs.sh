#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/finishing/allcli_debug.feature"
