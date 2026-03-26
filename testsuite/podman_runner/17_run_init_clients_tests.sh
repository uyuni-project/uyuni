#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "Running SSH minion init client test..."
if ! $PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_ssh_minion"; then
  echo "SSH minion bootstrap failed, redeploying container and retrying..."
  $PODMAN_CMD rm -f opensusessh || true
  bash "${SCRIPT_DIR}/10_run_sshminion.sh"
  $PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_ssh_minion"
fi

echo "Running remaining init client tests..."
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients_others"

