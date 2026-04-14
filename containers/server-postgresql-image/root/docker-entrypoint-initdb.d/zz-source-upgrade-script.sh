#!/bin/bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

set -Eeo pipefail

UPGRADE_HOOKS_DIR="/docker-entrypoint-upgdb.d"

if [ ! -d "$UPGRADE_HOOKS_DIR" ]; then
    echo "Upgrade scripts directory $UPGRADE_HOOKS_DIR not found, skipping."
    exit 0
fi

for f in $(find "$UPGRADE_HOOKS_DIR" -maxdepth 1 -type f | sort); do
    case "$f" in
        *.sh)
            echo "Running upgrade script: $f"
            "$f"
            ;;
        *)
            echo "Ignoring unrecognised file: $f"
            ;;
    esac
done
