#!/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients"

