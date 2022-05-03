#!/bin/sh -e
set -x

REL_ENG_FOLDER="/manager/rel-eng"

help() {
  echo ""
  echo "Script to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -d <API1|PROJECT1>[,<API2|PROJECT2>...] -c OSC_CFG_FILE [-p PACKAGE1,PACKAGE2,...,PACKAGEN] [-v] [-t] [-n PROJECT]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -p  Comma separated list of packages. If absent, all packages are submitted"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo "  -n  If used, update PROJECT instead of the projects specified with -d"
  echo "  -e  If used, when checking out projects from obs, links will be expanded. Useful for comparing packages that are links" 
  echo ""
}

OSC_EXPAND="FALSE"

while getopts ":d:c:p:n:vthe" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    p) PACKAGES="$(echo ${OPTARG}|tr ',' ' ')";;
    c) export OSCRC=${OPTARG};;
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

# declare /manager as "save"
git config --global --add safe.directory /manager

cd ${REL_ENG_FOLDER}

# If we have more than one destinations, keep SRPMS so we don't
# need to rebuild for each submission
if [ "$(echo ${DESTINATIONS}|cut -d',' -f2)" != "" ]; then
  export KEEP_SRPMS=TRUE
fi

# separate packages and container images
IMAGES=
PKGS=
for P in ${PACKAGES}; do
    if [ -f packages/${P} ]; then
        PKGS="${PKGS} ${P}"
    else
        IMAGES="${IMAGES} ${P}"
    fi
done

# Build SRPMS
if [ -z "${PACKAGES}" -o -n "${PKGS}" ]; then
  echo "*************** BUILDING PACKAGES ***************"
  ./build-packages-for-obs.sh ${PKGS}
fi

if [ -z "${PACKAGES}" -o -n "${IMAGES}" ]; then
  echo "********** BUILDING CONTAINER IMAGES ************"
  ./build-containers-for-obs.sh ${IMAGES}
fi

# Submit 
for DESTINATION in $(echo ${DESTINATIONS}|tr ',' ' '); do
  export OSCAPI=$(echo ${DESTINATION}|cut -d'|' -f1)
  export OBS_PROJ=$(echo ${DESTINATION}|cut -d'|' -f2)
  echo "*************** PUSHING TO ${OBS_PROJ} ***************"
  ./push-packages-to-obs.sh ${PACKAGES}
done
