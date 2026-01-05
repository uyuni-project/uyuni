#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c "zypper ref && zypper -n install expect"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:secondary_parallelizable"
