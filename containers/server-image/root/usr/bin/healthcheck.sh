#!/bin/bash
set -o pipefail
MANAGER_COMPLETE="/root/.MANAGER_SETUP_COMPLETE"
set +e
/usr/bin/spacewalk-diskcheck
DISK_EXIT_CODE=$?
set -e

if [ $DISK_EXIT_CODE -eq 3 ]; then
    echo "Healthcheck failed: spacewalk-diskcheck returned critical error (3)"
    exit 1
fi

if [ -f "$MANAGER_COMPLETE" ]; then
    set -e
    /usr/bin/systemctl is-active multi-user.target
    salt-call --local --no-color status.ping_master localhost |grep -q True
    curl --noproxy localhost --fail http://localhost/rhn/manager/login
else
    echo "Setup file not found yet. Skipping app checks, disk is healthy."
    exit 0
fi
