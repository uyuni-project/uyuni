#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export TAGS=\"\\\"not @flaky\\\"\" && cd /testsuite && rake cucumber:secondary"

ls -la /tmp
