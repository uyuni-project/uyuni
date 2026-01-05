#!/bin/bash
set -ex
if [ ! -d /tmp/testing ];then
  if [[ "$(uname)" == "Darwin" ]]; then
    mkdir -p /tmp/testing
    podman machine init --memory 20480 --volume "$HOME" --volume /tmp/testing
    podman machine start
  else
    mkdir /tmp/testing
  fi
fi
