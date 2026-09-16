#!/bin/bash

# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: MIT

if [[ ! -z "$TZ"  ]]; then
    if [[ ! -f "/usr/share/zoneinfo/$TZ" ]]; then
        echo "Invalid timezone: $TZ"
        exit 1
    fi
    rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/$TZ" /etc/localtime
fi
