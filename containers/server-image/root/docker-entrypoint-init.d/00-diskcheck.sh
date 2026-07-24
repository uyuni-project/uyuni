#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Check if disk space is critically low
/usr/bin/spacewalk-diskcheck -f || {
    RC=$?
    if [ $RC -eq 3 ]; then
        echo "Startup aborted: Critically low disk space detected!"
        exit 1
    fi
    true
}
