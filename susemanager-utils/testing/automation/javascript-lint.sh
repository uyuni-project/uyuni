#!/usr/bin/env bash

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`
echo $HERE
echo $GITROOT
docker pull $REGISTRY/$NODEJS_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$NODEJS_CONTAINER /manager/web/html/src/scripts/docker-javascript-lint.sh
