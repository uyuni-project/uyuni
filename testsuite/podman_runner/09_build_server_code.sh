#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec server bash -c "rctomcat stop"
$PODMAN_CMD exec server bash -c "rctaskomatic stop"

# Use the internal deploy script
$PODMAN_CMD exec server bash /testsuite/podman_runner/internal_deploy_server_code.sh

$PODMAN_CMD exec server bash -c "rctomcat restart"
$PODMAN_CMD exec server bash -c "rctaskomatic restart"
