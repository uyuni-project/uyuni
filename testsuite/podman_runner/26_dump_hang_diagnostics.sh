#!/bin/bash
# Dump the state of the container runtime after a step failed or timed out.
# The failures this is meant to explain are podman calls that never return, so
# every command here has a timeout and nothing is allowed to fail the job.
# Host state comes first: a wedged podman can sit in the kernel where even
# "timeout" cannot reach it, and that state is exactly what explains the hang.

run() {
    echo "===== $* ====="
    timeout -k 5 30 "$@" 2>&1 | head -100
    echo
}

# Command lines are what tells one "podman exec" from another, but they also
# carry the flaky test token and the registry password, so redact them first.
redact() {
    sed -E -e 's/(TOKEN|PASSWD|PASSWORD|SECRET|CREDENTIALS)=[^[:space:]]*/\1=<redacted>/g' \
           -e 's/(login[[:space:]].*)-p[[:space:]]+[^[:space:]]+/\1-p <redacted>/g'
}

echo "########## container runtime diagnostics ##########"

run uname -a

echo "===== overlay and fuse mounts ====="
grep -E 'overlay|fuse' /proc/self/mounts | head -40
echo

echo "===== processes in uninterruptible sleep ====="
ps -eo pid,stat,wchan:32,etimes,args | awk 'NR == 1 || $2 ~ /D/' | redact | head -40
echo

# Whatever their state: the wedge seen so far is a podman blocked on a futex,
# which is an ordinary interruptible sleep and never shows up in the list above.
echo "===== kernel stacks of the runtime processes ====="
for pid in $(pgrep -f 'podman|conmon|crun|runc|fuse-overlayfs' | head -40); do
    [ -r "/proc/${pid}/status" ] || continue
    state=$(awk '/^State:/ { print $2 }' "/proc/${pid}/status" 2>/dev/null)
    cmdline=$(tr '\0' ' ' < "/proc/${pid}/cmdline" 2>/dev/null | redact | cut -c1-200)
    echo "--- pid ${pid} state=${state} wchan=$(cat "/proc/${pid}/wchan" 2>/dev/null) :: ${cmdline} ---"
    sudo cat "/proc/${pid}/stack" 2>/dev/null | head -20
done
echo

echo "===== blocked tasks ====="
echo w | sudo tee /proc/sysrq-trigger > /dev/null 2>&1
sudo dmesg -T 2>&1 | tail -100
echo

# Podman last, and through "sudo -i" like the test scripts, so this reports the
# same binary they run. Any of these can be the call that is already wedged.
run sudo -i podman version
run sudo -i podman ps -a
run sudo -i podman info

echo "===== container logs ====="
for container in controller server; do
    echo "--- ${container} ---"
    timeout -k 5 30 sudo -i podman logs --tail 50 "${container}" 2>&1 | head -60
done
echo

exit 0
