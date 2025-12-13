#!/bin/bash

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

set -xe
TESTS="$@"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:secondary ${TESTS}"
