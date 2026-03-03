#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

# Use the internal deploy script
$PODMAN_CMD exec server bash /testsuite/podman_runner/internal_deploy_server_code.sh

# Restart services
$PODMAN_CMD exec server bash -c "rctomcat restart"
$PODMAN_CMD exec server bash -c "rctaskomatic restart"
