#!/usr/bin/env -S bash -euo pipefail

# SPDX-FileCopyrightText: Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# -*- sh-indentation: 2 -*-

# Check pre-requisites
if ! command -v git &>/dev/null ; then
  echo "Git not found, install git before proceeding"
  exit 2
fi

if ! git rev-parse 2>/dev/null; then
  echo "Not inside of a git repository, exitting"
  exit 3
fi

ENGINE="${CONTAINER_ENGINE:-podman}"
IMAGE='registry.opensuse.org/systemsmanagement/uyuni/master/docker/containers_tw/uyuni-master-python'
TAG='latest'
GITROOT="$(git rev-parse --show-toplevel)"
MOUNT="/mgr"
CHECK_ALL_FILES=""
USE_LOCAL=""

function help() {
  echo ""
  echo "This script lints Uyuni Python code with Black and Pylint"
  echo ""
  echo "Syntax:"
  echo ""
  echo "  $(basename ${0}) [-ahl] [FILE1 FILE2 ..]"
  echo ""
  echo "Format all modified, not commited files in this repository:"
  echo ""
  echo "  $(basename ${0}) -a"
  echo ""
  echo "Format files in this repository:"
  echo "  $(basename ${0}) <FILE1> [FILE2 FILE3 ..]"
  echo ""
  echo "Files must provide path relative to the repository root, for example:"
  echo ""
  echo "  $(basename ${0}) python/spacewalk/satellite_tools/reposync.py"
  echo ""
  echo "You can provide a Python directory, in which case Black lints and Pylint" \
       "checks every Python file in the directory, for example:"
  echo ""
  echo "  $(basename ${0}) python"
  echo ""
  echo "By default, Black and Pylint run in the ${IMAGE} container, same as in the Github CI."
  echo "Use -l to run the locally installed black and pylint binaries instead:"
  echo ""
  echo "  $(basename ${0}) -l -a"
}

function ensure_local_binaries() {
  if ! command -v black &>/dev/null ; then
    echo "black not found, install python3-black before proceeding"
    exit 4
  fi

  if ! command -v pylint &>/dev/null ; then
    echo "pylint not found, install python3-pylint before proceeding"
    exit 5
  fi
}

function ensure_latest_container_image() {
  $ENGINE pull --quiet $IMAGE
}

function execute_black() {
  if [[ "${USE_LOCAL}" == "true" ]]; then
    black -t py36 "$@"
    return
  fi
  $ENGINE run --rm -v ${GITROOT}:${MOUNT}:z ${IMAGE}:${TAG} black -t py36 "$@"
}

function execute_lint() {
  if [[ "${USE_LOCAL}" == "true" ]]; then
    pylint --rcfile "${GITROOT}/python/linting/pylintrc" "$@"
    return
  fi
  $ENGINE run --rm -v ${GITROOT}:${MOUNT}:z ${IMAGE}:${TAG} pylint --rcfile /root/.pylintrc "$@"
}

function get_all_py_files() {
  # Filter added, copied, modified, and renamed files
  echo "$(git diff --name-only --diff-filter=ACMR HEAD)" | grep '\.py' | tr '\n' ' '
}

function main() {
  if [[ "${USE_LOCAL}" == "true" ]]; then
    ensure_local_binaries
    # File paths are relative to the repository root
    cd "${GITROOT}"
  else
    ensure_latest_container_image
  fi
  if [[ "${CHECK_ALL_FILES}" == "true" ]]; then
    files="$(get_all_py_files)"
    echo "Linting and formatting: $files"
    execute_black $files
    execute_lint $files
    exit 0
  elif [[ $# -eq 0 ]]; then
    help
    exit 1
  fi
  execute_black "$@"
  execute_lint "$@"
}

while getopts ":ahl" flag; do
  case "${flag}" in
      a) CHECK_ALL_FILES="true";;
      h) help && exit 0;;
      l) USE_LOCAL="true";;
  esac
done
shift $((OPTIND - 1))

main "$@"
