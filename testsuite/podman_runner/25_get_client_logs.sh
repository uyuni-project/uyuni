#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "export PUBLISH_CUCUMBER_REPORT=${PUBLISH_CUCUMBER_REPORT} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export DEBLIKE_MINION=deblike_minion && export BUILD_HOST=buildhost && cd /testsuite && cucumber features/finishing/allcli_debug.feature"
