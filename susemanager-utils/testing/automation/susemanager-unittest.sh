#! /bin/sh

PRODUCT="Uyuni"

while getopts 'P:h' c
do
    case $c in
        P) PRODUCT=$OPTARG ;;
        h) echo "Usage $0 [-P PRODUCT]";exit -2;;
    esac
done

HERE=`dirname $0`

if [ ! -f $HERE/VERSION.${PRODUCT} ];then
   echo "VERSION.${PRODUCT} does not exist"
   exit -3
fi

echo "Loading VERSION.${PRODUCT}"
. $HERE/VERSION.${PRODUCT}
GITROOT=`readlink -f $HERE/../../../`

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/python/:/manager/client/rhel/spacewalk-client-tools/src"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="cd /manager/susemanager; make -f Makefile.susemanager unittest_inside_docker pylint_inside_docker"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$PGSQL_CONTAINER
echo docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
docker run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
   EXIT=1
fi

exit $EXIT
