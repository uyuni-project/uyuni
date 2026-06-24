#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

set -e

# This script is used on podman as is doesn't make a difference between liveness and readiness checks.

/usr/bin/liveness-check.sh

# Check that the login page shows up
curl --noproxy localhost --fail --silent --show-error --connect-timeout 10 --max-time 20 \
        --retry 2 --retry-all-errors --retry-delay 1 \
        http://localhost/rhn/manager/login > /dev/null
exit $?
