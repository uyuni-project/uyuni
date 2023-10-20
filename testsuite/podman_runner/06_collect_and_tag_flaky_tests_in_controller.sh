#!/bin/bash
set -xe
sudo -i podman exec controller-test bash -c "export GITHUB_TOKEN=${GITHUB_TOKEN} && cd /testsuite && rake utils:collect_and_tag_flaky_tests"
