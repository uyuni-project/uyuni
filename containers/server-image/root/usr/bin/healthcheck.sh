#!/bin/bash
set -o pipefail
MANAGER_COMPLETE="/root/.MANAGER_SETUP_COMPLETE"

check_manager_login() {
    local attempts=2
    local attempt=1

    while [ "$attempt" -le "$attempts" ]; do
        if curl --noproxy localhost --fail --silent --show-error --connect-timeout 1 --max-time 1 \
            http://localhost/rhn/manager/login > /dev/null; then
            return 0
        fi

        sleep 0.5
        attempt=$((attempt + 1))
    done

    return 1
}

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
    if ! check_manager_login; then
        echo "Healthcheck failed: manager login endpoint is not reachable after retries."
        exit 1
    fi
else
    echo "Setup file not found yet. Skipping app checks, disk is healthy."
    exit 0
fi
