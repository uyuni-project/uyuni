#!/bin/bash

if [[ ! -z "$TZ"  ]]; then
    if [[ ! -f "/usr/share/zoneinfo/$TZ" ]]; then
        echo "Invalid timezone: $TZ"
        exit 1
    fi
    rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/$TZ" /etc/localtime
fi
