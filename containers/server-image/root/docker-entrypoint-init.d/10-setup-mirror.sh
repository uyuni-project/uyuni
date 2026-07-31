#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# In the container case, we have the MIRROR_PATH environment variable at setup
if [ "${MIRROR_PATH}" = "disable" ]; then
    if grep -q "^server.susemanager.fromdir =" /etc/rhn/rhn.conf; then
        sed -i "s/^server.susemanager.fromdir =/#server.susemanager.fromdir =/" /etc/rhn/rhn.conf
    fi
elif [ -n "${MIRROR_PATH}" ]; then
    if ! grep -q "^server.susemanager.fromdir = ${MIRROR_PATH}" /etc/rhn/rhn.conf; then
        sed -i "s/^server.susemanager.fromdir =/#server.susemanager.fromdir =/" /etc/rhn/rhn.conf
        echo "server.susemanager.fromdir = ${MIRROR_PATH}" >> /etc/rhn/rhn.conf
    fi
fi
