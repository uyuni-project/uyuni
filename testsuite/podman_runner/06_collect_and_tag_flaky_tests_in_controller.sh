#!/bin/bash
set -e

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [[ -n "$GITHUB_TOKEN" ]]; then
  echo "Running flaky tests collection..."
  set +x
  $PODMAN_CMD exec -e GITHUB_TOKEN="$GITHUB_TOKEN" controller bash --login -c "cd /testsuite && rake utils:collect_and_tag_flaky_tests"
fi
