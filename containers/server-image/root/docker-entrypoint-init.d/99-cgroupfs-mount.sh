#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ "${container:=unknown}" != "oci" ]; then
    exit 0
fi

# Prepare the cgroup mount for systemd.
# Skip it when the runtime already provides one (e.g. Kubernetes with
# private cgroup namespaces): mounting cgroup2 on top of an existing
# mount fails with EBUSY, which would abort the whole container startup
# even though systemd can use the existing mount just fine.
if ! mountpoint -q /sys/fs/cgroup; then
    mount -t cgroup2 none /sys/fs/cgroup
fi
