#! /bin/sh -e
# Set pipefail so the result of a pipe is different to zero if one of the commands fail.
# This way, we can add tee but be sure the result would be non zero if the command before has failed.
set -o pipefail


SCRIPT=$(basename ${0})
help() {
  echo ""
  echo "Script to run a container to push SUSE Multi-Linux Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} [-P PRODUCT] -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE -s SSH_PRIVATE_KEY [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -s  Path to the private key used for MFA, a file ending with .pub must also"
  echo "      exist, containing the public key"
  echo "  -g  Path to TEA config (usually ~/.config/tea/config.yml)"
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
  echo "By default 'docker' is used to run the container. To use podman instead set EXECUTOR=podman"
  echo "Extra options for the executor please provide set EXECUTOR_OPTS"
  echo ""
}

EXECUTOR="${EXECUTOR:=docker}"
EXECUTOR_OPTS="${EXECUTOR_OPTS}"

PARALLEL_BUILD="FALSE"
if [ -z ${PRODUCT+x} ];then
    VPRODUCT="VERSION.Uyuni"
else
    VPRODUCT="VERSION.${PRODUCT}"
fi

while getopts ":d:c:s:g:p:P:n:vthex" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES="$(echo ${OPTARG}|tr ',' ' ')";;
    P) VPRODUCT="VERSION.${OPTARG}" ;;
    c) CREDENTIALS=${OPTARG};;
    s) SSHKEY=${OPTARG};;
    g) TEACONF=${OPTARG};;
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

if [ "${SSHKEY}" != "" ]; then
  if [ ! -f ${SSHKEY} ]; then
    echo "ERROR: File ${SSHKEY} does not exist!"
    exit 1
  fi
  if [ ! -f ${SSHKEY}.pub ]; then
    echo "ERROR: File ${SSHKEY}.pub does not exist!"
    exit 1
  fi
  # Hint: every key provided is mounted as is_rsa, also when it is not an RSA key. It works also for other key types
  MOUNTSSHKEY="--mount type=bind,source=${SSHKEY},target=/root/.ssh/id_rsa --mount type=bind,source=${SSHKEY}.pub,target=/root/.ssh/id_rsa.pub"
  USESSHKEY="-s /root/.ssh/id_rsa"
  SSHDIR=$(dirname ${SSHKEY})
  if [ -f ${SSHDIR}/known_hosts ]; then
    MOUNTSSHKEY="$MOUNTSSHKEY --mount type=bind,source=${SSHDIR}/known_hosts,target=/root/.ssh/known_hosts"
  fi
fi

if [ "${TEACONF}" != "" ]; then
  if [ ! -f ${TEACONF} ]; then
    echo "ERROR: File ${TEACONF} does not exist!"
    exit 1
  fi
  MOUNTTEA="--mount type=bind,source=${TEACONF},target=/root/.config/tea/config.yml"
  USETEACONF="-g /root/.config/tea/config.yml"
fi

COOKIEJAR=$(mktemp /tmp/osc_cookiejar.XXXXXX)
MOUNTCOOKIEJAR="--mount type=bind,source=${COOKIEJAR},target=/root/.osc_cookiejar"

INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $(id -u) $(id -g)"
$EXECUTOR pull $REGISTRY/$PUSH2OBS_CONTAINER

test -n "$PACKAGES" || {
    PACKAGES=$(ls "$GITROOT"/rel-eng/packages/)
}
echo "Starting building and submission at $(date)"
date
PIDS=""

[ -d ${GITROOT}/logs ] || mkdir ${GITROOT}/logs
for p in ${PACKAGES};do
    pkg_dir=$(cat rel-eng/packages/${p} | tr -s " " | cut -d" " -f 2)
    CHOWN_CMD="${CHOWN_CMD}; chown -f -R $(id -u):$(id -g) /manager/$pkg_dir"
    CMD="/manager/susemanager-utils/testing/docker/scripts/push-to-obs.sh -d '${DESTINATIONS}' -c /tmp/.oscrc ${USESSHKEY} ${USETEACONF} -p '${p}' ${VERBOSE} ${TEST} ${OBS_TEST_PROJECT} ${EXTRA_OPTS}"
    if [ "$PARALLEL_BUILD" == "TRUE" ];then
        echo "Building ${p} in parallel"
        $EXECUTOR run --rm=true -v ${GITROOT}:/manager ${EXECUTOR_OPTS} --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc ${MOUNTCOOKIEJAR} ${MOUNTSSHKEY} ${MOUNTTEA} ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?};${CHOWN_CMD} && exit \${RET}" 2>&1 > ${GITROOT}/logs/${p}.log &
        pid=${!}
        PIDS="${PIDS} ${pid}"
        ln -s ${GITROOT}/logs/${p}.log ${GITROOT}/logs/${pid}.log
    else
        echo "Building ${p}"
        $EXECUTOR run --rm=true -v ${GITROOT}:/manager ${EXECUTOR_OPTS} --mount type=bind,source=${CREDENTIALS},target=/tmp/.oscrc ${MOUNTCOOKIEJAR} ${MOUNTSSHKEY} ${MOUNTTEA} ${REGISTRY}/${PUSH2OBS_CONTAINER} /bin/bash -c "${INITIAL_CMD};${CMD};RET=\${?};${CHOWN_CMD} && exit \${RET}" | tee ${GITROOT}/logs/${p}.log
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

# cleanup temp file
rm -f ${COOKIEJAR}

if [ $PRET -ne 0 ];then
    echo "The following packages failed to build:"
    for p in ${PKG_FAILED}; do echo "- ${p}"; done
    echo "Please review the logs at ${GITROOT}/logs/"
    exit ${PRET}
fi

set -e
