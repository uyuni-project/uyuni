#!/bin/bash
set -xe
sudo -i podman exec controller-test bash -c "if [ \"${GITHUB_TOKEN}\" == \"\" ];then echo \"empty\";fi:cd /testsuite && echo \"DEBUG IN SHELL\" && ruby ext-tools/collect_and_tag_flaky_tests.rb features && rake utils:collect_and_tag_flaky_tests"
