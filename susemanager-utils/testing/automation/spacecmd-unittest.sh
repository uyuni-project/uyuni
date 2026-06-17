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
        h) echo "Usage ${SCRIPT} [-P PRODUCT] [p]";exit 2;;
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

DOCKER_RUN_EXPORT="PYTHONPATH=$PYTHONPATH"
DOCKER_RUN_VOLUMES="-v $GITROOT:/manager"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"


cd $GITROOT/spacecmd
$EXECUTOR pull ${REGISTRY}/${PGSQL_CONTAINER}

CMD="pushd /manager/spacecmd; make -f Makefile.python __pytest"

echo
echo "##################### RUNNING TESTS ON MAIN PYTHON VERSION ###########################"
$EXECUTOR run --rm -e $DOCKER_RUN_EXPORT $DOCKER_RUN_VOLUMES ${REGISTRY}/${PGSQL_CONTAINER} \
	/bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; popd; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=1
fi

if [ "${VPRODUCT}" = "VERSION.Uyuni" ]; then
echo
echo "##################### RUNNING TESTS ON PYTHON 3.6 ###########################"
$EXECUTOR run --rm -e $DOCKER_RUN_EXPORT $DOCKER_RUN_VOLUMES ${REGISTRY}/${PGSQL_CONTAINER_PYTHON36} \
	/bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; popd; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
    EXIT=2
fi
fi

exit $EXIT
