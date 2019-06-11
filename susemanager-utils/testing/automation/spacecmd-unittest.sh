#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

cd $GITROOT/spacecmd
make -f Makefile.python docker_pytest

exit $?
