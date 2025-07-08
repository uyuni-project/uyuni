#!/bin/sh

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
        h) echo "Usage ${SCRIPT} [-P PRODUCT] [-p]";exit 2;;
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

$EXECUTOR run --interactive --tty --rm -v "$GITROOT:/manager" $REGISTRY/$REPORTDB_DOC_CONTAINER /manager/susemanager-utils/testing/docker/scripts/generate-reportdb-docs.sh "$BRAND_NAME"
