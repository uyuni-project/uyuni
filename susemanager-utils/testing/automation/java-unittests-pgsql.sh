#! /bin/sh

TARGET="test-pr"
if [ -n "$1" ]; then
    TARGET=$1
fi

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /manager/java/scripts/docker-testing-pgsql.sh $TARGET
