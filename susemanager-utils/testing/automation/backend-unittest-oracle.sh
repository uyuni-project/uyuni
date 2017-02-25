#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# fake usix.py
if [ ! -e $GITROOT/usix/spacewalk/common/usix.py ]; then
    ln -sf . $GITROOT/usix/spacewalk
fi

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/client/rhel/rhn-client-tools/src:/manager/usix"
EXIT=0
docker pull $REGISTRY/$ORACLE_CONTAINER
docker run --privileged --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$ORACLE_CONTAINER /manager/backend/test/docker-backend-oracle-tests.sh
if [ $? -ne 0 ]; then
    EXIT=3
fi

rm $GITROOT/usix/spacewalk

exit $EXIT
