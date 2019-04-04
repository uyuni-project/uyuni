#!/bin/bash -e
SCRIPT=$(basename ${0})
BASE_DIR=$(dirname "${0}")
OSC="osc -A https://api.suse.de"
# List of packages that won't be built in any case
EXCLUDED_PACKAGES=(heirloom-pkgtools oracle-server-admin oracle-server-scripts rhnclient smartpm jabberd-selinux oracle-rhnsat-selinux oracle-selinux oracle-xe-selinux spacewalk-monitoring-selinux spacewalk-proxy-selinux spacewalk-selinux cx_Oracle apt-spacewalk perl-DBD-Oracle spacewalk-jpp-workaround)

print_incorrect_syntax() {
  echo "ERROR: Incorrect syntax (use -h for help)"
}

help() {
  echo ""
  echo "Wrapper to build spacewalk packages from the git repo using osc build"
  echo ""
  echo "Syntax: "
  echo ""
  echo "${SCRIPT} <ARGUMENTS>"
  echo ""
  echo "Mandatory:"
  echo ""
  echo " --osc_project_wc=<PATH>    Path to the OSC project working path"
  echo " --repository=<REPOSITORY>  Repository to use for osc build (e.g. SLE_15 or"
  echo "                            SLE_12_SP3)"
  echo ""
  echo "Optional:"
  echo ""
  echo " --packages=<PACKAGE_NAMES> One or more package names, separated by commas."
  echo "                            If not present, all packages will be built"
  echo " --no_revert                DANGEROUS: Do not revert changes on OSC project"
  echo "                            working copy. Should use it only for debugging"
  echo " --force                    Continue even if there are uncommited changes."
  echo "                            Can be a problem if such changes affect the"
  echo "                            packages you intend to build"
  echo ""
}


#
# Check if tito installed
#
check_requirements () {
    if [ "$(which tito 2>/dev/null)" == "" ]; then
	cat <<EOF
Please install tito. Read more how to do this here:
https://github.com/uyuni-project/uyuni/wiki/tito
EOF
	exit 1;
    fi
}

check_requirements;

# read the options
ARGS=$(getopt -o h --long help,no_revert,force,packages:,osc_project_wc:,repository: -n "${SCRIPT}" -- "$@")
if [ $? -ne 0 ];
then
  print_incorrect_syntax
  exit 1
fi
eval set -- "${ARGS}"
while true ; do
  case "${1}" in
    -h|--help)        help; exit 1;;
    --packages)       PACKAGES="$(echo ${2}|tr ',' ' ')"; shift 2;;
    --osc_project_wc) OSC_PROJECT_WC="${2}"; shift 2;;
    --repository)     REPOSITORY="${2}"; shift 2;;
    --no_revert)      REVERT="FALSE"; shift 1;;
    --force)          FORCE="TRUE"; shift 1;;
    --) shift ; break ;;
    *) print_incorrect_syntax; exit 1;;
  esac
done

if [ "${OSC_PROJECT_WC}" == "" -o \
     "${REPOSITORY}" == "" ]; then
  print_incorrect_syntax
  exit 1
fi

if [ ! -d ${OSC_PROJECT_WC} ]; then
  echo "ERROR: Directory ${OSC_PROJECT_WC} does not exist!"
  exit 1
fi

if [[ ! -z $(git status -s) ]]; then
  if [ "${FORCE}" == "TRUE" ]; then
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "WARNING!!!! Uncommited changes detected, but continuing as --force was present!!!"
    echo "Keep in mind that if any of the changes is part of the packages you are trying to"
    echo "build, those changes WILL NOT be part of the build!!!"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  else
    echo "ERROR: Uncommited changes detected. Commit or stash them, or if you are sure that"
    echo "such changes will not affect the packages you intend to build, use --force"
    exit 1
  fi
fi

# If --packages was not present, build all packages
if [ "${PACKAGES}" == "" ]; then
  for EXCLUDED_PACKAGE in ${EXCLUDED_PACKAGES[@]}; do
    EXCLUDED_GREP_PACKAGES=$(echo "${EXCLUDED_GREP_PACKAGES} -e ${EXCLUDED_PACKAGE}")
  done
  PACKAGES=$(ls ${BASE_DIR}/packages/ | grep -v -x ${EXCLUDED_GREP_PACKAGES})
fi

echo "**************************************************************"
echo " Building tarballs, specs and changelogs..."
echo "**************************************************************"
export TEST='TRUE' # So tito runs with --test and uses HEAD and tags
${BASE_DIR}/build-packages-for-obs.sh ${PACKAGES}
echo "**************************************************************"
echo " Building packages with osc build for ${REPOSITORY}..."
echo "**************************************************************"
for PACKAGE in ${PACKAGES}; do
  if [ ! -d ${OSC_PROJECT_WC}/${PACKAGE} ]; then
    echo "ERROR: ${OSC_PROJECT_WC}/${PACKAGE} does not exist! Do you need to checkout the package?"
    exit 1
  fi
  cp /tmp/push-packages-to-obs/SRPMS/${PACKAGE}/* ${OSC_PROJECT_WC}/${PACKAGE}
  cd ${OSC_PROJECT_WC}/${PACKAGE}
  set +e
  ${OSC} build ${REPOSITORY}
  if [ ${?} -ne 0 ]; then
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "WARNING!!!! Build failed, but changes ${OSC_PROJECT_WC}/${PACKAGE}"
    echo "where not reverted so you can check!!!"
    echo "If you want to revert, go to directory ${OSC_PROJECT_WC}/${PACKAGE}"
    echo "and run:"
    echo "${OSC} revert *"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    exit 1
  fi
  set -e 
done
if [ "${REVERT}" == "FALSE" ]; then
  echo ""
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  echo "WARNING!!!! You decided not to revert changes to local OSC project working copy!!!"
  echo "This means you could have conflicts next time you run 'osc up'!!!"
  echo "Also you could screw up packages if you run 'osc commit'!!!"
  echo "If you changed your mind, run the same command with without --no_revert option!!!"
  echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
  exit 1
else
  echo "**************************************************************"
  echo " Reverting changes at local OSC project working copy..."
  echo "**************************************************************"
  for PACKAGE in ${PACKAGES}; do
    echo "Reverting ${PACKAGE}..."
    cd ${OSC_PROJECT_WC}/${PACKAGE}
    ${OSC} revert * || true # Tolerate errors, in case there are untracked files
  done
fi
exit 0 
