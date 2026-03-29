#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/collect_sql_snapshot.sh \
  --phase pre|post \
  --run-id <id> \
  --session-dir <path> \
  [--db <dbname>]
USAGE
}

PHASE=""
RUN_ID=""
SESSION_DIR=""
DB_NAME=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --phase)
      PHASE="$2"
      shift 2
      ;;
    --run-id)
      RUN_ID="$2"
      shift 2
      ;;
    --session-dir)
      SESSION_DIR="$2"
      shift 2
      ;;
    --db)
      DB_NAME="$2"
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

[[ "$PHASE" == "pre" || "$PHASE" == "post" ]] || die "--phase must be pre or post"
[[ -n "$RUN_ID" ]] || die "--run-id is required"
[[ -n "$SESSION_DIR" ]] || die "--session-dir is required"

phase3_load_env
require_cmd kubectl jq

if [[ -z "$DB_NAME" ]]; then
  DB_NAME="$DB_BENCH_DB"
fi

mkdir -p "$SESSION_DIR/sql"
TXT_OUT="$SESSION_DIR/sql/${PHASE}-sql-${RUN_ID}.txt"
JSON_OUT="$SESSION_DIR/sql/${PHASE}-sql-${RUN_ID}.json"
SETTINGS_CSV="$SESSION_DIR/sql/${PHASE}-settings-${RUN_ID}.csv"
DBSTAT_CSV="$SESSION_DIR/sql/${PHASE}-pg-stat-database-${RUN_ID}.csv"

SQL_FILE="$PHASE3_ROOT/sql/${PHASE}_snapshot.sql"
[[ -f "$SQL_FILE" ]] || die "Snapshot SQL file not found: $SQL_FILE"

# Text snapshot from canonical SQL file.
run_psql_file "$DB_NAME" "$SQL_FILE" > "$TXT_OUT"

# Additional CSV exports for machine-processing convenience.
run_psql_tsv "$DB_NAME" "COPY (
  SELECT name, setting, unit, source
  FROM pg_settings
  WHERE name IN (
    'shared_buffers','effective_cache_size','effective_io_concurrency','random_page_cost',
    'synchronous_commit','wal_level','wal_buffers','max_wal_size','min_wal_size',
    'checkpoint_timeout','checkpoint_completion_target','track_io_timing',
    'track_wal_io_timing','shared_preload_libraries'
  )
  ORDER BY name
) TO STDOUT WITH CSV HEADER;" > "$SETTINGS_CSV"

run_psql_tsv "$DB_NAME" "COPY (
  SELECT datname,numbackends,xact_commit,xact_rollback,blks_read,blks_hit,
         tup_returned,tup_fetched,tup_inserted,tup_updated,tup_deleted,
         temp_files,temp_bytes,deadlocks,blk_read_time,blk_write_time,stats_reset
  FROM pg_stat_database
  ORDER BY datname
) TO STDOUT WITH CSV HEADER;" > "$DBSTAT_CSV"

# JSON helpers.
scalar() {
  local q="$1"
  run_psql_tsv "$DB_NAME" "$q" | tr -d '[:space:]'
}

psql_json_array() {
  local q="$1"
  local db_pod
  db_pod="$(get_db_pod)"
  local wrapped="SELECT COALESCE(json_agg(t), '[]'::json)::text FROM (${q}) t;"

  if [[ -n "$DB_ADMIN_PASS" ]]; then
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- env PGPASSWORD="$DB_ADMIN_PASS" \
      psql -X -v ON_ERROR_STOP=1 -A -t -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$DB_NAME" -c "$wrapped"
  else
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- \
      psql -X -v ON_ERROR_STOP=1 -A -t -U "$DB_ADMIN_USER" -d "$DB_NAME" -c "$wrapped"
  fi
}

server_version_num="$(scalar "SHOW server_version_num;")"
has_pg_stat_wal="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_wal') IS NOT NULL)::int;")"
has_pg_stat_io="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_io') IS NOT NULL)::int;")"
has_pg_stat_checkpointer="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_checkpointer') IS NOT NULL)::int;")"
has_pg_stat_statements="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname='pg_stat_statements')::int;")"

settings_json="$(psql_json_array "SELECT name,setting,unit,source FROM pg_settings WHERE name IN (
  'shared_buffers','effective_cache_size','effective_io_concurrency','random_page_cost',
  'synchronous_commit','wal_level','wal_buffers','max_wal_size','min_wal_size',
  'checkpoint_timeout','checkpoint_completion_target','track_io_timing','track_wal_io_timing',
  'shared_preload_libraries'
) ORDER BY name")"

dbstats_json="$(psql_json_array "SELECT datname,numbackends,xact_commit,xact_rollback,blks_read,blks_hit,
  tup_returned,tup_fetched,tup_inserted,tup_updated,tup_deleted,temp_files,temp_bytes,
  deadlocks,blk_read_time,blk_write_time,stats_reset
  FROM pg_stat_database ORDER BY datname")"

pg_stat_wal_json='[]'
pg_stat_io_json='[]'
pg_stat_checkpointer_json='[]'
pg_stat_statements_json='[]'

if [[ "$has_pg_stat_wal" == "1" ]]; then
  pg_stat_wal_json="$(psql_json_array "SELECT * FROM pg_catalog.pg_stat_wal")"
fi
if [[ "$has_pg_stat_io" == "1" ]]; then
  pg_stat_io_json="$(psql_json_array "SELECT * FROM pg_catalog.pg_stat_io")"
fi
if [[ "$has_pg_stat_checkpointer" == "1" ]]; then
  pg_stat_checkpointer_json="$(psql_json_array "SELECT * FROM pg_catalog.pg_stat_checkpointer")"
fi
if [[ "$has_pg_stat_statements" == "1" ]]; then
  pg_stat_statements_json="$(psql_json_array "SELECT queryid,calls,total_exec_time,mean_exec_time,rows,
    shared_blks_hit,shared_blks_read,temp_blks_written,wal_records,wal_fpi,wal_bytes,
    left(regexp_replace(query, E'[\\n\\r\\t ]+', ' ', 'g'), 200) AS query_sample
    FROM pg_stat_statements ORDER BY total_exec_time DESC LIMIT 20")"
fi

wal_insert_lsn="$(scalar "SELECT pg_current_wal_insert_lsn();")"
wal_flush_lsn="$(scalar "SELECT pg_current_wal_flush_lsn();")"

jq -n \
  --arg phase "$PHASE" \
  --arg run_id "$RUN_ID" \
  --arg snapshot_time_utc "$(now_utc)" \
  --arg db_name "$DB_NAME" \
  --arg text_file "$TXT_OUT" \
  --arg settings_csv "$SETTINGS_CSV" \
  --arg pg_stat_database_csv "$DBSTAT_CSV" \
  --argjson server_version_num "$server_version_num" \
  --arg wal_insert_lsn "$wal_insert_lsn" \
  --arg wal_flush_lsn "$wal_flush_lsn" \
  --argjson has_pg_stat_wal "$has_pg_stat_wal" \
  --argjson has_pg_stat_io "$has_pg_stat_io" \
  --argjson has_pg_stat_checkpointer "$has_pg_stat_checkpointer" \
  --argjson has_pg_stat_statements "$has_pg_stat_statements" \
  --argjson settings "$settings_json" \
  --argjson pg_stat_database "$dbstats_json" \
  --argjson pg_stat_wal "$pg_stat_wal_json" \
  --argjson pg_stat_io "$pg_stat_io_json" \
  --argjson pg_stat_checkpointer "$pg_stat_checkpointer_json" \
  --argjson pg_stat_statements_top20 "$pg_stat_statements_json" \
  '{
    phase: $phase,
    run_id: $run_id,
    snapshot_time_utc: $snapshot_time_utc,
    db_name: $db_name,
    server_version_num: $server_version_num,
    wal_insert_lsn: $wal_insert_lsn,
    wal_flush_lsn: $wal_flush_lsn,
    availability: {
      pg_stat_wal: ($has_pg_stat_wal == 1),
      pg_stat_io: ($has_pg_stat_io == 1),
      pg_stat_checkpointer: ($has_pg_stat_checkpointer == 1),
      pg_stat_statements: ($has_pg_stat_statements == 1)
    },
    selected_settings: $settings,
    pg_stat_database: $pg_stat_database,
    pg_stat_wal: $pg_stat_wal,
    pg_stat_io: $pg_stat_io,
    pg_stat_checkpointer: $pg_stat_checkpointer,
    pg_stat_statements_top20: $pg_stat_statements_top20,
    artifacts: {
      text: $text_file,
      settings_csv: $settings_csv,
      pg_stat_database_csv: $pg_stat_database_csv
    }
  }' > "$JSON_OUT"

log "Collected SQL snapshot: $TXT_OUT"
log "Collected SQL snapshot JSON: $JSON_OUT"
