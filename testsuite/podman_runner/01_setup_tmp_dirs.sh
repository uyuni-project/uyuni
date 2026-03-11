#!/bin/bash
set -ex
if [ ! -d /tmp/testing ];then
  mkdir -p /tmp/testing && chmod 755 /tmp/testing
  if [[ "$(uname)" == "Darwin" ]]; then
    podman machine init --memory 20480 --volume "$HOME" --volume /tmp/testing
    podman machine start
  fi
fi
