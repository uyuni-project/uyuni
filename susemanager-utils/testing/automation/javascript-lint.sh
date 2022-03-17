#!/usr/bin/env bash

if [ -z ${PRODUCT+x} ];then
    PRODUCT="Uyuni"
fi

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
echo $HERE
echo $GITROOT

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/web/html/src/scripts/docker-javascript-lint.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$NODEJS_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" $REGISTRY/$NODEJS_CONTAINER /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
