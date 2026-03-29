#!/usr/bin/env bash

# Shared helpers for benchmark-phase3 scripts.

PHASE3_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

log() {
  printf '[%s] %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*"
}

warn() {
  printf '[%s] WARN: %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*" >&2
}

die() {
  printf '[%s] ERROR: %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$*" >&2
  exit 1
}

require_cmd() {
  local c
  for c in "$@"; do
    command -v "$c" >/dev/null 2>&1 || die "Required command not found: $c"
  done
}

now_utc() {
  date -u +%Y-%m-%dT%H:%M:%SZ
}

now_epoch() {
  date -u +%s
}

csv_to_lines() {
  # Convert comma-separated values into newline-separated values.
  # Trims surrounding spaces around each element.
  echo "$1" | tr ',' '\n' | sed -E 's/^\s+//; s/\s+$//' | sed '/^$/d'
}

append_csv_row() {
  # Usage: append_csv_row <csv_path> <field1> <field2> ...
  # Writes one properly quoted CSV row so commas in fields do not corrupt parsing.
  local csv_path="$1"
  shift
  python3 - "$csv_path" "$@" <<'PY'
from __future__ import annotations

import csv
import sys

path = sys.argv[1]
row = sys.argv[2:]

with open(path, "a", encoding="utf-8", newline="") as fh:
    csv.writer(fh).writerow(row)
PY
}

phase3_load_env() {
  local env_file
  env_file="${PHASE3_ENV_FILE:-$PHASE3_ROOT/config/phase3.env}"

  if [[ -f "$env_file" ]]; then
    # shellcheck disable=SC1090
    set -a && . "$env_file" && set +a
    log "Loaded env file: $env_file"
  else
    warn "Env file not found at $env_file, using defaults and current environment"
  fi

  : "${KUBECONFIG:=/etc/rancher/rke2/rke2.yaml}"
  : "${UYUNI_NAMESPACE:=uyuni}"
  : "${BENCH_NAMESPACE:=default}"
  : "${MONITORING_NAMESPACE:=monitoring}"
  : "${UYUNI_FQDN:=uyuni.home.arpa}"
  : "${STORAGE_CLASS:=local-path}"

  : "${DB_HOST:=db.uyuni.svc.cluster.local}"
  : "${DB_PORT:=5432}"
  : "${DB_ADMIN_USER:=postgres}"
  : "${DB_ADMIN_PASS:=}"
  : "${DB_BENCH_DB:=pgbench_bench}"

  : "${RUNS:=3}"
  : "${PGCLIENT_IMAGE:=postgres:16}"
  : "${PROM_URL:=http://prometheus-operated.monitoring.svc:9090}"

  : "${LOCUST_USERS:=5,10,20}"
  : "${LOCUST_SPAWN_RATE:=2}"
  : "${LOCUST_DURATION:=120}"

  : "${PGBENCH_DURATION:=60}"
  : "${PGBENCH_SCALE_SMOKE:=10}"
  : "${PGBENCH_SCALE_STORAGE:=50}"
  : "${PGBENCH_CLIENTS:=1,4,8,16}"

  : "${PACKAGE_DOWNLOAD_CONCURRENCY:=1,4,8}"
  : "${PACKAGE_DOWNLOAD_LIMIT:=5}"

  : "${ENABLE_PG_STAT_STATEMENTS:=0}"
  : "${UYUNI_GUI_USER:=admin}"
  : "${UYUNI_GUI_PASS:=admin}"
  : "${PGBENCH_WARMUP:=0}"

  export KUBECONFIG
}

phase3_new_session_dir() {
  local outdir="${1:-$PHASE3_ROOT/results}"
  local profile="${2:-adhoc}"
  local storage="${3:-$STORAGE_CLASS}"
  local ts
  ts="$(date -u +%Y%m%dT%H%M%SZ)"
  mkdir -p "$outdir"
  echo "$outdir/session-${ts}-${profile}-${storage}"
}

phase3_save_metadata_snapshot() {
  local session_dir="$1"
  mkdir -p "$session_dir/metadata"

  {
    echo "session_dir=$session_dir"
    echo "snapshot_time_utc=$(now_utc)"
    echo "storage_class=$STORAGE_CLASS"
    echo "uyuni_namespace=$UYUNI_NAMESPACE"
    echo "bench_namespace=$BENCH_NAMESPACE"
    echo "monitoring_namespace=$MONITORING_NAMESPACE"
    echo "db_host=$DB_HOST"
    echo "db_port=$DB_PORT"
    echo "prom_url=$PROM_URL"
  } >"$session_dir/metadata/session-env.txt"

  env | sort >"$session_dir/metadata/full-environment.txt"
  kubectl get nodes -o wide >"$session_dir/metadata/kubectl-nodes.txt"
  kubectl get sc -o wide >"$session_dir/metadata/kubectl-storageclasses.txt"
  kubectl get sc "$STORAGE_CLASS" -o yaml >"$session_dir/metadata/storageclass-${STORAGE_CLASS}.yaml"
  kubectl -n "$UYUNI_NAMESPACE" get pods -o wide >"$session_dir/metadata/kubectl-uyuni-pods.txt"
  kubectl -n "$UYUNI_NAMESPACE" get svc -o wide >"$session_dir/metadata/kubectl-uyuni-services.txt"
  kubectl -n "$UYUNI_NAMESPACE" get pvc -o wide >"$session_dir/metadata/kubectl-uyuni-pvcs.txt"
}

get_db_pod() {
  local pod
  pod="$(kubectl -n "$UYUNI_NAMESPACE" get pods -o name | sed 's#^pod/##' | grep -E '^db-' | head -n1 || true)"
  [[ -n "$pod" ]] || die "Could not detect DB pod in namespace $UYUNI_NAMESPACE"
  echo "$pod"
}

get_uyuni_pod() {
  local pod
  pod="$(kubectl -n "$UYUNI_NAMESPACE" get pods -o name | sed 's#^pod/##' | grep -E '^uyuni-' | grep -v 'setup' | head -n1 || true)"
  [[ -n "$pod" ]] || die "Could not detect Uyuni application pod in namespace $UYUNI_NAMESPACE"
  echo "$pod"
}

get_micro_client_pod() {
  local pod
  pod="$(kubectl -n "$UYUNI_NAMESPACE" get pods -o name | sed 's#^pod/##' | grep -E '^micro-client-' | head -n1 || true)"
  [[ -n "$pod" ]] || die "Could not detect micro-client pod in namespace $UYUNI_NAMESPACE"
  echo "$pod"
}

run_psql_cmd() {
  # Usage: run_psql_cmd <db_name> <sql>
  local db_name="$1"
  local sql="$2"
  local db_pod
  db_pod="$(get_db_pod)"

  if [[ -n "$DB_ADMIN_PASS" ]]; then
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- env PGPASSWORD="$DB_ADMIN_PASS" \
      psql -X -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$db_name" -c "$sql"
  else
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- \
      psql -X -v ON_ERROR_STOP=1 -U "$DB_ADMIN_USER" -d "$db_name" -c "$sql"
  fi
}

run_psql_file() {
  # Usage: run_psql_file <db_name> <sql_file>
  local db_name="$1"
  local sql_file="$2"
  local db_pod
  db_pod="$(get_db_pod)"

  if [[ -n "$DB_ADMIN_PASS" ]]; then
    kubectl -n "$UYUNI_NAMESPACE" exec -i "$db_pod" -- env PGPASSWORD="$DB_ADMIN_PASS" \
      psql -X -v ON_ERROR_STOP=1 -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$db_name" -f - <"$sql_file"
  else
    kubectl -n "$UYUNI_NAMESPACE" exec -i "$db_pod" -- \
      psql -X -v ON_ERROR_STOP=1 -U "$DB_ADMIN_USER" -d "$db_name" -f - <"$sql_file"
  fi
}

run_psql_tsv() {
  # Usage: run_psql_tsv <db_name> <sql>
  local db_name="$1"
  local sql="$2"
  local db_pod
  db_pod="$(get_db_pod)"

  if [[ -n "$DB_ADMIN_PASS" ]]; then
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- env PGPASSWORD="$DB_ADMIN_PASS" \
      psql -X -v ON_ERROR_STOP=1 -A -t -F $'\t' -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$db_name" -c "$sql"
  else
    kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- \
      psql -X -v ON_ERROR_STOP=1 -A -t -F $'\t' -U "$DB_ADMIN_USER" -d "$db_name" -c "$sql"
  fi
}

run_pgbench_cmd() {
  # Usage: run_pgbench_cmd <db_name> <extra pgbench args>
  local db_name="$1"
  shift
  local db_pod
  db_pod="$(get_db_pod)"

  if kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- sh -lc 'command -v pgbench >/dev/null 2>&1' >/dev/null 2>&1; then
    if [[ -n "$DB_ADMIN_PASS" ]]; then
      kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- env PGPASSWORD="$DB_ADMIN_PASS" \
        pgbench -h "$DB_HOST" -p "$DB_PORT" -U "$DB_ADMIN_USER" -d "$db_name" "$@"
    else
      kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- \
        pgbench -U "$DB_ADMIN_USER" -d "$db_name" "$@"
    fi
  else
    run_pgbench_via_pgclient "$db_name" "$@"
  fi
}

check_pgbench_available() {
  local db_pod
  db_pod="$(get_db_pod)"
  if kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- sh -lc 'command -v pgbench >/dev/null 2>&1' >/dev/null 2>&1; then
    return 0
  fi

  local pod="phase3-pgbench-check-$(date -u +%H%M%S)"
  kubectl -n "$BENCH_NAMESPACE" run "$pod" \
    --image="$PGCLIENT_IMAGE" \
    --restart=Never \
    --command -- sh -lc 'command -v pgbench >/dev/null 2>&1' >/dev/null

  # Wait up to 120s for pod to reach terminal phase.
  local phase=""
  local i
  for i in $(seq 1 60); do
    phase="$(kubectl -n "$BENCH_NAMESPACE" get pod "$pod" -o jsonpath='{.status.phase}' 2>/dev/null || true)"
    if [[ "$phase" == "Succeeded" || "$phase" == "Failed" ]]; then
      break
    fi
    sleep 2
  done

  local exit_code
  exit_code="$(kubectl -n "$BENCH_NAMESPACE" get pod "$pod" -o jsonpath='{.status.containerStatuses[0].state.terminated.exitCode}' 2>/dev/null || echo "1")"
  kubectl -n "$BENCH_NAMESPACE" delete pod "$pod" --ignore-not-found=true >/dev/null 2>&1 || true

  [[ "$exit_code" == "0" ]]
}

resolve_db_admin_pass_if_empty() {
  if [[ -n "$DB_ADMIN_PASS" ]]; then
    return 0
  fi

  local db_pod
  db_pod="$(get_db_pod)"

  local candidate
  for var_name in POSTGRES_PASSWORD POSTGRESQL_PASSWORD DB_ADMIN_PASS; do
    candidate="$(kubectl -n "$UYUNI_NAMESPACE" exec "$db_pod" -- sh -lc "printf '%s' \"\${$var_name:-}\"" 2>/dev/null || true)"
    if [[ -n "$candidate" ]]; then
      DB_ADMIN_PASS="$candidate"
      export DB_ADMIN_PASS
      return 0
    fi
  done

  return 1
}

run_pgbench_via_pgclient() {
  local db_name="$1"
  shift

  resolve_db_admin_pass_if_empty || die "DB_ADMIN_PASS is required for pgbench fallback via PGCLIENT_IMAGE"

  local pod="phase3-pgbench-$(date -u +%H%M%S)-$RANDOM"
  local -a pb_args=()
  local idx=1
  local arg_count="$#"

  # Create an idle pod first so custom pgbench scripts can be copied inside.
  kubectl -n "$BENCH_NAMESPACE" run "$pod" \
    --image="$PGCLIENT_IMAGE" \
    --restart=Never \
    --env="PGPASSWORD=$DB_ADMIN_PASS" \
    --command -- sh -lc 'sleep 3600' >/dev/null

  local phase=""
  local i
  for i in $(seq 1 120); do
    phase="$(kubectl -n "$BENCH_NAMESPACE" get pod "$pod" -o jsonpath='{.status.phase}' 2>/dev/null || true)"
    if [[ "$phase" == "Running" ]]; then
      break
    fi
    if [[ "$phase" == "Failed" || "$phase" == "Succeeded" ]]; then
      kubectl -n "$BENCH_NAMESPACE" logs "$pod" || true
      kubectl -n "$BENCH_NAMESPACE" delete pod "$pod" --ignore-not-found=true >/dev/null 2>&1 || true
      die "Temporary pgbench client pod terminated before becoming ready: $pod (phase=$phase)"
    fi
    sleep 1
  done
  [[ "$phase" == "Running" ]] || die "Timed out waiting for pgbench client pod: $pod"

  # Rewrite local script paths passed via -f so they exist inside the pod.
  while [[ "$idx" -le "$arg_count" ]]; do
    local cur="${!idx}"
    if [[ "$cur" == "-f" ]]; then
      idx=$((idx + 1))
      local src="${!idx}"
      [[ -f "$src" ]] || die "pgbench script not found: $src"
      local remote="/tmp/$(basename "$src")"
      kubectl -n "$BENCH_NAMESPACE" cp "$src" "$pod:$remote" >/dev/null
      pb_args+=("-f" "$remote")
    elif [[ "$cur" == -f* && "$cur" != "-f" ]]; then
      local src_inline="${cur#-f}"
      [[ -f "$src_inline" ]] || die "pgbench script not found: $src_inline"
      local remote_inline="/tmp/$(basename "$src_inline")"
      kubectl -n "$BENCH_NAMESPACE" cp "$src_inline" "$pod:$remote_inline" >/dev/null
      pb_args+=("-f$remote_inline")
    else
      pb_args+=("$cur")
    fi
    idx=$((idx + 1))
  done

  local args_quoted
  args_quoted="$(printf '%q ' "${pb_args[@]}")"

  local exit_code=0
  kubectl -n "$BENCH_NAMESPACE" exec "$pod" -- \
    sh -lc "pgbench -h '$DB_HOST' -p '$DB_PORT' -U '$DB_ADMIN_USER' -d '$db_name' $args_quoted" || exit_code=$?

  kubectl -n "$BENCH_NAMESPACE" delete pod "$pod" --ignore-not-found=true >/dev/null 2>&1 || true
  [[ "$exit_code" == "0" ]]
}
