#!/bin/bash
# Print what the container runtime looks like before any container is started.
# A hang in a later "podman exec" cannot be explained without knowing which
# storage driver and mount program podman picked on this runner, so record it
# up front, in every job, whether or not the job goes on to fail.
# Nothing here may fail the job.

echo "===== runner ====="
uname -a
echo "ImageOS=${ImageOS} ImageVersion=${ImageVersion}"
echo

# Through "sudo -i", like the test scripts, and with the resolved path: root's
# login PATH is exactly what decides which podman the tests get, and reporting
# a different one here would hide the very discrepancy this file exists for.
echo "===== versions ====="
for binary in podman conmon crun runc; do
    echo "--- ${binary}: $(sudo -i command -v "${binary}" 2>/dev/null || echo 'not on root PATH') ---"
    sudo -i "${binary}" --version 2>/dev/null | head -2
done
echo

echo "===== fuse-overlayfs shipped by the image ====="
for path in /usr/local/bin/fuse-overlayfs /usr/bin/fuse-overlayfs; do
    if [ -e "${path}" ]; then
        echo "${path}: $("${path}" --version 2>&1 | head -1)"
    fi
done
echo

echo "===== storage configuration ====="
for conf in /etc/containers/storage.conf /root/.config/containers/storage.conf; do
    if sudo test -e "${conf}"; then
        echo "--- ${conf} ---"
        sudo grep -vE '^\s*(#|$)' "${conf}"
    fi
done
echo

echo "===== podman storage ====="
sudo -i podman info --format 'driver={{ .Store.GraphDriverName }} options={{ .Store.GraphOptions }} root={{ .Store.GraphRoot }}'
echo

exit 0
