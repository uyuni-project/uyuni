#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if ! $PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/github_validation/init_clients/sle_ssh_minion.feature"; then
  echo "SSH minion bootstrap failed, restarting opensusessh container and retrying..."
  $PODMAN_CMD rm -f opensusessh || true
  bash "$(dirname "$0")/10_run_sshminion.sh"
fi

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients"
