#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD run --privileged -d --name redis -h redis -p 6379:6379 ghcr.io/$UYUNI_PROJECT/uyuni/redis:$UYUNI_VERSION
