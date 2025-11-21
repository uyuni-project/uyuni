#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

if [[ "$GITHUB_TOKEN" ]]; then
  $PODMAN_CMD exec controller bash --login -c "export GITHUB_TOKEN=${GITHUB_TOKEN} && cd /testsuite && rake utils:collect_and_tag_flaky_tests"
fi
