#! /bin/sh

TARGET="test-pr"

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

CREDENTIALS="${HOME}/.oscrc"

help() {
  echo ""
  echo "Script to run a docker container to run the pgsql Java unit tests"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -c /path/to/oscrc [-t ant-target]"
  echo ""
  echo "Where: "
  echo "  -c  Path to the OSC credentials. Default: ${CREDENTIALS}"
  echo "  -t  Ant target to run. Default: ${TARGET}"
  echo ""
}

while getopts "c:t:h" opts; do
  case "${opts}" in
    c) CREDENTIALS=${OPTARG};;
    t) TARGET=${OPTARG};;
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

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/java/scripts/docker-testing-pgsql.sh ${TARGET}"
CLEAN_CMD="/manager/susemanager-utils/testing/automation/clean-objects.sh"

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" \
    --mount type=bind,source=${CREDENTIALS},target=/root/.oscrc \
    $REGISTRY/$PGSQL_CONTAINER \
    /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CLEAN_CMD} && exit \${RET}"
