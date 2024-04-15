#!/bin/bash
set -xe
sudo -i podman exec uyuni-controller bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=uyuni-server && export HOSTNAME=uyuni-controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export TAGS=\"\\\"not @flaky\\\"\" && cd /testsuite && rake cucumber:secondary"
