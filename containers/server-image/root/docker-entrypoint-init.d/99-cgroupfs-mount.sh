#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ "${container:=unknown}" != "oci" ]; then
    exit 0
fi

# Prepare the cgroup mount for systemd.
# Skip it when cgroup2 is already mounted there (e.g. Kubernetes with
# private cgroup namespaces): mounting cgroup2 on top of an existing
# cgroup2 mount fails with EBUSY, which would abort the whole container
# startup even though systemd can use the existing mount just fine.
# Test the filesystem type rather than mountpoint-ness: the Helm chart
# mounts an emptyDir at /sys/fs/cgroup, which is a mountpoint but not a
# cgroup filesystem, and systemd cannot start without cgroup2 there.
if [ "$(stat -f -c %T /sys/fs/cgroup)" != "cgroup2fs" ]; then
    mount -t cgroup2 none /sys/fs/cgroup
fi
