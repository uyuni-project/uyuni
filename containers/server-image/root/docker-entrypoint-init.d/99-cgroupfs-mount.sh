#!/bin/bash
if test "$container" != "oci"; then
    echo "Skipped"
    exit 0
fi

# Prepare the cgroup mount for systemd
mount -t cgroup2 none /sys/fs/cgroup
