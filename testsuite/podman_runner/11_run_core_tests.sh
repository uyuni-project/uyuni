#!/bin/bash
set -xe
export CR=docker
${CR} exec controller-test bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=${CR} && export SERVER=uyuni-server-all-in-one-test && export HOSTNAME=controller-test && cd /testsuite && rake cucumber:container_core"

