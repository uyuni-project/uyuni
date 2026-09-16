#!/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

set -xe
TESTS="$@"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:secondary ${TESTS}"
