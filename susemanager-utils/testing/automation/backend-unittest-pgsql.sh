#! /bin/sh
SCRIPT=$(basename ${0})
EXECUTOR="${EXECUTOR:=docker}"

if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

while getopts 'P:ph' option
do
    case ${option} in
        P) VPRODUCT="VERSION.${OPTARG}" ;;
        p) EXECUTOR="podman" ;;
        h) echo "Usage ${SCRIPT} [-p] [-P PRODUCT]";exit 2;;
    esac
done

HERE=`dirname $0`

if [ ! -f ${HERE}/${VPRODUCT} ];then
   echo "${VPRODUCT} does not exist"
   exit 3
fi

echo "Loading ${VPRODUCT}"
. ${HERE}/${VPRODUCT}
GITROOT=`readlink -f ${HERE}/../../../`

# fake usix.py
if [ ! -e $GITROOT/python/spacewalk/common/usix.py ]; then
    ln -sf ../../usix/common/usix.py $GITROOT/python/spacewalk/common/usix.py
fi

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/python/:/manager/client/rhel/spacewalk-client-tools/src"
EXIT=0

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

$EXECUTOR pull $REGISTRY/$PGSQL_CONTAINER
CMD="/manager/python/test/docker-backend-common-tests.sh"
$EXECUTOR run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=1
fi
CMD="/manager/python/test/docker-backend-tools-tests.sh"
$EXECUTOR run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=2
fi
CMD="/manager/python/test/docker-backend-pgsql-tests.sh"
$EXECUTOR run --privileged --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=3
fi
CMD="/manager/python/test/docker-backend-server-tests.sh"
$EXECUTOR run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=4
fi

rm -f $GITROOT/python/spacewalk/common/usix.py*

exit $EXIT
