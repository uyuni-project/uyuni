#! /bin/sh
SCRIPT=$(basename ${0})

TARGET="test-pr"
EXECUTOR="docker"

if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

help() {
  echo ""
  echo "Script to run a docker container to run the pgsql Java unit tests"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} [-t ant-target] [-P PROJECT] [-p]"
  echo ""
  echo "Where: "
  echo "  -t  Ant target to run. Default: ${TARGET}"
  echo "  -p  If given use podman instead of docker"
  echo ""
}

while getopts "c:t:P:ph" opts; do
  case "${opts}" in
    t) TARGET=${OPTARG};;
    P) VPRODUCT="VERSION.${OPTARG}" ;;
    p) EXECUTOR="podman" ;;
    h) help
       exit 0;;
    *) echo "Invalid syntax. Use ${SCRIPT} -h"
       exit 1;;
  esac
done
shift $((OPTIND-1))
HERE=`dirname $0`

if [ ! -f ${HERE}/${VPRODUCT} ];then
   echo "${VPRODUCT} does not exist"
   exit 3
fi

echo "Loading ${VPRODUCT}"
. ${HERE}/${VPRODUCT}

GITROOT=`readlink -f ${HERE}/../../../`

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CMD="/manager/java/scripts/docker-testing-pgsql.sh ${TARGET}"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

$EXECUTOR pull $REGISTRY/$PGSQL_CONTAINER
$EXECUTOR run --privileged --rm=true -v "$GITROOT:/manager" \
    -v "${HOME}/.obs-to-maven-cache:/manager/java/.obs-to-maven-cache" \
    -v "${HOME}/.obs-to-maven-cache/repository:/manager/java/buildconf/ivy/repository" \
    $REGISTRY/$PGSQL_CONTAINER \
    /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"
