#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/detect_pg_capabilities.sh [--session-dir <path>]

Detect PostgreSQL version/capabilities and optionally enable instrumentation toggles.
USAGE
}

SESSION_DIR=""
while [[ $# -gt 0 ]]; do
  case "$1" in
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

phase3_load_env
require_cmd kubectl jq

if [[ -z "$SESSION_DIR" ]]; then
  SESSION_DIR="$(phase3_new_session_dir "$PHASE3_ROOT/results" "pg-capabilities" "$STORAGE_CLASS")"
fi

mkdir -p "$SESSION_DIR/sql"
LOG_FILE="$SESSION_DIR/sql/detect-pg-capabilities.log"
CAP_JSON="$SESSION_DIR/pg-capabilities.json"
BEFORE_SETTINGS="$SESSION_DIR/sql/pg-settings-before.tsv"
AFTER_SETTINGS="$SESSION_DIR/sql/pg-settings-after.tsv"

scalar() {
  local q="$1"
  run_psql_tsv postgres "$q" | tr -d '[:space:]'
}

snapshot_settings() {
  local out="$1"
  run_psql_tsv postgres "SELECT name,setting,source FROM pg_settings WHERE name IN (
    'shared_preload_libraries','compute_query_id','track_io_timing','track_wal_io_timing'
  ) ORDER BY name;" > "$out"
}

restart_db_deployment() {
  local db_deploy
  db_deploy="$(kubectl -n "$UYUNI_NAMESPACE" get deploy -o json | jq -r '.items[].metadata.name' | grep -E '^(db|postgres|pgsql)' | head -n1 || true)"
  [[ -n "$db_deploy" ]] || die "Could not detect DB deployment for safe restart"

  log "Restarting DB deployment: $db_deploy"
  kubectl -n "$UYUNI_NAMESPACE" rollout restart deploy/"$db_deploy"
  kubectl -n "$UYUNI_NAMESPACE" rollout status deploy/"$db_deploy" --timeout=10m
}

{
  log "Detecting PostgreSQL capabilities"

  snapshot_settings "$BEFORE_SETTINGS"

  server_version_num="$(scalar "SHOW server_version_num;")"
  [[ -n "$server_version_num" ]] || die "Unable to read server_version_num"

  has_pg_stat_wal="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_wal') IS NOT NULL)::int;")"
  has_pg_stat_io="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_io') IS NOT NULL)::int;")"
  has_pg_stat_checkpointer="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_checkpointer') IS NOT NULL)::int;")"
  has_pg_stat_statements_ext="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname='pg_stat_statements')::int;")"

  has_track_io_setting="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_settings WHERE name='track_io_timing')::int;")"
  has_track_wal_io_setting="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_settings WHERE name='track_wal_io_timing')::int;")"

  before_track_io=""
  before_track_wal_io=""
  before_shared_preload=""
  before_compute_query_id=""

  if [[ "$has_track_io_setting" == "1" ]]; then
    before_track_io="$(scalar "SHOW track_io_timing;")"
  fi
  if [[ "$has_track_wal_io_setting" == "1" ]]; then
    before_track_wal_io="$(scalar "SHOW track_wal_io_timing;")"
  fi
  before_shared_preload="$(scalar "SHOW shared_preload_libraries;")"
  before_compute_query_id="$(scalar "SELECT setting FROM pg_settings WHERE name='compute_query_id';")"

  pg_stat_statements_was_enabled="0"
  pg_stat_statements_enabled_now="$has_pg_stat_statements_ext"

  if [[ "$ENABLE_PG_STAT_STATEMENTS" == "1" && "$has_pg_stat_statements_ext" == "0" ]]; then
    log "ENABLE_PG_STAT_STATEMENTS=1 and extension missing; enabling server settings"
    run_psql_cmd postgres "ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';"
    run_psql_cmd postgres "ALTER SYSTEM SET compute_query_id = 'auto';"
    restart_db_deployment

    # Ensure benchmark database exists before creating extension there.
    db_exists="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_database WHERE datname='${DB_BENCH_DB}')::int;")"
    if [[ "$db_exists" == "0" ]]; then
      run_psql_cmd postgres "CREATE DATABASE ${DB_BENCH_DB};"
    fi
    run_psql_cmd "$DB_BENCH_DB" "CREATE EXTENSION IF NOT EXISTS pg_stat_statements;"
    pg_stat_statements_enabled_now="$(scalar "SELECT EXISTS (SELECT 1 FROM pg_extension WHERE extname='pg_stat_statements')::int;")"
  elif [[ "$has_pg_stat_statements_ext" == "1" ]]; then
    pg_stat_statements_was_enabled="1"
  fi

  if [[ "$has_track_io_setting" == "1" ]]; then
    run_psql_cmd postgres "ALTER SYSTEM SET track_io_timing = on;"
  fi
  if [[ "$has_track_wal_io_setting" == "1" ]]; then
    run_psql_cmd postgres "ALTER SYSTEM SET track_wal_io_timing = on;"
  fi

  if [[ "$has_track_io_setting" == "1" || "$has_track_wal_io_setting" == "1" ]]; then
    run_psql_cmd postgres "SELECT pg_reload_conf();"
  fi

  after_track_io=""
  after_track_wal_io=""
  if [[ "$has_track_io_setting" == "1" ]]; then
    after_track_io="$(scalar "SHOW track_io_timing;")"
  fi
  if [[ "$has_track_wal_io_setting" == "1" ]]; then
    after_track_wal_io="$(scalar "SHOW track_wal_io_timing;")"
  fi

  after_shared_preload="$(scalar "SHOW shared_preload_libraries;")"
  after_compute_query_id="$(scalar "SELECT setting FROM pg_settings WHERE name='compute_query_id';")"

  # Re-evaluate version-gated views after potential restart/config updates.
  has_pg_stat_io_now="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_io') IS NOT NULL)::int;")"
  has_pg_stat_checkpointer_now="$(scalar "SELECT (to_regclass('pg_catalog.pg_stat_checkpointer') IS NOT NULL)::int;")"

  snapshot_settings "$AFTER_SETTINGS"

  jq -n \
    --arg generated_at "$(now_utc)" \
    --arg session_dir "$SESSION_DIR" \
    --arg storage_class "$STORAGE_CLASS" \
    --argjson server_version_num "$server_version_num" \
    --argjson has_pg_stat_wal "$has_pg_stat_wal" \
    --argjson has_pg_stat_io "$has_pg_stat_io_now" \
    --argjson has_pg_stat_checkpointer "$has_pg_stat_checkpointer_now" \
    --argjson has_pg_stat_statements "$pg_stat_statements_enabled_now" \
    --argjson has_track_io_setting "$has_track_io_setting" \
    --argjson has_track_wal_io_setting "$has_track_wal_io_setting" \
    --arg before_track_io "$before_track_io" \
    --arg after_track_io "$after_track_io" \
    --arg before_track_wal_io "$before_track_wal_io" \
    --arg after_track_wal_io "$after_track_wal_io" \
    --arg before_shared_preload "$before_shared_preload" \
    --arg after_shared_preload "$after_shared_preload" \
    --arg before_compute_query_id "$before_compute_query_id" \
    --arg after_compute_query_id "$after_compute_query_id" \
    --arg before_settings_file "$BEFORE_SETTINGS" \
    --arg after_settings_file "$AFTER_SETTINGS" \
    '{
      generated_at_utc: $generated_at,
      session_dir: $session_dir,
      storage_class: $storage_class,
      server_version_num: $server_version_num,
      capabilities: {
        pg_stat_wal: ($has_pg_stat_wal == 1),
        pg_stat_io: ($has_pg_stat_io == 1),
        pg_stat_checkpointer: ($has_pg_stat_checkpointer == 1),
        pg_stat_statements: ($has_pg_stat_statements == 1),
        track_io_timing_setting: ($has_track_io_setting == 1),
        track_wal_io_timing_setting: ($has_track_wal_io_setting == 1)
      },
      settings_before: {
        track_io_timing: $before_track_io,
        track_wal_io_timing: $before_track_wal_io,
        shared_preload_libraries: $before_shared_preload,
        compute_query_id: $before_compute_query_id
      },
      settings_after: {
        track_io_timing: $after_track_io,
        track_wal_io_timing: $after_track_wal_io,
        shared_preload_libraries: $after_shared_preload,
        compute_query_id: $after_compute_query_id
      },
      setting_snapshots: {
        before: $before_settings_file,
        after: $after_settings_file
      },
      expectations: {
        pg_stat_io_expected_by_version: ($server_version_num >= 160000),
        pg_stat_checkpointer_expected_by_version: ($server_version_num >= 170000)
      }
    }' > "$CAP_JSON"

  log "Wrote capability file: $CAP_JSON"
} | tee "$LOG_FILE"
