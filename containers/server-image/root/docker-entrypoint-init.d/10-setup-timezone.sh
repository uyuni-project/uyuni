#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

if [ -n "${TZ}" ]; then
    if [ ! -e "/usr/share/zoneinfo/${TZ}" ]; then
        echo "Wrong timezone set: '${TZ}'"
        exit 1
    fi
    rm -f /etc/localtime
    ln -s "/usr/share/zoneinfo/${TZ}" /etc/localtime
fi
