#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/run_pgbench_matrix.sh \
  [--profile smoke|full] [--scale N] [--clients 1,4,8] [--duration 60] [--runs 3] \
  --session-dir <path>

Scenarios run:
  - builtin_default
  - readonly_custom
  - wal_small_update (full profile only by default)
USAGE
}

PROFILE="smoke"
SCALE=""
CLIENTS=""
DURATION=""
RUN_COUNT=""
SESSION_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"
      shift 2
      ;;
    --scale)
      SCALE="$2"
      shift 2
      ;;
    --clients)
      CLIENTS="$2"
      shift 2
      ;;
    --duration)
      DURATION="$2"
      shift 2
      ;;
    --runs)
      RUN_COUNT="$2"
      shift 2
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

[[ -n "$SESSION_DIR" ]] || die "--session-dir is required"

phase3_load_env
require_cmd kubectl python3 jq
check_pgbench_available || die "pgbench binary is not available in DB pod"

mkdir -p "$SESSION_DIR/pgbench"
RUNS_CSV="$SESSION_DIR/pgbench/pgbench-runs.csv"
SUMMARY_CSV="$SESSION_DIR/pgbench/pgbench-summary.csv"
SUMMARY_MD="$SESSION_DIR/pgbench/pgbench-summary.md"

case "$PROFILE" in
  smoke)
    : "${SCALE:=$PGBENCH_SCALE_SMOKE}"
    : "${CLIENTS:=1,4,8}"
    : "${DURATION:=60}"
    : "${RUN_COUNT:=$RUNS}"
    SCENARIOS="builtin_default readonly_custom"
    ;;
  full)
    : "${SCALE:=$PGBENCH_SCALE_STORAGE}"
    : "${CLIENTS:=1,4,8,16}"
    : "${DURATION:=120}"
    : "${RUN_COUNT:=$RUNS}"
    SCENARIOS="builtin_default readonly_custom wal_small_update"
    ;;
  *)
    die "Unsupported profile: $PROFILE"
    ;;
esac

bash "$SCRIPT_DIR/prepare_pgbench_db.sh" --scale "$SCALE" --session-dir "$SESSION_DIR"

if [[ ! -f "$RUNS_CSV" ]]; then
  cat > "$RUNS_CSV" <<CSV
run_id,profile,scenario,clients,duration,scale,run_number,start_time_utc,end_time_utc,start_epoch,end_epoch,storage_class,namespace,db_name,tps,latency_ms,latency_stddev_ms,failures,aborted,exit_code,raw_log,pre_sql_json,post_sql_json,prom_summary_json,node_metadata
CSV
fi

parse_pgbench_output() {
  local raw_file="$1"
  local out_json="$2"

  python3 - "$raw_file" "$out_json" <<'PY'
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

raw_path = Path(sys.argv[1])
out_path = Path(sys.argv[2])
text = raw_path.read_text(encoding="utf-8", errors="replace")

def find_float(pattern: str) -> float | None:
    m = re.search(pattern, text, flags=re.IGNORECASE)
    if not m:
        return None
    try:
        return float(m.group(1))
    except Exception:
        return None

def find_int(pattern: str) -> int | None:
    m = re.search(pattern, text, flags=re.IGNORECASE)
    if not m:
        return None
    try:
        return int(m.group(1))
    except Exception:
        return None

payload = {
    "tps": find_float(r"tps\s*=\s*([0-9.]+)\s*\(without initial connection time\)"),
    "latency_ms": find_float(r"latency average\s*=\s*([0-9.]+)\s*ms"),
    "latency_stddev_ms": find_float(r"latency stddev\s*=\s*([0-9.]+)\s*ms"),
    "failures": find_int(r"number of failed transactions:\s*([0-9]+)"),
    "aborted": find_int(r"number of transactions aborted:\s*([0-9]+)"),
}
out_path.write_text(json.dumps(payload, indent=2), encoding="utf-8")
print(json.dumps(payload))
PY
}

for scenario in $SCENARIOS; do
  while IFS= read -r client; do
    [[ -n "$client" ]] || continue

    if [[ "$PGBENCH_WARMUP" == "1" ]]; then
      warmup_dir="$SESSION_DIR/pgbench/${scenario}/c${client}/warmup"
      mkdir -p "$warmup_dir"
      warmup_raw="$warmup_dir/pgbench-warmup.log"

      case "$scenario" in
        builtin_default)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 > "$warmup_raw" 2>&1 || true
          ;;
        readonly_custom)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 -f "$PHASE3_ROOT/sql/pgbench_readonly.sql" > "$warmup_raw" 2>&1 || true
          ;;
        wal_small_update)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 -f "$PHASE3_ROOT/sql/pgbench_wal_small_update.sql" > "$warmup_raw" 2>&1 || true
          ;;
      esac
    fi

    for run_n in $(seq 1 "$RUN_COUNT"); do
      run_id="${scenario}-c${client}-r${run_n}"
      run_dir="$SESSION_DIR/pgbench/${scenario}/c${client}/run${run_n}"
      mkdir -p "$run_dir"

      raw_log="$run_dir/pgbench-raw.log"
      parsed_json="$run_dir/pgbench-parsed.json"
      run_json="$run_dir/run-result.json"
      node_meta="$run_dir/node-metadata.txt"

      kubectl get nodes -o wide > "$node_meta"

      bash "$SCRIPT_DIR/collect_sql_snapshot.sh" --phase pre --run-id "$run_id" --session-dir "$SESSION_DIR" --db "$DB_BENCH_DB"
      pre_sql_json="$SESSION_DIR/sql/pre-sql-${run_id}.json"

      start_time_utc="$(now_utc)"
      start_epoch="$(now_epoch)"

      exit_code=0
      case "$scenario" in
        builtin_default)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 > "$raw_log" 2>&1 || exit_code=$?
          ;;
        readonly_custom)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 -f "$PHASE3_ROOT/sql/pgbench_readonly.sql" > "$raw_log" 2>&1 || exit_code=$?
          ;;
        wal_small_update)
          run_pgbench_cmd "$DB_BENCH_DB" -c "$client" -j "$client" -T "$DURATION" -P 10 -f "$PHASE3_ROOT/sql/pgbench_wal_small_update.sql" > "$raw_log" 2>&1 || exit_code=$?
          ;;
        *)
          echo "unknown scenario: $scenario" > "$raw_log"
          exit_code=1
          ;;
      esac

      end_time_utc="$(now_utc)"
      end_epoch="$(now_epoch)"

      bash "$SCRIPT_DIR/collect_sql_snapshot.sh" --phase post --run-id "$run_id" --session-dir "$SESSION_DIR" --db "$DB_BENCH_DB"
      post_sql_json="$SESSION_DIR/sql/post-sql-${run_id}.json"

      bash "$SCRIPT_DIR/collect_prometheus_range.sh" \
        --start-epoch "$start_epoch" \
        --end-epoch "$end_epoch" \
        --workload "pgbench-${scenario}" \
        --run-id "$run_id" \
        --session-dir "$SESSION_DIR"
      prom_summary_json="$SESSION_DIR/prometheus/pgbench-${scenario}-${run_id}-summary.json"

      parsed_payload="$(parse_pgbench_output "$raw_log" "$parsed_json")"
      tps="$(echo "$parsed_payload" | jq -r '.tps // empty')"
      latency_ms="$(echo "$parsed_payload" | jq -r '.latency_ms // empty')"
      latency_stddev_ms="$(echo "$parsed_payload" | jq -r '.latency_stddev_ms // empty')"
      failures="$(echo "$parsed_payload" | jq -r '.failures // 0')"
      aborted="$(echo "$parsed_payload" | jq -r '.aborted // 0')"

      jq -n \
        --arg run_id "$run_id" \
        --arg profile "$PROFILE" \
        --arg scenario "$scenario" \
        --argjson clients "$client" \
        --argjson duration "$DURATION" \
        --argjson scale "$SCALE" \
        --argjson run_number "$run_n" \
        --arg start_time_utc "$start_time_utc" \
        --arg end_time_utc "$end_time_utc" \
        --argjson start_epoch "$start_epoch" \
        --argjson end_epoch "$end_epoch" \
        --arg storage_class "$STORAGE_CLASS" \
        --arg namespace "$UYUNI_NAMESPACE" \
        --arg db_name "$DB_BENCH_DB" \
        --arg raw_log "$raw_log" \
        --arg pre_sql_json "$pre_sql_json" \
        --arg post_sql_json "$post_sql_json" \
        --arg prom_summary_json "$prom_summary_json" \
        --arg node_metadata "$node_meta" \
        --argjson parsed "$parsed_payload" \
        --argjson exit_code "$exit_code" \
        '{
          run_id: $run_id,
          workload_name: "pgbench",
          profile: $profile,
          scenario: $scenario,
          workload_parameters: {
            clients: $clients,
            duration_seconds: $duration,
            scale: $scale,
            run_number: $run_number
          },
          start_time_utc: $start_time_utc,
          end_time_utc: $end_time_utc,
          start_epoch: $start_epoch,
          end_epoch: $end_epoch,
          storage_class: $storage_class,
          namespace: $namespace,
          node_metadata_path: $node_metadata,
          db_name: $db_name,
          raw_output_path: $raw_log,
          sql_snapshots: {
            pre: $pre_sql_json,
            post: $post_sql_json
          },
          prometheus_summary_path: $prom_summary_json,
          parsed_results: $parsed,
          exit_code: $exit_code
        }' > "$run_json"

      append_csv_row "$RUNS_CSV" \
        "$run_id" "$PROFILE" "$scenario" "$client" "$DURATION" "$SCALE" "$run_n" \
        "$start_time_utc" "$end_time_utc" "$start_epoch" "$end_epoch" \
        "$STORAGE_CLASS" "$UYUNI_NAMESPACE" "$DB_BENCH_DB" \
        "${tps:-}" "${latency_ms:-}" "${latency_stddev_ms:-}" "$failures" "$aborted" "$exit_code" \
        "$raw_log" "$pre_sql_json" "$post_sql_json" "$prom_summary_json" "$node_meta"

      log "Completed pgbench run: $run_id"
    done
  done < <(csv_to_lines "$CLIENTS")
done

python3 - "$RUNS_CSV" "$SUMMARY_CSV" "$SUMMARY_MD" <<'PY'
from __future__ import annotations

import csv
import math
import statistics
import sys
from collections import defaultdict
from pathlib import Path

runs_csv = Path(sys.argv[1])
summary_csv = Path(sys.argv[2])
summary_md = Path(sys.argv[3])

rows = []
with runs_csv.open(encoding="utf-8") as fh:
    reader = csv.DictReader(fh)
    for r in reader:
        if r.get("exit_code") not in {"0", ""}:
            continue
        try:
            tps = float(r["tps"]) if r.get("tps") else math.nan
            lat = float(r["latency_ms"]) if r.get("latency_ms") else math.nan
        except Exception:
            continue
        rows.append((r["scenario"], r["clients"], tps, lat))

groups: dict[tuple[str, str], dict[str, list[float]]] = defaultdict(lambda: {"tps": [], "lat": []})
for scenario, clients, tps, lat in rows:
    if not math.isnan(tps):
        groups[(scenario, clients)]["tps"].append(tps)
    if not math.isnan(lat):
        groups[(scenario, clients)]["lat"].append(lat)

header = [
    "scenario",
    "clients",
    "runs",
    "tps_mean",
    "tps_min",
    "tps_max",
    "tps_stddev",
    "tps_cv_pct",
    "latency_mean_ms",
    "latency_min_ms",
    "latency_max_ms",
    "latency_stddev_ms",
    "latency_cv_pct",
]

summary_rows = []
for (scenario, clients), vals in sorted(groups.items()):
    tps = vals["tps"]
    lat = vals["lat"]
    if not tps and not lat:
        continue
    tps_mean = statistics.fmean(tps) if tps else math.nan
    tps_std = statistics.pstdev(tps) if len(tps) > 1 else 0.0
    tps_cv = (tps_std / tps_mean * 100.0) if tps and tps_mean else math.nan

    lat_mean = statistics.fmean(lat) if lat else math.nan
    lat_std = statistics.pstdev(lat) if len(lat) > 1 else 0.0
    lat_cv = (lat_std / lat_mean * 100.0) if lat and lat_mean else math.nan

    summary_rows.append(
        {
            "scenario": scenario,
            "clients": clients,
            "runs": str(max(len(tps), len(lat))),
            "tps_mean": f"{tps_mean:.4f}" if tps else "",
            "tps_min": f"{min(tps):.4f}" if tps else "",
            "tps_max": f"{max(tps):.4f}" if tps else "",
            "tps_stddev": f"{tps_std:.4f}" if tps else "",
            "tps_cv_pct": f"{tps_cv:.2f}" if tps else "",
            "latency_mean_ms": f"{lat_mean:.4f}" if lat else "",
            "latency_min_ms": f"{min(lat):.4f}" if lat else "",
            "latency_max_ms": f"{max(lat):.4f}" if lat else "",
            "latency_stddev_ms": f"{lat_std:.4f}" if lat else "",
            "latency_cv_pct": f"{lat_cv:.2f}" if lat else "",
        }
    )

with summary_csv.open("w", encoding="utf-8", newline="") as fh:
    writer = csv.DictWriter(fh, fieldnames=header)
    writer.writeheader()
    for row in summary_rows:
        writer.writerow(row)

with summary_md.open("w", encoding="utf-8") as fh:
    fh.write("# PGbench Summary\n\n")
    fh.write(f"Source runs CSV: `{runs_csv}`\n\n")
    fh.write("| Scenario | Clients | Runs | TPS mean | TPS min | TPS max | TPS stddev | TPS CV% | Lat mean ms | Lat min ms | Lat max ms | Lat stddev ms | Lat CV% |\n")
    fh.write("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n")
    for row in summary_rows:
      fh.write(
          f"| {row['scenario']} | {row['clients']} | {row['runs']} | {row['tps_mean']} | {row['tps_min']} | {row['tps_max']} | {row['tps_stddev']} | {row['tps_cv_pct']} | {row['latency_mean_ms']} | {row['latency_min_ms']} | {row['latency_max_ms']} | {row['latency_stddev_ms']} | {row['latency_cv_pct']} |\n"
      )

print(summary_md)
PY

log "PGbench summary markdown: $SUMMARY_MD"
