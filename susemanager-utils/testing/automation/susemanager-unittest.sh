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
        h) echo "Usage ${SCRIPT} [-P PRODUCT]";exit 2;;
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

DOCKER_RUN_EXPORT="PYTHONPATH=/manager/python/:/manager/client/rhel/spacewalk-client-tools/src"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="cd /manager/susemanager; make -f Makefile.susemanager unittest_inside_docker pylint_inside_docker"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

$EXECUTOR pull $REGISTRY/$PGSQL_CONTAINER
echo "$EXECUTOR run --rm=true -e $DOCKER_RUN_EXPORT -v \"$GITROOT:/manager\" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c \"${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}\""
$EXECUTOR run --rm=true -e $DOCKER_RUN_EXPORT -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
exit $?
