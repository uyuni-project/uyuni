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
echo > $WORKSPACE/succeeded
echo > $WORKSPACE/failed

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


while read PKG_NAME PKG_VER PKG_DIR; do
    ./rel-eng/build-one-package-for-obs.sh $PKG_NAME $PKG_VER $PKG_DIR &
done < <(git_package_defs)

wait

SUCCEED_CNT=$(cat $WORKSPACE/succeeded | wc -l)
FAILED_CNT=$(cat $WORKSPACE/failed | wc -l)
FAILED_PKG=$(cat $WORKSPACE/failed)

echo "======================================================================"
echo "Built obs packages:  $SUCCEED_CNT"
test $FAILED_CNT != 0 && {
  echo "Failed obs packages: $FAILED_CNT$FAILED_PKG"
}
echo "======================================================================"

exit $FAILED_CNT
