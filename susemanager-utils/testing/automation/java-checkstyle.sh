#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

CREDENTIALS="${HOME}/.oscrc"

help() {
  echo ""
  echo "Script to run a docker container to verify java code style"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -c /path/to/oscrc"
  echo ""
  echo "Where: "
  echo "  -c  Path to the OSC credentials. Default: ${CREDENTIALS}"
  echo ""
}

while getopts "c:h" opts; do
  case "${opts}" in
    c) CREDENTIALS=${OPTARG};;
    h) help
       exit 0;;
    *) echo "Invalid syntax. Use ${SCRIPT} -h"
       exit 1;;
  esac
done
shift $((OPTIND-1))

if [ "${CREDENTIALS}" == "" ]; then
  echo "ERROR: Mandatory paramenter -c is missing!"
  exit 1
fi

if [ ! -f ${CREDENTIALS} ]; then
  echo "ERROR: File ${CREDENTIALS} does not exist!"
  exit 1
fi

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" \
    --mount type=bind,source=${CREDENTIALS},target=/root/.oscrc \
    $REGISTRY/$PGSQL_CONTAINER \
    /manager/java/scripts/docker-checkstyle.sh
