#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# SSH_INIT_CLIENTS_CMD="cd /testsuite && rake cucumber:github_validation_init_clients_ssh_minion"

if ! $PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/github_validation/init_clients/sle_ssh_minion.feature"; then
  echo "SSH minion bootstrap failed, restarting opensusessh container and retrying..."
  $PODMAN_CMD rm -f opensusessh || true
  bash "${SCRIPT_DIR}/10_run_sshminion.sh"
  $PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/github_validation/init_clients/sle_ssh_minion.feature"
fi

echo "Running remaining init client tests..."
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/github_validation/init_clients/sle_minion.feature && cucumber features/github_validation/init_clients/min_rhlike_salt.feature && cucumber features/github_validation/init_clients/min_deblike_salt.feature && cucumber features/github_validation/init_clients/buildhost_bootstrap.feature"
