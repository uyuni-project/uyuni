#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-Only

# This script is common between podman and Kubernetes
# Checking that the HTTP server responds is in the readiness check on Kubernetes.

set -o pipefail
MANAGER_COMPLETE="/var/spacewalk/.MANAGER_SETUP_COMPLETE"

set +e
/usr/bin/spacewalk-diskcheck
DISK_EXIT_CODE=$?
set -e

RESTART_MARKER="/var/run/uyuni-restart-pending"
if [ -f "${RESTART_MARKER}" ]; then
    echo "Restart pending – reporting unhealthy"
    exit 1
fi

if [ $DISK_EXIT_CODE -eq 3 ]; then
    echo "Healthcheck failed: spacewalk-diskcheck returned critical error (3)"
    exit 1
fi

if [ -f "$MANAGER_COMPLETE" ]; then
    /usr/bin/systemctl is-active multi-user.target
    salt-call --local --no-color status.ping_master localhost |grep -q True
else
    echo "Healthcheck failed: setup file not found. Skipping app checks, disk is healthy."
    exit 1
fi
