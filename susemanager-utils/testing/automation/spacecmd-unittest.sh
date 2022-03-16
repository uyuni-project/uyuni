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

cd $GITROOT/spacecmd
make DOCKER_REGISTRY="${REGISTRY}" DOCKER_IMAGE="${PGSQL_CONTAINER}" -f Makefile.python docker_pytest

exit $?
