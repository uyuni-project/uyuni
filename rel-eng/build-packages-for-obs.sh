#! /bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
set -e
#
# For all packages in git:/rel-eng/packages (or defined in $PACKAGES)
# provide tarball, spec and changes in $WORKSPACE/SRPMS/<package>
#
# git_package_defs() has a hardcoded list of packages excluded by default.
#
WORKSPACE=${WORKSPACE:-/tmp/push-packages-to-obs}
PACKAGE="$@"

grep -v -- "\(--help\|-h\|-?\)\>" <<<"$@" || {
  cat <<EOF
Usage: build-packages-for-obs.sh [PACKAGE]..
Build package for submission to OBS from the current HEAD. Without argument
all packages mentioned in rel-eng/packages are processed. Package directories
will be created in \$WORKSPACE/SRPMS/<package> ($WORKSPACE).
EOF
  exit 0
}

# check cwd is in git
GIT_DIR=$(git rev-parse --show-cdup)
test -z "$GIT_DIR" || cd "$GIT_DIR"
GIT_DIR=$(pwd)

# check presence of tito
test -x "/usr/bin/tito" || {
  echo "Missing '/usr/bin/tito' needed for build." >&2
  exit 2
}
TITO="/usr/bin/tito"

# check for unrpm
which unrpm &> /dev/null || {
  echo "unrpm not found in the PATH, do 'zypper install build'" >&2
  exit 2
}

# create workspace
test -d "$WORKSPACE" || mkdir -p "$WORKSPACE"

# build the src rpms...
SRPM_DIR="$WORKSPACE/SRPMS"
rm -rf "$SRPM_DIR"
mkdir -p "$SRPM_DIR"

SRPMBUILD_DIR="$WORKSPACE/SRPMBUILD"
rm -rf "$SRPMBUILD_DIR"
mkdir -p "$SRPMBUILD_DIR"
trap "test -d \"$SRPMBUILD_DIR\" && /bin/rm -rf -- \"$SRPMBUILD_DIR\" " 0 1 2 3 13 15

# not nice but tito does not take it via CLI, via .rc
# file prevents parallel execution for different OBS
# projects.Thus we patched tito to take the builddir
# from environment:
export RPMBUILD_BASEDIR=$SRPMBUILD_DIR

function git_package_defs() {
  # - "PKG_NAME PKG_VER PKG_DIR" from git:/rel-eng/packages/, using
  #   a hardcoded blacklist of packages we do not build.
  # - define $PACKAGE to build a specific set of packages.
  # - usage:
  #      while read PKG_NAME PKG_VER PKG_DIR; do
  #        ...
  #      done < <(git_package_defs)
  #
  test -n "$PACKAGE" || {
    PACKAGE=$(ls "$GIT_DIR"/rel-eng/packages/ \
              | grep -v -x -e heirloom-pkgtools -e rhnclient -e smartpm -e jabberd-selinux -e oracle-rhnsat-selinux -e oracle-selinux -e oracle-xe-selinux -e spacewalk-monitoring-selinux -e spacewalk-proxy-selinux -e spacewalk-selinux -e cx_Oracle -e apt-spacewalk -e perl-DBD-Oracle -e spacewalk-jpp-workaround)
  }
  for N in $PACKAGE; do
    awk -vN=$N '{printf "%s %s %s\n", N, $1, $2}' "$GIT_DIR"/rel-eng/packages/$N
  done
}

echo "Going to build new obs packages in $SRPM_DIR..."
T_DIR="$SRPMBUILD_DIR/.build"
T_LOG="$SRPMBUILD_DIR/.log"
SUCCEED_CNT=0
FAILED_CNT=0
FAILED_PKG=

VERBOSE=$VERBOSE
while read PKG_NAME PKG_VER PKG_DIR; do
 for tries in 1 2 3; do

  if [[ $PKG_DIR == *"containers"* ]]; then
    CONTAINER_NAME=$(basename "$PKG_DIR")
    CONTAINS_SPEC_FILE=0

    if ls $GIT_DIR/$PKG_DIR | grep \.spec 1> /dev/null 2>&1; then
      CONTAINS_SPEC_FILE=1
    fi
    if [ ! -d "$GIT_DIR/$PKG_DIR" ]; then
      FAILED_CNT=$(($FAILED_CNT+1))
      FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $(basename $PKG_DIR)")"
      echo "*** FAILED Building package [$(basename $PKG_DIR)] - $PKG_DIR does not exist"
      continue 2
    elif [ $CONTAINS_SPEC_FILE == 0 ]; then
      #check if folder does not contains a spec file, otherwise although it's in containers folder it's a RPM package
      CONTAINER_NAME=$(basename "$PKG_DIR")
      echo "=== Building container image [${CONTAINER_NAME}]"

      cp -r "$GIT_DIR/$PKG_DIR" "$SRPM_DIR/"
      if [ -f "${PKG_DIR}/Chart.yaml" ]; then
        pushd "${SRPM_DIR}/${CONTAINER_NAME}" >/dev/null
        CHART_FILES="values.yaml values.schema.json charts crds templates LICENSE README.md"
        TO_INCLUDE=()
        for F in ${CHART_FILES}; do
          if [ -e "${F}" ]; then
              TO_INCLUDE+=("${F}")
          fi
        done
        tar cf "${SRPM_DIR}/${CONTAINER_NAME}/${CONTAINER_NAME}.tar" "${TO_INCLUDE[@]}"
        rm -r "${TO_INCLUDE[@]}"
        popd >/dev/null
      fi
      SUCCEED_CNT=$(($SUCCEED_CNT+1))
      continue 2
    else # $CONTAINS_SPEC_FILE==1
      echo "*** [ $PKG_DIR ] contains a spec file"
    fi
  fi

  echo "=== Building package [$PKG_NAME-$PKG_VER] from $PKG_DIR (Try $tries)"
  rm -rf "$SRPMBUILD_DIR"
  mkdir -p "$SRPMBUILD_DIR"

  cd "$GIT_DIR/$PKG_DIR"
  $TITO build ${VERBOSE:+--debug} ${TEST:+--test} --srpm >"$T_LOG" 2>&1 || {
    cat "$T_LOG"
    test $tries -eq 3 || continue
    FAILED_CNT=$(($FAILED_CNT+1))
    FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $PKG_NAME-$PKG_VER")"
    echo "*** FAILED Building package [$PKG_NAME-$PKG_VER]"
    continue 2
  }
  ${VERBOSE:+cat "$T_LOG"}

  eval $(awk '/^Wrote:.*src.rpm/{srpm=$2}/^Wrote:.*.changes/{changes=$2}END{ printf "SRPM=\"%s\"\n",srpm; printf "CHANGES=\"%s\"\n",changes; }' "$T_LOG")
  if [ "$(head -n1 ${CHANGES}|grep '^- ')" != "" ]; then
    echo "*** Untagged package, adding fake header..."
    sed -i "1i Fri Jan 01 00:00:00 CEST 2038 - faketagger@suse.inet\n" ${CHANGES}
    sed -i '1i -------------------------------------------------------------------' ${CHANGES}
  fi
  if [ -e "$SRPM" -a -e "$CHANGES" ]; then
    mkdir "$T_DIR"
    ( set -e; cd "$T_DIR"; unrpm "$SRPM"; ) >/dev/null 2>&1
    test -z "$CHANGES" || mv "$CHANGES" "$T_DIR"
  else
    test $tries -eq 3 || continue
    FAILED_CNT=$(($FAILED_CNT+1))
    FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $PKG_NAME-$PKG_VER")"
    echo "*** FAILED Building package [$PKG_NAME-$PKG_VER] - src.rpm or changes file does not exist"
    continue 2
  fi

  # Convert to obscpio
  SPEC_VER=$(sed -n -e 's/^Version:\s*\(.*\)/\1/p' ${T_DIR}/${PKG_NAME}.spec)
  SOURCE=$(sed -n -e 's/^\(Source\|Source0\):\s*.*[[:space:]\/]\(.*\)/\2/p' ${T_DIR}/${PKG_NAME}.spec|sed -e "s/%{name}/${PKG_NAME}/"|sed -e "s/%{version}/${SPEC_VER}/")
  # If the package does not have sources, we don't need to repackage them
  if [ "${SOURCE}" != "" ]; then
    FOLDER=$(tar -tf ${T_DIR}/${SOURCE}|head -1|sed -e 's/\///')
    (cd ${T_DIR}; tar -xf ${SOURCE}; rm ${SOURCE}; mv ${FOLDER} ${PKG_NAME}; find ${PKG_NAME} | cpio --create --format=newc --reproducible > ${FOLDER}.obscpio; rm -rf ${PKG_NAME})
  fi
  # Move to destination
  mv "$T_DIR" "$SRPM_DIR/$PKG_NAME"
  # If the package does not have sources, we don't need service or .obsinfo file
  if [ "${SOURCE}" != "" ]; then
    # Copy service
    cp ${BASE_DIR}/_service "${SRPM_DIR}/${PKG_NAME}"
    # Create .obsinfo file
    cat > "${SRPM_DIR}/${PKG_NAME}/${PKG_NAME}.obsinfo" <<EOF
name: ${PKG_NAME}
version: $(echo ${FOLDER}|sed -e "s/${PKG_NAME}-//")
mtime: $(date +%s)
commit: $(git rev-parse --verify HEAD)
EOF
  fi
  # Release is handled by the Buildservice
  # Remove everything what prevents us from submitting
  sed -i 's/^Release.*$/Release:    1/i' $SRPM_DIR/$PKG_NAME/*.spec

  SUCCEED_CNT=$(($SUCCEED_CNT+1))
  break
 done
done < <(git_package_defs)

echo "======================================================================"
echo "Built obs packages:  $SUCCEED_CNT"
test $FAILED_CNT != 0 && {
  echo "Failed obs packages: $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"

exit $FAILED_CNT
