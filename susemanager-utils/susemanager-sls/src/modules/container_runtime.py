# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
Container Runtime Detection Module for SaltStack

This module provides functionality to detect the container runtime (e.g., Docker, Podman, Kubernetes)
running on the target system. It works by analyzing system files, environment variables, and
specific indicators to identify the runtime.

Usage:
    - Run the module using the Salt CLI:
        salt '*' container_runtime.get_container_runtime

    - Example return values:
        - 'docker' for Docker
        - 'podman' for Podman
        - None if no container runtime is detected

References:
    - Most of the code was ported from
    https://github.com/SUSE/connect-ng/blob/main/internal/collectors/container_runtime.go
"""

import os
import re

try:
    from salt.utils import fopen
except ImportError:
    from salt.utils.files import fopen

RUNTIME_DOCKER = "docker"
RUNTIME_RKT = "rkt"
RUNTIME_NSPAWN = "systemd-nspawn"
RUNTIME_LXC = "lxc"
RUNTIME_LXC_LIBVIRT = "lxc-libvirt"
RUNTIME_OPENVZ = "openvz"
RUNTIME_KUBERNETES = "kube"
RUNTIME_GARDEN = "garden"
RUNTIME_PODMAN = "podman"
RUNTIME_GVISOR = "gvisor"
RUNTIME_FIREJAIL = "firejail"
RUNTIME_NOT_FOUND = "not-found"

CONTAINER_RUNTIMES = [
    RUNTIME_DOCKER,
    RUNTIME_RKT,
    RUNTIME_NSPAWN,
    RUNTIME_LXC,
    RUNTIME_LXC_LIBVIRT,
    RUNTIME_OPENVZ,
    RUNTIME_KUBERNETES,
    RUNTIME_GARDEN,
    RUNTIME_PODMAN,
    RUNTIME_GVISOR,
    RUNTIME_FIREJAIL,
]


def _detect_container_files():
    """
    Detects specific files that indicate the presence of certain container runtimes.
    """
    files = [
        (RUNTIME_PODMAN, "/run/.containerenv"),
        (RUNTIME_DOCKER, "/.dockerenv"),
        (RUNTIME_KUBERNETES, "/var/run/secrets/kubernetes.io/serviceaccount"),
    ]

    for runtime, location in files:
        if os.path.exists(location):
            return runtime

    return RUNTIME_NOT_FOUND


def _get_container_runtime(input_string):
    """
    Determines the container runtime from the input string.
    """
    if not input_string or not input_string.strip():
        return RUNTIME_NOT_FOUND

    for runtime in CONTAINER_RUNTIMES:
        if runtime in input_string:
            return runtime

    return RUNTIME_NOT_FOUND


def _read_file(file_path):
    """
    Reads the contents of a file safely.
    """
    try:
        with fopen(file_path, "r") as f:
            return f.read().strip()
    # pylint: disable-next=broad-exception-caught
    except Exception:
        return ""


def get_container_runtime():
    """
    Returns the container runtime the process is running in.
    """
    cgroups = _read_file("/proc/self/cgroup")
    runtime = _get_container_runtime(cgroups)
    if runtime != RUNTIME_NOT_FOUND:
        return runtime

    if os.path.exists("/proc/vz") and not os.path.exists("/proc/bc"):
        return RUNTIME_OPENVZ

    if os.path.exists("/__runsc_containers__"):
        return RUNTIME_GVISOR

    cmdline = _read_file("/proc/1/cmdline")
    runtime = _get_container_runtime(cmdline)
    if runtime != RUNTIME_NOT_FOUND:
        return runtime

    container_env = os.getenv("container")
    runtime = _get_container_runtime(container_env)
    if runtime != RUNTIME_NOT_FOUND:
        return runtime

    systemd_container = _read_file("/run/systemd/container")
    runtime = _get_container_runtime(systemd_container)
    if runtime != RUNTIME_NOT_FOUND:
        return runtime

    runtime = _detect_container_files()
    if runtime != RUNTIME_NOT_FOUND:
        return runtime

    # Docker was not detected at this point.
    # An overlay mount on "/" may indicate we're under containerd or other runtime.
    mounts = _read_file("/proc/mounts")
    if re.match("^[^ ]+ / overlay", mounts):
        return RUNTIME_KUBERNETES

    return None
