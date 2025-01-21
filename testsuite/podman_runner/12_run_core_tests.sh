#!/bin/bash
set -xe
sudo -i podman exec server bash -c "sed -e 's/http:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/rhn/spacewalk-common-channels.ini"
sudo -i podman exec server bash -c "sed -e 's/https:\/\/download.opensuse.org/file:\/\/\/mirror\/download.opensuse.org/g' -i /etc/rhn/spacewalk-common-channels.ini"
sudo -i podman exec controller bash -c "export SCC_CREDENTIALS=\"test|test\" && export BUILD_HOST=buildhost && export AUTH_REGISTRY=${AUTH_REGISTRY} && export AUTH_REGISTRY_CREDENTIALS=\"${AUTH_REGISTRY_CREDENTIALS}\" &&  export NO_AUTH_REGISTRY=${NO_AUTH_REGISTRY} && export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=podman && export SERVER=server && export HOSTNAME=controller && export SSH_MINION=opensusessh && export MINION=sle_minion && export RHLIKE_MINION=rhlike_minion && export DEBLIKE_MINION=deblike_minion && cd /testsuite && rake cucumber:github_validation_core"
