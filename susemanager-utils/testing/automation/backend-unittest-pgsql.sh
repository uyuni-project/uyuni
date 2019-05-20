#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# fake usix.py
if [ ! -e $GITROOT/backend/common/usix.py ]; then
    ln -sf ../../usix/common/usix.py $GITROOT/backend/common/usix.py
fi

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/client/rhel/spacewalk-client-tools/src"
EXIT=0
docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /manager/backend/test/docker-backend-common-tests.sh
if [ $? -ne 0 ]; then
    EXIT=1
fi
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /manager/backend/test/docker-backend-tools-tests.sh
if [ $? -ne 0 ]; then
    EXIT=2
fi
docker run --privileged --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /manager/backend/test/docker-backend-pgsql-tests.sh
if [ $? -ne 0 ]; then
    EXIT=3
fi

rm -f $GITROOT/backend/common/usix.py*

exit $EXIT
