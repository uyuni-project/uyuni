#!/bin/bash

NEW_VERSION=14
OLD_VERSION=12

FAST_UPGRADE=""

if [ $# -gt 1 ]; then
    echo "`date +"%H:%M:%S"`   Usage: '$0' or '$0 fast'"
    exit 1
fi

if [ $# == 1 ]; then
    if [ $1 == "fast" ]; then
        FAST_UPGRADE="-f"
    else
        echo "`date +"%H:%M:%S"`   Unknown option '$1'"
        echo "`date +"%H:%M:%S"`   Usage: '$0' or '$0 fast'"
        exit 1
    fi
fi

$(dirname $0)/pg-migrate-x-to-y.sh -s $OLD_VERSION -d $NEW_VERSION $FAST_UPGRADE
