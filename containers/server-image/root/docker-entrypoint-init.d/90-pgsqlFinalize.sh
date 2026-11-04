#!/usr/bin/env bash
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only
set -eu -o pipefail

productdb=$(sed -n "s/^\s*db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)
reportdb=$(sed -n "s/^\s*report_db_name\s*=\s*\([^ ]*\)\s*$/\1/p" /etc/rhn/rhn.conf)

if [ -z "$productdb" ] || [ -z "$reportdb" ]; then
    echo "Misconfigured product, missing database name in rhn.conf"
    exit 1
fi

query="\set QUIET 1
\pset tuples_only
SELECT COALESCE(
  (SELECT d.datcollversion IS DISTINCT FROM pg_database_collation_actual_version(d.oid)
   FROM pg_database as d
   WHERE d.datname = '$productdb'
  ), false
) reindex;"

# Wait for postgres (bsc#1287172)
# This is needed only for podman, it is handled by helm on kubernetes
# (Note: a better fix would be https://github.com/uyuni-project/uyuni-tools/pull/819 but it does not work with older podman
# so it has been reverted https://github.com/uyuni-project/uyuni-tools/pull/820)
db_timeout=${DB_STARTUP_TIMEOUT:-300}
retries=$db_timeout
reindex_needed=""
last_error=""

while [ "$retries" -gt 0 ]; do
    set +e
    output=$(printf '%s' "$query" | spacewalk-sql --select-mode - 2>&1)
    rc=$?
    set -e
    if [ "$rc" -eq 0 ]; then
        reindex_needed=$(echo "$output" | xargs)
        break
    fi
    last_error="$output"
    if [ "$retries" -eq "$db_timeout" ]; then
        echo "Waiting for database to accept connections..."
    fi
    retries=$((retries - 1))
    sleep 1
done

if [ "$reindex_needed" = "t" ]; then
    # Reindexing may not be needed for every collation change, but better be on the safe side.
    echo "Reindexing database. This may take a while, please do not cancel it!"
    # Replaced bash-only <<< with pipe
    echo "REINDEX DATABASE \"${productdb}\";" | spacewalk-sql --select-mode -

    # After reindexing, alter the collation version
    # Some databases like template0 may not accept changes and that's fine
    set +e
    echo "Refreshing collations"
    echo "ALTER DATABASE ${productdb} REFRESH COLLATION VERSION;" | spacewalk-sql -
    echo "ALTER DATABASE ${reportdb} REFRESH COLLATION VERSION;" | spacewalk-sql --reportdb -
    set -e
elif [ "$reindex_needed" = "f" ]; then
    :
else
    echo "Error: Database is not accepting connections after ${db_timeout}s timeout. Collation check skipped." >&2
    if [ -n "$last_error" ]; then
        echo "Last error: $last_error" >&2
    fi
fi
