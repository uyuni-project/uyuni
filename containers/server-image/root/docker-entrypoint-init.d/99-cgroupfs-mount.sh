#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ "${container:=unknown}" != "oci" ]; then
    exit 0
fi

# Prepare the cgroup mount for systemd.
# Only mount when /sys/fs/cgroup is not already a cgroup2 filesystem.
#
# We must check the filesystem type, not just whether it is a mountpoint:
#   * On Kubernetes the chart mounts an emptyDir at /sys/fs/cgroup to avoid
#     touching (and potentially corrupting) the host's cgroupfs. That
#     emptyDir IS a mountpoint, but it is not cgroup2, so systemd needs us
#     to mount cgroup2 into it.
#   * Some runtimes pre-mount cgroup2 there (e.g. private cgroup
#     namespaces). Mounting again would fail with EBUSY and abort startup,
#     so in that case we must skip.
# A plain "mountpoint -q" check cannot tell these two apart.
if [ "$(stat -f -c %T /sys/fs/cgroup 2>/dev/null)" != "cgroup2fs" ]; then
    mount -t cgroup2 none /sys/fs/cgroup
fi
