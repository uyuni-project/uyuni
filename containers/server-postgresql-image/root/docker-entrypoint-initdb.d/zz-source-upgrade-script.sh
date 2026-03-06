#!/bin/bash
# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

set -Eeo pipefail

UPGDB_DIR="/docker-entrypoint-upgdb.d"

if [ ! -d "$UPGDB_DIR" ]; then
    echo "Upgrade scripts directory $UPGDB_DIR not found, skipping."
    exit 0
fi

for f in $(find "$UPGDB_DIR" -maxdepth 1 -type f | sort); do
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
