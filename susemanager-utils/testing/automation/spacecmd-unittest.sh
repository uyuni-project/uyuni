#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

cd $GITROOT/spacecmd
make DOCKER_REGISTRY="${REGISTRY}" DOCKER_IMAGE="${PGSQL_CONTAINER}" -f Makefile.python docker_pytest

exit $?
