#! /bin/bash
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
              | grep -v -x -e heirloom-pkgtools -e oracle-server-admin -e oracle-server-scripts -e rhnclient -e smartpm -e jabberd-selinux -e oracle-rhnsat-selinux -e oracle-selinux -e oracle-xe-selinux -e spacewalk-monitoring-selinux -e spacewalk-proxy-selinux -e spacewalk-selinux -e apt-spacewalk -e perl-DBD-Oracle)
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

VERBOSE=
while read PKG_NAME PKG_VER PKG_DIR; do
  echo "=== Building package [$PKG_NAME-$PKG_VER] from $PKG_DIR"
  rm -rf "$SRPMBUILD_DIR"
  mkdir -p "$SRPMBUILD_DIR"

  cd "$GIT_DIR/$PKG_DIR"
  $TITO build ${VERBOSE:+--debug} --test --srpm >"$T_LOG" 2>&1 || {
    cat "$T_LOG"
    FAILED_CNT=$(($FAILED_CNT+1))
    FAILED_PKG="$FAILED_PKG$(echo -ne "\n    $PKG_NAME-$PKG_VER")"
    echo "*** FAILED Building package [$PKG_NAME-$PKG_VER]"
    continue
  }
  ${VERBOSE:+cat "$T_LOG"}

  eval $(awk '/^Wrote:.*src.rpm/{srpm=$2}/^Wrote:.*.changes/{changes=$2}END{ printf "SRPM=\"%s\"\n",srpm; printf "CHANGES=\"%s\"\n",changes; }' "$T_LOG")
  mkdir "$T_DIR"
  ( set -e; cd "$T_DIR"; unrpm "$SRPM"; ) >/dev/null 2>&1
  test -z "$CHANGES" || mv "$CHANGES" "$T_DIR"

  mv "$T_DIR" "$SRPM_DIR/$PKG_NAME"
  #
  # change BuildRoot: in spec files to SUSE flavor.
  # The buildservice requires it this way.
  # Otherwise some errors are not seen during build
  #
  sed -i 's/^BuildRoot.*$/BuildRoot:    %{_tmppath}\/%{name}-%{version}-build/i' $SRPM_DIR/$PKG_NAME/*.spec
  SUCCEED_CNT=$(($SUCCEED_CNT+1))
done < <(git_package_defs)

echo "======================================================================"
echo "Built obs packages:  $SUCCEED_CNT"
test $FAILED_CNT != 0 && {
  echo "Failed obs packages: $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"

exit $FAILED_CNT
