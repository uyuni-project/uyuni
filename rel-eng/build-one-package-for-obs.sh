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
PKG_NAME=$1
PKG_VER=$2
PKG_DIR=$3

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
SRPM_DIR="$WORKSPACE/SRPMS/$PKG_NAME"
rm -rf "$SRPM_DIR"
mkdir -p "$SRPM_DIR"

SRPMBUILD_DIR="$WORKSPACE/SRPMBUILD/$PKG_NAME"
rm -rf "$SRPMBUILD_DIR"
mkdir -p "$SRPMBUILD_DIR"
trap "test -d \"$SRPMBUILD_DIR\" && /bin/rm -rf -- \"$SRPMBUILD_DIR\" " 0 1 2 3 13 15

# not nice but tito does not take it via CLI, via .rc
# file prevents parallel execution for different OBS
# projects.Thus we patched tito to take the builddir
# from environment:
export RPMBUILD_BASEDIR=$SRPMBUILD_DIR

echo "Going to build new obs packages in $SRPM_DIR..."
T_DIR="$SRPMBUILD_DIR/.build"
T_LOG="$SRPMBUILD_DIR/.log"

VERBOSE=$VERBOSE

for tries in 1 2 3; do
  echo "=== Building package [$PKG_NAME-$PKG_VER] from $PKG_DIR (Try $tries)"
  rm -rf "$SRPMBUILD_DIR"
  mkdir -p "$SRPMBUILD_DIR"

  cd "$GIT_DIR/$PKG_DIR"
  $TITO build ${VERBOSE:+--debug} ${TEST:+--test} --srpm >"$T_LOG" 2>&1 || {
    cat "$T_LOG"
    test $tries -eq 3 || continue
    RESULT=-1
    echo $PKG_NAME >> $WORKSPACE/failed
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
    RESULT=-1
    echo $PKG_NAME >> $WORKSPACE/failed
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
  echo "$PKG_NAME" >> $WORKSPACE/succeeded
  RESULT=0
  break
 done


exit $RESULT
