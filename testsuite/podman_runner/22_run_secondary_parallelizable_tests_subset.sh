#!/bin/bash
set -xe
if [ $# -ne 1 ];
then
    echo "Usage: $0 X"
    echo "where X is the set"
    exit 1
fi

sudo -i podman exec controller bash -c "export SCC_CREDENTIALS=\"test|test\" && export BUILD_HOST=buildhost && export AUTH_REGISTRY=${AUTH_REGISTRY} && export AUTH_REGISTRY_CREDENTIALS=\"${AUTH_REGISTRY_CREDENTIALS}\" && export NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY} && export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sleminion && export RHLIKE_MINION=rhlikeminion && export DEBLIKE_MINION=deblikeminion && cd /testsuite && export TAGS=\"\\\"not @flaky\\\"\" && rake cucumber:secondary_parallelizable_${1}"
