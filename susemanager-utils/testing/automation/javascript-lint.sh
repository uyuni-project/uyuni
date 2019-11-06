#!/usr/bin/env bash

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`
echo $HERE
echo $GITROOT

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/web/html/src/scripts/docker-javascript-lint.sh"
CLEAN_CMD="/manager/susemanager-utils/testing/automation/clean-objects.sh"

docker pull $REGISTRY/$NODEJS_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$NODEJS_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CLEAN_CMD} && exit \${RET}"
