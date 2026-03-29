#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/prepare_pgbench_db.sh --scale <n> [--recreate] [--session-dir <path>]

Creates/initializes dedicated pgbench benchmark database.
USAGE
}

SCALE=""
RECREATE=0
SESSION_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --scale)
      SCALE="$2"
      shift 2
      ;;
    --recreate)
      RECREATE=1
      shift
      ;;
    --session-dir)
      SESSION_DIR="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      die "Unknown argument: $1"
      ;;
  esac
done

[[ -n "$SCALE" ]] || die "--scale is required"

phase3_load_env
[[ "$DB_BENCH_DB" != "susemanager" ]] || die "DB_BENCH_DB must not be susemanager"
require_cmd kubectl jq
check_pgbench_available || die "pgbench binary is not available in DB pod"

if [[ -z "$SESSION_DIR" ]]; then
  SESSION_DIR="$(phase3_new_session_dir "$PHASE3_ROOT/results" "pgbench-prepare" "$STORAGE_CLASS")"
fi
mkdir -p "$SESSION_DIR/pgbench"

INIT_LOG="$SESSION_DIR/pgbench/pgbench-init-scale-${SCALE}.log"
DB_LIST_FILE="$SESSION_DIR/pgbench/database-list.tsv"
DB_SIZE_FILE="$SESSION_DIR/pgbench/database-size-summary.tsv"

exists_query="SELECT EXISTS (SELECT 1 FROM pg_database WHERE datname='${DB_BENCH_DB}')::int;"

db_exists="$(run_psql_tsv postgres "$exists_query" | tr -d '[:space:]')"

{
  log "Preparing benchmark DB: $DB_BENCH_DB (scale=$SCALE recreate=$RECREATE)"

  if [[ "$RECREATE" == "1" && "$db_exists" == "1" ]]; then
    log "Recreating benchmark database $DB_BENCH_DB"
    run_psql_cmd postgres "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${DB_BENCH_DB}' AND pid <> pg_backend_pid();"
    run_psql_cmd postgres "DROP DATABASE ${DB_BENCH_DB};"
    db_exists="0"
  fi

  if [[ "$db_exists" == "0" ]]; then
    run_psql_cmd postgres "CREATE DATABASE ${DB_BENCH_DB};"
  fi

  # Dedicated benchmark role; created only if missing.
  run_psql_cmd postgres "DO \$\$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'pgbench_runner') THEN
    CREATE ROLE pgbench_runner LOGIN;
  END IF;
END
\$\$;"

  run_psql_cmd postgres "GRANT CONNECT ON DATABASE ${DB_BENCH_DB} TO pgbench_runner;"

  # Initialize benchmark schema/data.
  run_pgbench_cmd "$DB_BENCH_DB" -i -s "$SCALE"

  run_psql_tsv postgres "SELECT datname, pg_get_userbyid(datdba) AS owner, datcollate, datctype FROM pg_database ORDER BY datname;" > "$DB_LIST_FILE"
  run_psql_tsv postgres "SELECT datname, pg_database_size(datname) AS bytes, pg_size_pretty(pg_database_size(datname)) AS pretty FROM pg_database ORDER BY pg_database_size(datname) DESC;" > "$DB_SIZE_FILE"

  log "Benchmark DB preparation complete"
  log "Database list: $DB_LIST_FILE"
  log "Database sizes: $DB_SIZE_FILE"
} | tee "$INIT_LOG"
