#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [ $# -ne 1 ];
then
    echo "Usage: $0 X"
    echo "where X is the set"
    exit 1
fi

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && export TAGS=\"\\\"not @flaky\\\"\" && rake cucumber:secondary_parallelizable_${1}"
