#! /bin/sh
SCRIPT=$(basename ${0})
EXECUTOR="${EXECUTOR:=docker}"

if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

help() {
  echo ""
  echo "Script to run a container to verify helm charts"
  echo "Usage: ${SCRIPT} [-P PROJECT] [-p]"
  echo "       -p use podman instead of docker"
  echo "       -P PROJECT use the special PROJECT configuration"
}

while getopts "c:P:ph" opts; do
  case "${opts}" in
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
CMD="/manager/susemanager-utils/testing/docker/scripts/helm-charts-tests.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"

$EXECUTOR pull $REGISTRY/$HELM_CONTAINER
$EXECUTOR run --rm=true -v "$GITROOT:/manager" \
    $REGISTRY/$HELM_CONTAINER \
    /bin/bash -c "${INITIAL_CMD}; ${CMD}; RET=\${?}; ${CHOWN_CMD} && exit \${RET}"

