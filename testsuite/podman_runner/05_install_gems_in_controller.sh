#!/bin/bash
set -xe

if [[ "$(uname)" == "Darwin" ]]; then
  PODMAN_CMD="podman"
else
  PODMAN_CMD="sudo -i podman"
fi

$PODMAN_CMD exec controller bash --login -c 'export GEM_PATH="/usr/lib64/ruby/gems/3.3.0"'
$PODMAN_CMD exec controller bash --login -c "cd /testsuite && bundle.ruby3.3 install --gemfile Gemfile --verbose"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite; bundle.ruby3.3 check || bundle.ruby3.3 install --gemfile Gemfile --verbose"
$PODMAN_CMD exec controller bash --login -c "cd /testsuite; bundle.ruby3.3 check"
