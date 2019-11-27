#!/bin/bash

if [[ -z "$1" ]]; then
    echo "Missing argument: before|after"
    exit 1
fi

ACTION="after-update"
if [[ "$1" == "before-update" ]]; then
    ACTION="unlock"
elif [[ "$1" == "after-update" ]]; then
    ACTION="lock"
    ZYPPER_EXIT_CODE=$2
else
    echo "Allowed arguments: before-update|after-update"
    exit 1
fi


PATTERN=$(/usr/bin/rpm -qa | grep patterns-caasp-Node)
if [[ -z "PATTERN" ]];then
    echo "patterns-caasp-Node* not found"
    exit 2
fi

REQUIRES=$(/usr/bin/rpm -q --queryformat "[%{REQUIRES}\n]" $PATTERN)
for REQ in $REQUIRES; do
    if [[ "$REQ" != "pattern()" && "$REQ" != "rpmlib("* ]]; then
        if [[ $ACTION == "lock" ]]; then
            echo "locking $REQ"
            zypper addlock $REQ
        else
            # TODO check zypper exit code ?
            echo "unlocking $REQ"
            zypper removelock $REQ
        fi
    fi
done
