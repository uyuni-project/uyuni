#! /bin/sh -e

HERE=`dirname $0`
. $HERE/VERSION
GITROOT=`readlink -f $HERE/../../../`

help() {
  echo ""
  echo "Script to run a docker container to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -p  Comma separated list of packages. If absent, all packages are submitted"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo "  -n  If used, update PROJECT instead of the projects specified with -d,"
  echo "      for example, if you want to package only the changes from a PR on"
  echo "      a separate project"
  echo "  -e  If used, when checking out projects from obs, links will be expanded. Useful for comparing packages that are links"
  echo "  -x  Enable parallel builds"
  echo ""
}

PARALLEL_BUILD="FALSE"

while getopts ":d:c:p:n:vthex" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES=${OPTARG};;
    c) CREDENTIALS=${OPTARG};;
    v) VERBOSE="-v";;
    t) TEST="-t";;
    n) OBS_TEST_PROJECT="-n ${OPTARG}";;
    e) EXTRA_OPTS="-e";;
    x) PARALLEL_BUILD="TRUE";;
    h) help
       exit 0;;
    *) echo "Invalid syntax. Use ${SCRIPT} -h"
       exit 1;;
  esac
done
shift $((OPTIND-1))

if [ "${DESTINATIONS}" == "" ]; then
  echo "ERROR: Mandatory parameter -d is missing!"
  exit 1
fi

if [ "${CREDENTIALS}" == "" ]; then
  echo "ERROR: Mandatory paramenter -c is missing!"
  exit 1
fi

if [ ! -f ${CREDENTIALS} ]; then
  echo "ERROR: File ${CREDENTIALS} does not exist!"
  exit 1
fi

docker pull $REGISTRY/$PUSH2OBS_CONTAINER

test -n "$PACKAGES" || {
    PACKAGES=$(ls "$GITROOT"/rel-eng/packages/)
}


echo "Starting building and submission at $(date)"
date
PIDS=""
user=$(id -u)
group=$(id -g)
[ -d ${GITROOT}/logs ] || mkdir ${GITROOT}/logs
for p in ${PACKAGES};do
    # Set pipefail so the result of a pipe is different to zero if one of the commands fail.
    # This way, we can add tee at the end but be sure the result would be non zero if the command before has failed.
    CMD="/manager/susemanager-utils/testing/docker/scripts/push-to-obs.sh -d '${DESTINATIONS}' -c /tmp/.oscrc -p '${p}' ${VERBOSE} ${TEST} ${OBS_TEST_PROJECT} ${EXTRA_OPTS}"
    if [ "$PARALLEL_BUILD" == "TRUE" ];then
        docker run --rm=true -v $GITROOT:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc $REGISTRY/$PUSH2OBS_CONTAINER /bin/bash -c "set -o pipefail;/manager/susemanager-utils/testing/docker/scripts/push-to-obs-wrapper.sh ${p} ${user} ${group} ${CMD} | tee /manager/logs/${p}.log" &
        PIDS="$PIDS $!"
    else
        docker run --rm=true -v $GITROOT:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc $REGISTRY/$PUSH2OBS_CONTAINER /bin/bash -c "set -o pipefail;/manager/susemanager-utils/testing/docker/scripts/push-to-obs-wrapper.sh ${p} ${user} ${group} ${CMD} | tee /manager/logs/${p}.log"
        cat ${GITROOT}/logs/{p}.log
    fi
done
echo "End of task at ($(date). Logs for each package at ${GITROOT}/logs/"

# Turn off exiting on error because we want to wait for all subprocesses
set +e

PRET=0
for i in $PIDS;do
    echo "Waiting for pid $i"
    wait $i
    result=$?
    echo "$i finished, result is $result"
    if [ $result -ne 0 ];then
        echo "Seems there was an error with pid $i"
        PRET=-1
    fi
done

if [ "$PARALLEL_BUILD" == "TRUE" ];then
    for p in ${PACKAGES};do
        cat ${GITROOT}/logs/${p}.log
    done
fi

if [ $PRET -ne 0 ];then
    echo "Review the logs."
    exit $PRET
fi

set -e
