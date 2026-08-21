#!/bin/bash
# Shared setup for podman_runner scripts.
# Source this file before setting PODMAN_CMD.
# On Linux, PODMAN_CMD is set to the podman_cmd shim which probes for
# cgroup_mutex availability before non-detached exec calls.
# See docs/adr/0001-ci-queue-jam-podman-exec-hang.md

if [[ "$(uname)" != "Darwin" ]]; then
    PODMAN_CMD="$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")/podman_cmd"
fi
