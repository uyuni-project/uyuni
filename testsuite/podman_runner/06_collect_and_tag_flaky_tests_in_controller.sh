#!/bin/bash
set -xe
sudo -i podman exec controller-test bash -c "if [ \"${GITHUB_TOKEN}\" == \"\" ];then echo \"empty gh token\";fi"
sudo -i podman exec controller-test bash -c "export GITHUB_TOKEN=${GITHUB_TOKEN} && cd /testsuite && ruby ext-tools/collect_and_tag_flaky_tests.rb features && rake utils:collect_and_tag_flaky_tests"
