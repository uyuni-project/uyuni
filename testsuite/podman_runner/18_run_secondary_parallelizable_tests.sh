#!/bin/bash
set -xe
export CR=docker
${CR} exec controller-test bash -c "zypper ref && zypper -n install expect"
${CR} exec controller-test bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=${CR} && export SERVER=uyuni-server-all-in-one-test && export HOSTNAME=controller-test && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export DEBLIKE_MINION=deblike_minion && cd /testsuite && rake cucumber:secondary_parallelizable"
