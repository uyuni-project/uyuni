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

cd $GITROOT/spacecmd
make DOCKER_REGISTRY="${REGISTRY}" DOCKER_IMAGE="${PGSQL_CONTAINER}" -f Makefile.python docker_pytest

exit $?
