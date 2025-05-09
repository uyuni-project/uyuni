#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sleminion && export RHLIKE_MINION=rhlikeminion && export DEBLIKE_MINION=deblikeminion && export BUILD_HOST=buildhost && cd /testsuite && cucumber features/finishing/allcli_debug.feature"
