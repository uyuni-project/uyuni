#! /bin/sh

PRODUCT="Uyuni"

help() {
  echo ""
  echo "Script to run a docker container to verify java code style"
  echo "Usage: $0 [-P PROJECT]"
}

while getopts "c:P:h" opts; do
  case "${opts}" in
    P) PRODUCT=$OPTARG ;;
    h) help
       exit 0;;
    *) echo "Invalid syntax. Use ${SCRIPT} -h"
       exit 1;;
  esac
done
shift $((OPTIND-1))

HERE=`dirname $0`

if [ ! -f $HERE/VERSION.${PRODUCT} ];then
   echo "VERSION.${PRODUCT} does not exist"
   exit -3
fi

echo "Loading VERSION.${PRODUCT}"
. $HERE/VERSION.${PRODUCT}
GITROOT=`readlink -f $HERE/../../../`

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/java/scripts/docker-checkstyle.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" \
    $REGISTRY/$PGSQL_CONTAINER \
    /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
