#!/bin/bash
set -xe
docker exec controller-test-1 bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=docker && export SERVER=uyuni-server-all-in-one-test-1 && export HOSTNAME=controller-test-1 && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export DEBLIKE_MINION=deblike_minion && cd /testsuite && rake cucumber:poc_init_clients"
