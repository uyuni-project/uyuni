#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

# Exit codes:
# 0 - Ready
# Any thing else: Pending or Failed

# Check if initialization is still in progress
if [ -f "/var/run/uyuni-init-pending" ]; then
    echo "Initialization in progress"
    exit 1
fi

# If we reach here, container is ready
exit 0
