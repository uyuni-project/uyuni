#!/bin/bash
set -xe

sudo -i podman exec controller bash -c "export PUBLISH_CUCUMBER_REPORT=${PUBLISH_CUCUMBER_REPORT} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && cd /testsuite && export TAGS=\"\\\"not @flaky\\\"\" && cat run_sets/recommended_tests.yml &&  rake cucumber:recommended_tests"
