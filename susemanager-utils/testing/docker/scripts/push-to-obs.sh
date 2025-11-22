#!/bin/sh -e

REL_ENG_FOLDER="/manager/rel-eng"

exists() {
  [ -n "$1" -a -e "$1" ]
}

is_package() {
  test -e /tmp/push-packages-to-obs/SRPMS/$1/Dockerfile && { echo "$1 is not a package"; return 1; }
  test -e /tmp/push-packages-to-obs/SRPMS/$1/Chart.yaml && { echo "$1 is not a package"; return 1; }
  exists /tmp/push-packages-to-obs/SRPMS/$1/*.kiwi && { echo "$1 is not a package"; return 1; }
  return 0
}

help() {
  echo ""
  echo "Script to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE -s SSH_PRIVATE_KEY [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destinations in the format API|PROJECT or GITURL|BRANCH,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master,gitea@src.opensuse.org:Galaxy|mlmtools-stable"
  echo "  -p  Comma separated list of packages. If absent, all packages are submitted"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -s  Path to the private key used for MFA, a file ending with .pub must also"
  echo "      exist, containing the public key"
  echo "  -g  Path to TEA config (usually ~/.config/tea/config.yml)"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo "  -n  If used, update PROJECT instead of the projects specified with -d"
  echo "  -e  If used, when checking out projects from obs, links will be expanded. Useful for comparing packages that are links" 
  echo ""
}

OSC_EXPAND="FALSE"

while getopts ":d:c:s:g:p:n:vthe" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES="$(echo ${OPTARG}|tr ',' ' ')";;
    c) export OSCRC=${OPTARG};;
    s) export SSHKEY=${OPTARG};;
    g) export TEACONF=${OPTARG};;
    v) export VERBOSE=1;;
    t) export TEST=1;;
    n) export OBS_TEST_PROJECT=${OPTARG};;
    e) export OSC_EXPAND="TRUE";;
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

if [ "${OSCRC}" == "" ]; then
  echo "ERROR: Mandatory paramenter -c is missing!"
  exit 1
fi

if [ ! -f ${OSCRC} ]; then
  echo "ERROR: File ${OSCRC} does not exist!"
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
  # create known_hosts with a few keys
  SSHDIR=$(dirname ${SSHKEY})
  if ! grep -q src.opensuse.org $SSHDIR/known_hosts ; then
    echo "src.opensuse.org ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIFKNThLRPznU5Io1KrAYHmYpaoLQEMGM9nwpKyYQCkPx" >> $SSHDIR/known_hosts
  fi
  if ! grep -q src.suse.de $SSHDIR/known_hosts ; then
    echo "src.suse.de ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIDkmQXh4J28Gm6dtv8PAQcHVTP3oocrowO+bbW5DNkc9" >> $SSHDIR/known_hosts
  fi
fi

if [ ! -f ${TEACONF} ]; then
  echo "ERROR: File ${TEACONF} does not exist!"
  exit 1
fi


# declare /manager as "safe"
git config --global --add safe.directory /manager
git config --global user.email "galaxy-releng@suse.de"
git config --global user.name "MLM Release Engineering"

cd ${REL_ENG_FOLDER}

# If we have more than one destinations, keep SRPMS so we don't
# need to rebuild for each submission
if [ "$(echo ${DESTINATIONS}|cut -d',' -f2)" != "" ]; then
  export KEEP_SRPMS=TRUE
fi

# Build SRPMS
echo "*************** BUILDING PACKAGES ***************"
build-packages-for-obs ${PACKAGES}

SUBMITTO=""

# Submit 
for DESTINATION in $(echo ${DESTINATIONS}|tr ',' ' '); do

  FIRST=$(echo ${DESTINATION}|cut -d'|' -f1)
  SECOND=$(echo ${DESTINATION}|cut -d'|' -f2)

  if [ "${FIRST:0:7}" == "https:/" -o "${FIRST:0:7}" == "http://" ]; then
    # http URL looks like OBS
    SUBMITTO="OBS"
  elif echo ${FIRST} | grep '@.\+:' >/dev/null ; then
    SUBMITTO="GIT"
  fi

  if [ "${SUBMITTO}" = "OBS" ]; then
    export OSCAPI=${FIRST}
    export OBS_PROJ=${SECOND}
    echo "*************** PUSHING TO ${OBS_PROJ} ***************"
    push-packages-to-obs ${PACKAGES}
  elif [ "${SUBMITTO}" = "GIT" ]; then
    export GIT_USR=$(echo ${FIRST}|cut -d'@' -f1)
    export GIT_SRV=$(echo ${FIRST}|cut -d'@' -f2 | cut -d':' -f1)
    export GIT_ORG=$(echo ${FIRST}|cut -d':' -f2 | cut -d'/' -f1)
    export GIT_PRODUCT_REPO=$(echo ${FIRST}|cut -d':' -f2 | cut -d'/' -f2)
    test "${GIT_ORG}" = ${GIT_PRODUCT_REPO} && export GIT_PRODUCT_REPO=""
    export BRANCH=${SECOND}
    PKS=""
    IMS=""
    for P in ${PACKAGES}; do
      is_package $P && PKS="$PKS $P" || IMS="$IMS $P"
    done

    PKS=$(echo "${PKS}" | awk '{$1=$1;print}')
    IMS=$(echo "${IMS}" | awk '{$1=$1;print}')

    if [  -n "${PKS}" -a -z "${GIT_PRODUCT_REPO}" ]; then
      # Push packages only to destinations which do not define a GIT_PRODUCT_REPO
      echo "*************** PUSHING PACKAGES TO ${GIT_USR}@${GIT_SRV}:${GIT_ORG}#${BRANCH} ***************"
      push-packages-to-git ${PKGS}
    fi

    if [  -n "${IMS}" -a -n "${GIT_PRODUCT_REPO}" ]; then
      # Push images only to destinations which do define a GIT_PRODUCT_REPO
      echo "*************** PUSHING IMAGES TO ${GIT_USR}@${GIT_SRV}:${GIT_ORG}/${GIT_PRODUCT_REPO}#${BRANCH} ***************"
      push-images-to-git ${IMS}
    fi
  else
    echo "ERROR: unknown where to submit to"
    exit 1
  fi
done
