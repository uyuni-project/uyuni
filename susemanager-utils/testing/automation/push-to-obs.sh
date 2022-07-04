#! /bin/sh -e
# Set pipefail so the result of a pipe is different to zero if one of the commands fail.
# This way, we can add tee but be sure the result would be non zero if the command before has failed.
set -o pipefail


SCRIPT=$(basename ${0})
help() {
  echo ""
  echo "Script to run a docker container to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} [-P PRODUCT] -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE -s SSH_PRIVATE_KEY [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -s  Path to the private key used for MFA"
  echo "  -p  Comma separated list of packages. If absent, all packages are submitted"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo "  -n  If used, update PROJECT instead of the projects specified with -d,"
  echo "      for example, if you want to package only the changes from a PR on"
  echo "      a separate project"
  echo "  -e  If used, when checking out projects from obs, links will be expanded. Useful for comparing packages that are links"
  echo "  -x  Enable parallel builds"
  echo "  -P  Is the product name, for example Uyuni or SUSE-Manager. This is to load VERSION.Uyuni or VERSION.SUSE-Manager. By default is Uyuni."
  echo ""
}

PARALLEL_BUILD="FALSE"
if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

while getopts ":d:c:s:p:P:n:vthex" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES="$(echo ${OPTARG}|tr ',' ' ')";;
    P) VPRODUCT="VERSION.${OPTARG}" ;;
    c) CREDENTIALS=${OPTARG};;
    s) SSHKEY=${OPTARG};;
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

HERE=`dirname $0`

if [ ! -f ${HERE}/${VPRODUCT} ];then
   echo "${VPRODUCT} does not exist"
   exit 3
fi

echo "Loading ${VPRODUCT}"
. ${HERE}/${VPRODUCT}
GITROOT=`readlink -f ${HERE}/../../../`

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

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"
docker pull $REGISTRY/$PUSH2OBS_CONTAINER

test -n "$PACKAGES" || {
    PACKAGES=$(ls "$GITROOT"/rel-eng/packages/)
    IMAGES=$(cd "$GITROOT"/containers; ls -d *-image)
}
echo "Starting building and submission at $(date)"
date
PIDS=""
[ -d ${GITROOT}/logs ] || mkdir ${GITROOT}/logs
for p in ${PACKAGES};do
    pkg_dir=$(cat rel-eng/packages/${p} | tr -s " " | cut -d" " -f 2)
    CHOWN_CMD="${CHOWN_CMD}; chown -f -R $(id -u):$(id -g) /manager/$pkg_dir"
    CMD="/manager/susemanager-utils/testing/docker/scripts/push-to-obs.sh -d '${DESTINATIONS}' -c /tmp/.oscrc -s /root/.ssh/id_rsa -p '${p}' ${VERBOSE} ${TEST} ${OBS_TEST_PROJECT} ${EXTRA_OPTS}"
    if [ "$PARALLEL_BUILD" == "TRUE" ];then
        echo "Building ${p} in parallel"
        docker run --rm=true -v ${GITROOT}:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc --mount type=bind,source=${SSHKEY},target=/root/.ssh/id_rsa ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?};${CHOWN_CMD} && exit \${RET}" 2>&1 > ${GITROOT}/logs/${p}.log &
        pid=${!}
        PIDS="${PIDS} ${pid}"
        ln -s ${GITROOT}/logs/${p}.log ${GITROOT}/logs/${pid}.log
    else
        echo "Building ${p}"
        docker run --rm=true -v ${GITROOT}:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc --mount type=bind,source=${SSHKEY},target=/root/.ssh/id_rsa ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?};${CHOWN_CMD} && exit \${RET}" | tee ${GITROOT}/logs/${p}.log
    fi
done

for p in ${IMAGES};do
    CMD="/manager/susemanager-utils/testing/docker/scripts/push-to-obs.sh -d '${DESTINATIONS}' -c /tmp/.oscrc -s /root/.ssh/id_rsa -p '${p}' ${VERBOSE} ${TEST} ${OBS_TEST_PROJECT} ${EXTRA_OPTS}"
    if [ "$PARALLEL_BUILD" == "TRUE" ];then
        echo "Building ${p} in parallel"
        docker run --rm=true -v ${GITROOT}:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc --mount type=bind,source=${SSHKEY},target=/root/.ssh/id_rsa ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?} && exit \${RET}" 2>&1 > ${GITROOT}/logs/${p}.log &
        pid=${!}
        PIDS="${PIDS} ${pid}"
        ln -s ${GITROOT}/logs/${p}.log ${GITROOT}/logs/${pid}.log
    else
        echo "Building ${p}"
        docker run --rm=true -v ${GITROOT}:/manager -v /srv/mirror:/srv/mirror --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc --mount type=bind,source=${SSHKEY},target=/root/.ssh/id_rsa ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?} && exit \${RET}" | tee ${GITROOT}/logs/${p}.log
    fi
done

echo "End of task at ($(date). Logs for each package at ${GITROOT}/logs/"

# Turn off exiting on error because we want to wait for all subprocesses
set +e

PRET=0
PKG_FAILED=""
for i in $PIDS;do
    package_name=$(basename $(readlink ${GITROOT}/logs/${i}.log) | cut -d"." -f1 )
    echo "Waiting for process with pid ${i}, building package ${package_name}"
    wait $i
    result=$?
    echo "$i finished, result is $result"
    if [ $result -ne 0 ];then
        echo "Seems there was an error with process pid ${i}"
        echo "When building package ${package_name}"
        cat ${GITROOT}/logs/${i}.log
        PKG_FAILED="${PKG_FAILED} ${package_name}"
        PRET=-1
    fi
done

if [ $PRET -ne 0 ];then
    echo "The following packages failed to build:"
    for p in ${PKG_FAILED}; do echo "- ${p}"; done
    echo "Please review the logs at ${GITROOT}/logs/"
    exit ${PRET}
fi

set -e
