#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c "cd /testsuite && rake utils:filter_secondary[/testsuite/run_sets/filter.yml,/testsuite/run_sets/secondary.yml,/testsuite/run_sets/secondary_parallelizable.yml,/testsuite/run_sets/recommended_tests.yml] "

