#!/bin/bash
set -xe
docker exec controller-test-1 bash -c "export CUCUMBER_PUBLISH_TOKEN=${CUCUMBER_PUBLISH_TOKEN} && export PROVIDER=docker && export SERVER=uyuni-server-all-in-one-test-1 && export HOSTNAME=controller-test-1 && cd /testsuite && rake cucumber:poc_core"

