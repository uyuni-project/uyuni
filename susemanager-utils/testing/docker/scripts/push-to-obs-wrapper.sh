#!/bin/bash
set -ex

p=$1
shift
user=$1
shift
group=$1
shift
CMD=$@
INITIAL_CMD="/manager/susemanager-utils/testing/automation/initial-objects.sh"
CHOWN_CMD="/manager/susemanager-utils/testing/automation/chown-objects.sh $user $group"
pkg_dir=$(cat /manager/rel-eng/packages/${p} | tr -s " " | cut -d" " -f 2)
CHOWN_CMD="${CHOWN_CMD}; chown -f -R $(id -u):$(id -g) /manager/$pkg_dir"

cleanup() {
    RET=${?}
    if [ ${RET} -eq 0 ];then
        echo "There were no errors"
        exit 0
    fi
    echo "There was an error ${RET}"
    ${CHOWN_CMD}
    exit ${RET}
}

run_me() {
    set -x
    trap cleanup EXIT
    ${INITIAL_CMD}
    ${CMD}
    RET=${?}
    echo "There was no error ${RET}"
    ${CHOWN_CMD}
    exit ${RET}
}

trap cleanup EXIT
run_me

