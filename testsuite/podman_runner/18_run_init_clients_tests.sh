#!/bin/bash
set -xe
sudo -i podman exec controller-test bash -c 'export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=uyuni-server-all-in-one-test && export PROXY=uyuni-proxy-test && export HOSTNAME=controller-test && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export RUNNER=$(hostname -I | cut -d " " -f1) && cd /testsuite && rake cucumber:github_validation_init_clients'
