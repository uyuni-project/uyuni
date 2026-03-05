#!/usr/bin/env bash
# SPDX-License-Identifier: MIT
# Custom entrypoint for Uyuni PostgreSQL container.
#
# Behaviour:
#   - Fresh data directory  → delegate entirely to the upstream entrypoint
#                             (which runs /docker-entrypoint-initdb.d/*)
#   - Existing data dir, image unchanged → just start postgres normally
#   - Existing data dir, image changed   → run upgrade scripts from
#                                          /docker-entrypoint-upgdb.d/*,
#                                          then start postgres normally
#
# Image identity is tracked via /etc/uyuni-image-ref (written at build time)
# and a persisted copy at $PGDATA/.uyuni-image-ref.

set -Eeo pipefail

UPSTREAM_ENTRYPOINT="/usr/local/bin/docker-entrypoint.sh"
IMAGE_REF_FILE="/etc/uyuni-image-ref"
PGDATA="${PGDATA:-/var/lib/pgsql/data}"
UPGDB_DIR="/docker-entrypoint-upgdb.d"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Returns 0 (true) when the postgres data directory already contains a cluster.
db_already_exists() {
    [ -s "$PGDATA/PG_VERSION" ]
}

# Returns the current image reference, or "unknown" if the file is missing.
current_image_ref() {
    if [ -s "$IMAGE_REF_FILE" ]; then
        cat "$IMAGE_REF_FILE"
    else
        echo "unknown"
    fi
}

# Returns the image reference that was recorded on the last container start.
stored_image_ref() {
    local ref_path="$PGDATA/.uyuni-image-ref"
    if [ -s "$ref_path" ]; then
        cat "$ref_path"
    else
        echo ""
    fi
}

# Persist the current image reference into the data directory so we can
# detect a change on the next run.
save_image_ref() {
    local ref_path="$PGDATA/.uyuni-image-ref"
    current_image_ref > "$ref_path"
    echo "Stored image ref: $(cat "$ref_path")"
}

# ---------------------------------------------------------------------------
# Upgrade script runner
# Mirrors the logic of docker_process_init_files from the upstream script.
# ---------------------------------------------------------------------------

run_upgrade_scripts() {
    if [ ! -d "$UPGDB_DIR" ]; then
        echo "No $UPGDB_DIR directory found, skipping upgrade scripts."
        return
    fi

    local files=()
    while IFS= read -r -d '' f; do
        files+=("$f")
    done < <(find "$UPGDB_DIR" -maxdepth 1 -type f -print0 | sort -z)

    if [ "${#files[@]}" -eq 0 ]; then
        echo "No upgrade scripts found in $UPGDB_DIR, nothing to do."
        return
    fi

    echo "Starting temporary postgres server for upgrade scripts..."
    PGUSER="${PGUSER:-${POSTGRES_USER:-postgres}}" \
        pg_ctl -D "$PGDATA" -o "-c listen_addresses='' -p ${PGPORT:-5432}" -w start

    local f
    for f in "${files[@]}"; do
        case "$f" in
            *.sh)
                if [ -x "$f" ]; then
                    echo "Running upgrade script: $f"
                    "$f"
                else
                    echo "Sourcing upgrade script: $f"
                    . "$f"
                fi
                ;;
            *.sql)
                echo "Running SQL upgrade script: $f"
                psql -v ON_ERROR_STOP=1 \
                     --username "${POSTGRES_USER:-postgres}" \
                     --no-password --no-psqlrc \
                     ${POSTGRES_DB:+--dbname "$POSTGRES_DB"} \
                     -f "$f"
                ;;
            *)
                echo "Ignoring unrecognised file: $f"
                ;;
        esac
    done

    echo "Stopping temporary postgres server after upgrade scripts..."
    PGUSER="${PGUSER:-postgres}" pg_ctl -D "$PGDATA" -m fast -w stop
}

log() {
    echo "[ENTRYPOINT] $*" >&2
}

main() {
    if [ $# -eq 0 ]; then set -- postgres; fi
    if [ "${1:0:1}" = '-' ]; then set -- postgres "$@"; fi

    if [ "$(id -u)" = '0' ]; then
        log "Running as root, dropping privileges to postgres..."
        if command -v setpriv > /dev/null; then
            exec setpriv --reuid=postgres --regid=postgres --clear-groups -- "$BASH_SOURCE" "$@"
        elif command -v runuser > /dev/null; then
            exec runuser -u postgres -- "$BASH_SOURCE" "$@"
        else
            exec su -p -s /bin/bash postgres -c "$BASH_SOURCE $*"
        fi
    fi

    if [ "$1" != 'postgres' ]; then
        exec "$@"
    fi

    if ! db_already_exists; then
        log "No existing database found – running initial setup."
        mkdir -p "$PGDATA"
        log "Delegating to upstream entrypoint: $UPSTREAM_ENTRYPOINT"
        exec "$UPSTREAM_ENTRYPOINT" "$@"
    fi

    local current stored
    current="$(current_image_ref)"
    stored="$(stored_image_ref)"

    if [ -z "$stored" ]; then
        # The DB exists, but our tracking file doesn't (e.g., first restart after a fresh
        # install, or attaching to a legacy database). Run upgrade scripts once to ensure
        # required DB objects are present, then persist the marker for future comparisons.
        log "No tracking file found. Running upgrade scripts for compatibility bootstrap."
        export PGPASSWORD="${PGPASSWORD:-${POSTGRES_PASSWORD:-}}"
        run_upgrade_scripts
        unset PGPASSWORD
        save_image_ref

    elif [ "$current" != "$stored" ]; then
        log "Image changed: '$stored' → '$current'. Running upgrade scripts..."
        export PGPASSWORD="${PGPASSWORD:-${POSTGRES_PASSWORD:-}}"
        run_upgrade_scripts
        unset PGPASSWORD
        save_image_ref
        log "Upgrade complete."
    else
        log "Image unchanged ($current). Starting normally."
    fi

    exec "$@"
}

main "$@"
