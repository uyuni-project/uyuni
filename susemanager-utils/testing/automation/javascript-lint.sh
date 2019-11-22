#!/usr/bin/env bash

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`
echo $HERE
echo $GITROOT

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/web/html/src/scripts/docker-javascript-lint.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)""

docker pull $REGISTRY/$NODEJS_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$NODEJS_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
