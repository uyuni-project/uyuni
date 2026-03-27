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
  echo "SSH minion bootstrap failed, clearing stale salt-ssh state on server and retrying..."
  $PODMAN_CMD exec server bash -c "pkill -f 'mgr-salt-ssh.*opensusessh|salt-ssh.*opensusessh' || true; rm -f /var/cache/salt/master/salt-ssh/session/opensusessh.p || true; chown -R salt:salt /var/cache/salt/master || true"
    # $PODMAN_CMD rm -f opensusessh || true
    # bash "${SCRIPT_DIR}/10_run_sshminion.sh"
  $PODMAN_CMD exec controller bash --login -c "$SSH_INIT_CLIENTS_CMD"
fi

echo "Running remaining init client tests..."
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_others"
