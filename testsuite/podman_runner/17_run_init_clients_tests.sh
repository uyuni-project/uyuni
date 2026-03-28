#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SSH_INIT_CLIENTS_CMD="cd /testsuite && rake cucumber:github_validation_init_clients_ssh_minion"

if ! $PODMAN_CMD exec controller bash --login -c "$SSH_INIT_CLIENTS_CMD"; then
  echo "SSH minion bootstrap failed, killing orphan processes on opensusessh and retrying..."
  $PODMAN_CMD exec opensusessh bash -c "echo 'Processes before cleanup:'; ps aux; kill -9 -1 || true"
  sleep 2
  $PODMAN_CMD exec opensusessh bash -c "rm -rf /var/tmp/venv-salt-minion /var/cache/salt /etc/salt /tmp/salt-bundle-*; ssh-keygen -A; /usr/sbin/sshd -e; echo 'Processes after cleanup:'; ps aux"
  $PODMAN_CMD exec controller bash --login -c "$SSH_INIT_CLIENTS_CMD"
fi

echo "Running remaining init client tests..."
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_others"
