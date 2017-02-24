#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

docker pull $REGISTRY/$ORACLE_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$ORACLE_CONTAINER /manager/java/scripts/docker-testing-oracle.sh
