#! /bin/sh
SCRIPT=$(basename ${0})

if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

while getopts 'P:h' option
do
    case ${option} in
        P) VPRODUCT="VERSION.${OPTARG}" ;;
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

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="cd /manager/susemanager-utils/susemanager-sls/; make -f Makefile.python junit_pytest"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$PGSQL_CONTAINER
echo docker run --rm=true -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
docker run --rm=true -v "$GITROOT:/manager" $REGISTRY/$PGSQL_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
if [ $? -ne 0 ]; then
   EXIT=1
fi

exit $EXIT
