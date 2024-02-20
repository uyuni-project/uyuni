#!/bin/sh -e
set -x

REL_ENG_FOLDER="/manager/rel-eng"

help() {
  echo ""
  echo "Script to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE -s SSH_PRIVATE_KEY [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -p  Comma separated list of packages. If absent, all packages are submitted"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -s  Path to the private key used for MFA, a file ending with .pub must also"
  echo "      exist, containing the public key"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo "  -n  If used, update PROJECT instead of the projects specified with -d"
  echo "  -e  If used, when checking out projects from obs, links will be expanded. Useful for comparing packages that are links" 
  echo ""
}

OSC_EXPAND="FALSE"

while getopts ":d:c:s:p:n:vthe" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES="$(echo ${OPTARG}|tr ',' ' ')";;
    c) export OSCRC=${OPTARG};;
    s) export SSHKEY=${OPTARG};;
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
fi

# declare /manager as "safe"
git config --global --add safe.directory /manager

cd ${REL_ENG_FOLDER}

# If we have more than one destinations, keep SRPMS so we don't
# need to rebuild for each submission
if [ "$(echo ${DESTINATIONS}|cut -d',' -f2)" != "" ]; then
  export KEEP_SRPMS=TRUE
fi

# Build SRPMS
echo "*************** BUILDING PACKAGES ***************"
build-packages-for-obs ${PACKAGES}

# Submit 
for DESTINATION in $(echo ${DESTINATIONS}|tr ',' ' '); do
  export OSCAPI=$(echo ${DESTINATION}|cut -d'|' -f1)
  export OBS_PROJ=$(echo ${DESTINATION}|cut -d'|' -f2)
  echo "*************** PUSHING TO ${OBS_PROJ} ***************"
  push-packages-to-obs ${PACKAGES}
done
