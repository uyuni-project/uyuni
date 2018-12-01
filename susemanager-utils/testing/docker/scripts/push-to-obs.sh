#!/bin/sh -e

REL_ENG_FOLDER="/manager/rel-eng"

help() {
  echo ""
  echo "Script to push SUSE Manager/Uyuni packages to IBS/OBS"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} -d <API1|PROJECT1>[,<API2|PROJECT2>...] [-v] [-t]"
  echo ""
  echo "Where: "
  echo "  -d  Comma separated list of destionations in the format API/PROJECT,"
  echo "      for example https://api.opensuse.org|systemsmanagement:Uyuni:Master"
  echo "  -c  Path to the OSC credentials (usually ~/.osrc)"
  echo "  -v  Verbose mode"
  echo "  -t  For tito, use current branch HEAD instead of latest package tag"
  echo ""
}

while getopts ":d:c:vth" opts; do
  case "${opts}" in
    d) DESTINATIONS=${OPTARG};;
    c) export OSCRC=${OPTARG};;
    v) export VERBOSE=1;;
    t) export TEST=1;;
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

cd ${REL_ENG_FOLDER}

# If we have more than one destinations, keep SRPMS so we don't
# need to rebuild for each submission
if [ "$(echo ${DESTINATIONS}|cut -d',' -f2)" != "" ]; then
  export KEEP_SRPMS=TRUE
fi

# Build SRPMS
echo "*************** BUILDING PACKAGES ***************"
./build-packages-for-obs.sh

# Submit 
for DESTINATION in $(echo ${DESTINATIONS}|tr ',' ' '); do
  export OSCAPI=$(echo ${DESTINATION}|cut -d'|' -f1)
  export OBS_PROJ=$(echo ${DESTINATION}|cut -d'|' -f2)
  echo "*************** PUSHING TO ${OBS_PROJ} ***************"
  ./push-packages-to-obs.sh
done

# Clean directories generated as user root
rm -r ./susemanager-frontend/susemanager-nodejs-sdk-devel/node_modules
rm -r ./web/html/src/node_modules
rm -r ./web/html/src/dist
