#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Exit codes:
# 0 - Ready
# Any thing else: Pending or Failed

# Check if initialization is still in progress
if [ -f "/var/run/uyuni-init-pending" ]; then
    echo "Initialization in progress"
    exit 1
fi

target_status=$(/usr/bin/systemctl is-active multi-user.target 2>/dev/null || true)

case "$target_status" in
    active)
        exit 0
        ;;
    failed)
        echo "multi-user.target entered failed state"
        exit 0
        ;;
    *)
        exit 1
        ;;
esac
