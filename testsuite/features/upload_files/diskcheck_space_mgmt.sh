#!/bin/bash
# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# default values
DSKCHK_CLEAN=0
DSKCHK_DIRECTORY=/root
DSKCHK_FILE_PREFIX="dskchk_space_holder_"
DSKCHK_MOUNT=/
DSKCHK_PERCENTAGE=0
DSKCHK_NEW_DIR_FILE=dskchk_delete_this_dir
DSKCHK_FILLINGS=0

function print_help() {
    echo "Usage: ${0} <options>"
    echo
    echo "    Mandatory options:"
    echo "        -p <decimal number> ... % of the disk space <100 that should be filled; ignored when cleanup"
    echo
    echo "    Optional options:"
    echo "        -c .................... cleanup; removes existing filling files - it's dependent on -d if given before"
    echo "        -d <directory> ........ the directory to use for the fillings; if not defined, /root is used"
    echo "        -f .................... delete fillings from the specified directory via -d"
    echo "        -h .................... prints help"
}

function cleanup() {
    echo "Cleanup:"
    rm ${DSKCHK_DIRECTORY}/${DSKCHK_FILE_PREFIX}*
    [ -f ${DSKCHK_DIRECTORY}/dskchk_delete_this_dir ] && [ ${DSKCHK_FILLINGS} -eq 0 ] && [ "${DSKCHK_DIRECTORY}" != "/" ] && rm -rf ${DSKCHK_DIRECTORY}
    # it may happen the files are in the "volumes" directory if run in container - in case this needs to be checked as well
    if [ -d /var/lib/containers/storage/volumes ]; then
        # delete the whole directory if created previously
        NEW_DIR_CHECK_FILE=$(find /var/lib/containers/storage/volumes -name "${DSKCHK_NEW_DIR_FILE}" -print) && [ -n "${NEW_DIR_CHECK_FILE}" ] && [ ${DSKCHK_FILLINGS} -eq 0 ] && { rm -rf $(dirname ${NEW_DIR_CHECK_FILE}); return 0; }
        # delete particular files if exist
        for FILE_TO_DELETE in $(find /var/lib/containers/storage/volumes -name "${DSKCHK_FILE_PREFIX}*" -print); do
            rm "${FILE_TO_DELETE}"
        done
    fi
    sync
}

# arguments parser
function parse_args() {
    while getopts 'cd:fhp:' option; do
    case "${option}" in
        # cleanup
        c)
            DSKCHK_CLEAN=1
            ;;
        # directory - storage and watched place
        d)
            # used parameter but no directory given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-"); then
                print_help
                exit 1
            fi
            # strip the last '/' if given
            DSKCHK_DIRECTORY=$(echo "${OPTARG}" | sed 's/\/$//')
            # If the mount does not exist, the default '/' is used
            df -BM ${NEW_MOUNT} &>/dev/null && DSKCHK_MOUNT="${DSKCHK_DIRECTORY}"
            ;;
        # fillings removal
        f)
            DSKCHK_FILLINGS=1
            ;;
        # help
        h)
            print_help
            exit 0
            ;;
        # percentage of the disk to be filled up
        p)
            # used parameter but no percentage given
            if [ -z "${OPTARG}" ] || $(echo "${OPTARG}" | grep -q "^-") || ! [[ "${OPTARG}" =~ ^[0-9]+$ ]] || [ ${OPTARG} -ge 100 ]; then
                echo "ERROR: Not a decimal number or wrong value given."
                print_help
                exit 1
            fi
            DSKCHK_PERCENTAGE=$((${OPTARG}))
            ;;
        *)
            print_help
            exit 1
            ;;
    esac
    done
}


# arguments
parse_args "$@"

# cleanup
if [ ${DSKCHK_CLEAN} -eq 1 ]; then
    cleanup
    exit 0
fi

if [ ${DSKCHK_PERCENTAGE} -eq 0 ]; then
    echo "ERROR: percentage not given. Nothing to do."
    exit 1
fi
if [ ! -d ${DSKCHK_DIRECTORY} ]; then
    mkdir -p ${DSKCHK_DIRECTORY} && touch ${DSKCHK_DIRECTORY}/dskchk_delete_this_dir
fi

DSKCHK_USED_SPACE_PERCENTAGE=$(df --output=pcent "${DSKCHK_MOUNT}" | tail -1 | tr -dc '0-9')
if [ ${DSKCHK_USED_SPACE_PERCENTAGE} -ge ${DSKCHK_PERCENTAGE} ]; then
    echo "Disk already filled up to ${DSKCHK_USED_SPACE_PERCENTAGE}%. Nothing to do."
    exit 0
fi
DSKCHK_MAX_SPACE=$(df -BM "${DSKCHK_MOUNT}" | tail -1 | sed 's/\s\+/ /g' | cut -d' ' -f2 | tr -dc '0-9')
DSKCHK_USED_SPACE=$(df -BM "${DSKCHK_MOUNT}" | tail -1 | sed 's/\s\+/ /g' | cut -d' ' -f3 | tr -dc '0-9')
DSKCHK_ENDSTATE_SPACE=$((${DSKCHK_MAX_SPACE} / 100 * ${DSKCHK_PERCENTAGE}))
DSKCHK_ADD_SPACE=$((${DSKCHK_ENDSTATE_SPACE} - ${DSKCHK_USED_SPACE}))

dd if=/dev/zero of=${DSKCHK_DIRECTORY}/${DSKCHK_FILE_PREFIX}$(date +%Y%m%d%H%M%S) bs=1M count=${DSKCHK_ADD_SPACE}
sync
echo "Current space:"
df -BM "${DSKCHK_MOUNT}"

exit 0
