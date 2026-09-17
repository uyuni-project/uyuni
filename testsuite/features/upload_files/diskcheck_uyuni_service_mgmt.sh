#!/bin/bash
# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# default values
DSKCHK_ALERT=90
DSKCHK_THRESHOLD=95
DSKCHK_DIR="/root/mnt-diskcheck"
DSKCHK_DIR_TO_WATCH="/"
# 0 - down, 1 - up, 255 - default undefined
DSKCHK_ACTION=255
DSKCHK_SERVICE_FILE="/etc/systemd/system/uyuni-server.service.d/diskcheck.conf"
DSKCHK_BACKUP_SERVICE_FILE="/tmp/diskcheck.conf.backup"
DSKCHK_RHN_CONF="/etc/rhn/rhn.conf"
DSKCHK_RHN_FLAG=0

function print_help() {
    echo "Usage: ${0} <action> [<options>]"
    echo
    echo "        Action:"
    echo "            up .................... deploy the service config file and restart the uyuni service"
    echo "            down................... undeploy the service config file and restart the uyuni service"
    echo "        Options:"
    echo "            -a <decimal number> ... % of the disk space <100, used for DISKCHECKALERT, default: ${DSKCHK_ALERT}"
    echo "            -d <directory> ........ the directory to check, used for DISKCHECKDIRS, default: ${DSKCHK_DIR}"
    echo "            -h .................... prints help"
    echo "            -r .................... rhn.conf setup, -a and -t are ignored"
    echo "            -t <decimal number> ... % of the disk space <100, used for DISKTHRESHOLD, default: ${DSKCHK_THRESHOLD}"
}

function setup() {
    echo "Setup:"
    mount AAA
    mkdir -p "$(dirname ${DSKCHK_SERVICE_FILE})"
    [ -f "${DSKCHK_SERVICE_FILE}" ] && cp "${DSKCHK_SERVICE_FILE}" "${DSKCHK_BACKUP_SERVICE_FILE}"
    if [ ${DSKCHK_RHN_FLAG} -eq 0 ]; then
        cat <<EOF >"${DSKCHK_SERVICE_FILE}"
[Unit]
RequiresMountsFor=${DSKCHK_DIR}

[Service]
Environment="PODMAN_EXTRA_ARGS=--env DISKCHECKDIRS=${DSKCHK_DIR} --env DISKCHECKALERT=${DSKCHK_ALERT} --env DISKTHRESHOLD=${DSKCHK_THRESHOLD} -v ${DSKCHK_DIR}:${DSKCHK_DIR}"
EOF
    # rhn.conf
    else
        cat <<EOF >"${DSKCHK_SERVICE_FILE}"
[Service]
Environment="PODMAN_EXTRA_ARGS=-v ${DSKCHK_DIR}:${DSKCHK_DIR}"
EOF
        # rhn.conf - need to run inside container and the uyuni server needs to restart from outside
        command -v mgrctl >/dev/null || return 1
        mgrctl exec -- "[ -f '${DSKCHK_RHN_CONF}' ]" || return 1
        mgrctl exec -- "grep -q '^spacecheck_dirs' '${DSKCHK_RHN_CONF}' && sed -i 's|^spacecheck_dirs\(.*\)$|#spacecheck_dirs\1|' '${DSKCHK_RHN_CONF}'"
        mgrctl exec -- "echo 'spacecheck_dirs = ${DSKCHK_DIR}' >>'${DSKCHK_RHN_CONF}'"
        mgrctl exec -- "tail -1 '${DSKCHK_RHN_CONF}'"
    fi
    echo "${DSKCHK_SERVICE_FILE}:"
    cat "${DSKCHK_SERVICE_FILE}"
    systemctl daemon-reload
    mgradm restart
    return $?
}

function cleanup() {
    echo "Cleanup:"
    local reload=0
    if [ -f "${DSKCHK_SERVICE_FILE}" ]; then
        # the default configuration exists - restore it from the backup file
        if [ -f "${DSKCHK_BACKUP_SERVICE_FILE}" ]; then
            cat "${DSKCHK_BACKUP_SERVICE_FILE}" >"${DSKCHK_SERVICE_FILE}"
        # no backup file - the configuration is without it, so just delete the file
        else
            rm "${DSKCHK_SERVICE_FILE}"
        fi
        reload=1
    fi
    # rhn.conf
    if [ ${DSKCHK_RHN_FLAG} -eq 1 ]; then
        # rhn.conf - need to run inside container and the uyuni server needs to restart from outside
        reload=1
        command -v mgrctl >/dev/null && \
        mgrctl exec -- "[ -f '${DSKCHK_RHN_CONF}' ]" && \
        mgrctl exec -- "grep -q '^spacecheck_dirs' '${DSKCHK_RHN_CONF}' && sed -i '/^spacecheck_dirs.*$/d' '${DSKCHK_RHN_CONF}'" && \
        mgrctl exec -- "grep -q '^#spacecheck_dirs' '${DSKCHK_RHN_CONF}' && sed -i 's|^#spacecheck_dirs\(.*\)$|spacecheck_dirs\1|' '${DSKCHK_RHN_CONF}'"
    fi
    ((${reload})) && { systemctl daemon-reload && mgradm restart; return $?; } || return 0
}

# arguments parser
function parse_args() {
    while getopts 'a:d:hrt:' option; do
    case "${option}" in
        # alert check
        a)
            # used parameter but no percentage given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-") || ! [[ "${OPTARG}" =~ ^[0-9]+$ ]] || [ ${OPTARG} -ge 100 ]; then
                echo "ERROR: Not a decimal number or wrong value given."
                print_help
                exit 1
            fi
            DSKCHK_ALERT=$((${OPTARG}))
            ;;
        # directory - storage and watched place
        d)
            # used parameter but no directory given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-"); then
                print_help
                exit 1
            fi
            DSKCHK_DIR="${OPTARG}"
            # get the most close mount point
            while true; do
                df | grep -q " ${DSKCHK_DIR}$" && break
                DSKCHK_DIR=$(dirname "${DSKCHK_DIR}")
                [ "${DSKCHK_DIR}" == "/" ] && break
            done
            ;;
        # help
        h)
            print_help
            exit 0
            ;;
        # rhn.conf setup
        r)
            DSKCHK_RHN_FLAG=1
            ;;
        # threshold check
        t)
            # used parameter but no percentage given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-") || ! [[ "${OPTARG}" =~ ^[0-9]+$ ]] || [ ${OPTARG} -ge 100 ]; then
                echo "ERROR: Not a decimal number or wrong value given."
                print_help
                exit 1
            fi
            DSKCHK_THRESHOLD=$((${OPTARG}))
            ;;
        *)
            print_help
            exit 1
            ;;
    esac
    done
    shift $((${OPTIND} - 1))
    if [ -z "${1}" ]; then
        echo "ERROR: No operation given."
        exit 1
    elif [ "${1}" == "up" ]; then
        DSKCHK_ACTION=1
    elif [ "${1}" == "down" ]; then
        DSKCHK_ACTION=0
    else
        echo "ERROR: Unknown argument '${1}'"
        exit 1
    fi
}


# arguments
parse_args "$@"
# up -> setup ; down -> cleanup
if [ ${DSKCHK_ACTION} -eq 1 ]; then
    setup
elif [ ${DSKCHK_ACTION} -eq 0 ]; then
    cleanup
else
    # should have not ended up here
    false
fi
exit $?
