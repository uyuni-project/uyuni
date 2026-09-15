#!/bin/bash
# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# default values
DSKCHK_IMAGE_DIR=/root
DSKCHK_IMAGE_FILE=diskcheck_disk.img
DSKCHK_IMAGE_SIZE=4000
DSKCHK_MOUNT=/root/mnt-diskcheck
DSKCHK_SELINUX_MOD_NAME="diskcheck"
DSKCHK_SELINUX_TE=/root/${DSKCHK_SELINUX_MOD_NAME}.te
DSKCHK_SELINUX_MOD=/root/${DSKCHK_SELINUX_MOD_NAME}.mod
DSKCHK_SELINUX_PP=/root/${DSKCHK_SELINUX_MOD_NAME}.pp
# 0 - down, 1 - up, 255 - default undefined
DSKCHK_ACTION=255

function print_help() {
    echo "Usage: ${0} <action> [<options>]"
    echo
    echo "        Action:"
    echo "            up ..................... create an image and mount it"
    echo "            down.................... unmount and remove the image"
    echo "        Options:"
    echo "            -d <image directory> ... a place where the image will be stored, the default: ${DSKCHK_IMAGE_DIR}"
    echo "            -h ..................... prints help"
    echo "            -m <mount point> ....... a place where the image will be mounted, default: ${DSKCHK_MOUNT}"
    echo "            -s <number> ............ image size in MB to be created, default: ${DSKCHK_IMAGE_SIZE}"
}

function setup() {
    echo "Setup:"
    [ ! -d ${DSKCHK_IMAGE_DIR} ] && mkdir -p ${DSKCHK_IMAGE_DIR}
    [ ! -d ${DSKCHK_MOUNT} ] && mkdir -p ${DSKCHK_MOUNT}
    dd if=/dev/zero of=${DSKCHK_IMAGE_DIR}/${DSKCHK_IMAGE_FILE} bs=1M count=${DSKCHK_IMAGE_SIZE} && \
    mkfs.ext4 ${DSKCHK_IMAGE_DIR}/${DSKCHK_IMAGE_FILE} && \
    mount -o loop,rw ${DSKCHK_IMAGE_DIR}/${DSKCHK_IMAGE_FILE} ${DSKCHK_MOUNT}
    return $?
}

function cleanup() {
    echo "Clean up:"
    mount | grep -q " ${DSKCHK_MOUNT} " && umount ${DSKCHK_MOUNT} || true
    [ -f ${DSKCHK_IMAGE_DIR}/${DSKCHK_IMAGE_FILE} ] && rm ${DSKCHK_IMAGE_DIR}/${DSKCHK_IMAGE_FILE}
    return 0
}

# arguments parser
function parse_args() {
    while getopts 'd:hm:s:' option; do
    case "${option}" in
        # directory - image place
        d)
            # used parameter but no directory given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-"); then
                print_help
                exit 1
            fi
            # strip the last '/' if given
            DSKCHK_IMAGE_DIR=$(echo "${OPTARG}" | sed 's/\/$//')
            ;;
        # help
        h)
            print_help
            exit 0
            ;;
        # mount point
        m)
            # used parameter but no directory given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-"); then
                print_help
                exit 1
            fi
            # strip the last '/' if given
            DSKCHK_MOUNT=$(echo "${OPTARG}" | sed 's/\/$//')
            ;;
        # image size
        s)
            # used parameter but no number given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-") || ! [[ "${OPTARG}" =~ ^[0-9]+$ ]]; then
                echo "ERROR: Not a decimal number or wrong value given."
                print_help
                exit 1
            fi
            DSKCHK_IMAGE_SIZE=$((${OPTARG}))
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
((${DSKCHK_ACTION})) && setup || cleanup
exit $?
