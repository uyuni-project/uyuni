#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

# fake usix.py
if [ ! -e $GITROOT/backend/common/usix.py ]; then
    ln -sf ../../usix/common/usix.py $GITROOT/backend/common/usix.py
fi

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/client/rhel/rhnlib/:/manager/client/rhel/rhn-client-tools/src"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="cd /manager/susemanager; make -f Makefile.susemanager unittest_inside_docker pylint_inside_docker"
CLEAN_CMD="/manager/susemanager-utils/testing/automation/clean-objects.sh"

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CLEAN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
   EXIT=1
fi

rm -f $GITROOT/backend/common/usix.py*

exit $EXIT
