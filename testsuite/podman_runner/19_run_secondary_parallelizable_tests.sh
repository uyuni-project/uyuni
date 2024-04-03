#!/bin/bash
set -xe
sudo -i podman exec controller bash -c "zypper ref && zypper -n install expect"
sudo -i podman exec controller bash -c "export BUILD_HOST=buildhost && export AUTH_REGISTRY=${AUTH_REGISTRY} && export AUTH_REGISTRY_CREDENTIALS=\"${AUTH_REGISTRY_CREDENTIALS}\" && export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export TAGS=\"\\\"not @flaky\\\"\" && export DEBLIKE_MINION=deblike_minion && cd /testsuite && rake cucumber:secondary_parallelizable"
