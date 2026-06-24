#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ "${container:=unknown}" != "oci" ]; then
    exit 0
fi

# Prepare the cgroup mount for systemd
mount -t cgroup2 none /sys/fs/cgroup
