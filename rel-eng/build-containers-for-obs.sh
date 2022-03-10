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
IMAGE="$@"

grep -v -- "\(--help\|-h\|-?\)\>" <<<"$@" || {
  cat <<EOF
Usage: build-containers-for-obs.sh [CONTAINER]..
Build container images for submission to OBS from the current HEAD. Without argument
all containers/*-image are processed. Container image directories will be created in 
\$WORKSPACE/SRPMS/<container> ($WORKSPACE).
EOF
  exit 0
}

# check cwd is in git
GIT_DIR=$(git rev-parse --show-cdup)
test -z "$GIT_DIR" || cd "$GIT_DIR"
GIT_DIR=$(pwd)

# create workspace
test -d "$WORKSPACE" || mkdir -p "$WORKSPACE"

# build the src rpms...
SRPM_DIR="$WORKSPACE/SRPMS"
rm -rf "$SRPM_DIR"
mkdir -p "$SRPM_DIR"

echo "Going to build new obs container images in $SRPM_DIR ..."

CONTAINER_PATH="$GIT_DIR/containers/$IMAGE"

test $CONTAINER_PATH != "$GIT_DIR/containers/" || {
  CONTAINER_PATH=$(ls -d "$GIT_DIR/containers/"*image)
}

for CONTAINER in $CONTAINER_PATH; do
  echo "=== Building container image [$(basename $CONTAINER)]"
  cp -r "$CONTAINER" "$SRPM_DIR/"
done
