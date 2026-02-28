#!/bin/bash
# Copyright (c) 2026 SUSE LLC

_IFS=${IFS}
IFS=$'\n'
declare -a ALL_PARAMS
ALL_PARAMS=('Hello world,' "y'all :)" "I'm singing \"I'm singing in the rain\"" "'Cause it's a beautiful day." "2 * 3 * 7")

[ "${#}" -ge 1 ] && ALL_PARAMS=(${@})
DEBUG_MSG=""
for ((i = 0 ; i < ${#ALL_PARAMS[@]} ; i++ )); do
    PARAM="${ALL_PARAMS[@]:${i}:1}"
    if [[ "${PARAM}" =~ [0-9*/+-]+ ]]; then
        echo "${PARAM} = $((${PARAM}))"
    else
        echo "${PARAM}"
    fi
    DEBUG_MSG="${DEBUG_MSG}  $((${i} + 1)):  ${PARAM}\n"
done
printf "\n\n### Shell script debug ###\nParameters:\n${DEBUG_MSG}"
IFS=${_IFS}

exit 0
