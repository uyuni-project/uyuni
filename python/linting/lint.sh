#!/usr/bin/env -S bash -euo pipefail

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

function help() {
  echo ""
  echo "This script lints Uyuni Python code with Black and Pylint"
  echo ""
  echo "Syntax:"
  echo ""
  echo "  $(basename ${0}) [-ah] [FILE1 FILE2 ..]"
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
  echo "  $(basename ${0}) python/spacewalk/satellite_tools/xmlWireSource.py"
  echo ""
  echo "You can provide a Python directory, in which case Black lints and Pylint" \
       "checks every Python file in the directory, for example:"
  echo ""
  echo "  $(basename ${0}) python"
}

function ensure_latest_container_image() {
  $ENGINE pull --quiet $IMAGE
}

function execute_black() {
  $ENGINE run --rm -v ${GITROOT}:${MOUNT} ${IMAGE}:${TAG} black -t py36 "$@"
}

function execute_lint() {
  $ENGINE run --rm -v ${GITROOT}:${MOUNT} ${IMAGE}:${TAG} pylint --rcfile /root/.pylintrc "$@"
}

function get_all_py_files() {
  # Filter added, copied, modified, and renamed Python files
  echo "$(git diff --name-only --diff-filter=ACMR HEAD)" | grep '\.py' | tr '\n' ' '
}

function get_all_files_with_python_shebang() {
  # Filter added, copied, modified, and renamed files containing Python shebang on it
  LIST_OF_FILES=$(git diff --name-only --diff-filter=ACMR HEAD)
  if [ ! -z "$LIST_OF_FILES" ]; then
    egrep '^#!/usr/bin/python|^#!/usr/bin/env python' $LIST_OF_FILES | cut -d":" -f1 | sort -u | tr '\n' ' '
  fi
}

function main() {
  ensure_latest_container_image
  if [[ "${CHECK_ALL_FILES}" == "true" ]]; then
    files="$(get_all_py_files)$(get_all_files_with_python_shebang)"
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

while getopts ":ah" flag; do
  case "${flag}" in
      a) CHECK_ALL_FILES="true";;
      h) help && exit 0;;
  esac
done

main "$@"
