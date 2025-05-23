#!/bin/bash
set -xe

sudo -i podman exec controller bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server.test.lan && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sleminion && export RHLIKE_MINION=rhlikeminion && cd /testsuite && export TAGS=\"\\\"not @flaky\\\"\" && cat run_sets/recommended_tests.yml &&  rake cucumber:recommended_tests"
