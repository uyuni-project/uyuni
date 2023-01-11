#! /bin/sh

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

help() {
  echo ""
  echo "Script to run a docker container to verify java code style"
  echo ""
}

while getopts "c:h" opts; do
  case "${opts}" in
    h) help
       exit 0;;
    *) echo "Invalid syntax. Use ${SCRIPT} -h"
       exit 1;;
  esac
done
shift $((OPTIND-1))

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/java/scripts/docker-checkstyle.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

docker pull $REGISTRY/$PGSQL_CONTAINER
docker run --privileged --rm=true -v "$GITROOT:/manager" \
    -v "${HOME}/.obs-to-maven-cache:/manager/java/.obs-to-maven-cache" \
    $REGISTRY/$PGSQL_CONTAINER \
    /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
