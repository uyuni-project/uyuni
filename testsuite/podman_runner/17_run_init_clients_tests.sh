#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

for attempt in 1 2; do
  $PODMAN_CMD exec controller bash --login -c "cd /testsuite && cucumber features/github_validation/init_clients/sle_ssh_minion.feature" && break
  [ "$attempt" -eq 2 ] && exit 1
  echo "SSH minion bootstrap failed, retrying..."
  $PODMAN_CMD rm -f opensusessh || true
  bash "$(dirname "$0")/10_run_sshminion.sh"
done

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake cucumber:github_validation_init_clients"
