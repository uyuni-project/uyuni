#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/collect_prometheus_range.sh \
  --start-epoch <sec> --end-epoch <sec> \
  --workload <name> --run-id <id> --session-dir <path> [--step 15]
USAGE
}

START_EPOCH=""
END_EPOCH=""
WORKLOAD=""
RUN_ID=""
SESSION_DIR=""
STEP="15"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --start-epoch)
      START_EPOCH="$2"
      shift 2
      ;;
    --end-epoch)
      END_EPOCH="$2"
      shift 2
      ;;
    --workload)
      WORKLOAD="$2"
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
    --step)
      STEP="$2"
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

[[ -n "$START_EPOCH" ]] || die "--start-epoch is required"
[[ -n "$END_EPOCH" ]] || die "--end-epoch is required"
[[ -n "$WORKLOAD" ]] || die "--workload is required"
[[ -n "$RUN_ID" ]] || die "--run-id is required"
[[ -n "$SESSION_DIR" ]] || die "--session-dir is required"

phase3_load_env
require_cmd python3 kubectl jq curl

mkdir -p "$SESSION_DIR/prometheus/raw"
SUMMARY_JSON="$SESSION_DIR/prometheus/${WORKLOAD}-${RUN_ID}-summary.json"
SUMMARY_CSV="$SESSION_DIR/prometheus/prometheus-runs.csv"

effective_prom_url="$PROM_URL"
pf_pid=""

if ! curl -fsS "$effective_prom_url/api/v1/status/runtimeinfo" >/dev/null 2>&1; then
  warn "Direct Prometheus URL unreachable from host: $effective_prom_url"
  prom_svc="$(
    kubectl -n "$MONITORING_NAMESPACE" get svc -o json \
      | jq -r '.items[].metadata.name' \
      | grep -E 'prometheus' \
      | head -n1 || true
  )"
  [[ -n "$prom_svc" ]] || die "Could not detect Prometheus service for port-forward fallback"

  log "Using Prometheus port-forward fallback via service/${prom_svc}"
  kubectl -n "$MONITORING_NAMESPACE" port-forward "svc/${prom_svc}" 19090:9090 >/tmp/phase3-prom-portforward.log 2>&1 &
  pf_pid=$!
  trap '[[ -n "$pf_pid" ]] && kill "$pf_pid" >/dev/null 2>&1 || true' EXIT
  sleep 3

  effective_prom_url="http://127.0.0.1:19090"
fi

python3 - "$PHASE3_ROOT/config/prom_queries.yaml" "$effective_prom_url" "$START_EPOCH" "$END_EPOCH" "$STEP" "$WORKLOAD" "$RUN_ID" "$SESSION_DIR/prometheus/raw" "$SUMMARY_JSON" "$SUMMARY_CSV" <<'PY'
from __future__ import annotations

import csv
import json
import math
import statistics
import sys
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any


def parse_queries_yaml(path: Path) -> list[dict[str, Any]]:
    text = path.read_text(encoding="utf-8")

    try:
        import yaml  # type: ignore

        data = yaml.safe_load(text)
        queries = data.get("queries", []) if isinstance(data, dict) else []
        out: list[dict[str, Any]] = []
        for q in queries:
            if not isinstance(q, dict):
                continue
            name = str(q.get("name", "")).strip()
            query = str(q.get("query", "")).strip()
            optional = bool(q.get("optional", False))
            if name and query:
                out.append({"name": name, "query": query, "optional": optional})
        return out
    except Exception:
        pass

    # Minimal fallback parser for the simple list format used in this repo.
    out: list[dict[str, Any]] = []
    current: dict[str, Any] | None = None
    for raw_line in text.splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#"):
            continue
        if line.startswith("- name:"):
            if current and current.get("name") and current.get("query"):
                out.append(current)
            current = {"name": line.split(":", 1)[1].strip(), "query": "", "optional": False}
        elif line.startswith("name:"):
            if current is None:
                current = {"name": line.split(":", 1)[1].strip(), "query": "", "optional": False}
        elif line.startswith("query:") and current is not None:
            current["query"] = line.split(":", 1)[1].strip()
        elif line.startswith("optional:") and current is not None:
            current["optional"] = line.split(":", 1)[1].strip().lower() in {"1", "true", "yes"}
    if current and current.get("name") and current.get("query"):
        out.append(current)
    return out


def percentile(values: list[float], p: float) -> float:
    if not values:
        return math.nan
    if len(values) == 1:
        return values[0]
    values = sorted(values)
    rank = (len(values) - 1) * p
    low = int(math.floor(rank))
    high = int(math.ceil(rank))
    if low == high:
        return values[low]
    frac = rank - low
    return values[low] * (1 - frac) + values[high] * frac


config_path = Path(sys.argv[1])
prom_url = sys.argv[2].rstrip("/")
start_epoch = sys.argv[3]
end_epoch = sys.argv[4]
step = sys.argv[5]
workload = sys.argv[6]
run_id = sys.argv[7]
raw_dir = Path(sys.argv[8])
summary_json_path = Path(sys.argv[9])
summary_csv_path = Path(sys.argv[10])

raw_dir.mkdir(parents=True, exist_ok=True)
queries = parse_queries_yaml(config_path)

summary_rows: list[dict[str, Any]] = []

for q in queries:
    name = q["name"]
    query = q["query"]
    optional = bool(q.get("optional", False))

    params = {
        "query": query,
        "start": start_epoch,
        "end": end_epoch,
        "step": step,
    }
    url = f"{prom_url}/api/v1/query_range?{urllib.parse.urlencode(params)}"

    raw_file = raw_dir / f"{workload}-{run_id}-{name}.json"
    values: list[float] = []
    missing = False
    error = ""

    try:
        with urllib.request.urlopen(url, timeout=30) as resp:
            payload = resp.read().decode("utf-8", errors="replace")
        data = json.loads(payload)
        raw_file.write_text(json.dumps(data, indent=2), encoding="utf-8")

        result = data.get("data", {}).get("result", [])
        if not result:
            missing = True
        else:
            for series in result:
                for point in series.get("values", []):
                    try:
                        values.append(float(point[1]))
                    except Exception:
                        continue
            if not values:
                missing = True
    except Exception as exc:  # noqa: BLE001
        missing = True
        error = str(exc)
        raw_file.write_text(json.dumps({"error": error, "url": url}, indent=2), encoding="utf-8")

    avg = statistics.fmean(values) if values else math.nan
    mx = max(values) if values else math.nan
    p95 = percentile(values, 0.95) if values else math.nan

    summary_rows.append(
        {
            "workload": workload,
            "run_id": run_id,
            "query_name": name,
            "optional": optional,
            "missing": missing,
            "error": error,
            "samples": len(values),
            "avg": avg,
            "max": mx,
            "p95": p95,
            "raw_file": str(raw_file),
        }
    )

summary_payload = {
    "workload": workload,
    "run_id": run_id,
    "start_epoch": int(start_epoch),
    "end_epoch": int(end_epoch),
    "prom_url": prom_url,
    "queries": summary_rows,
}
summary_json_path.write_text(json.dumps(summary_payload, indent=2), encoding="utf-8")

csv_header = [
    "workload",
    "run_id",
    "query_name",
    "optional",
    "missing",
    "error",
    "samples",
    "avg",
    "max",
    "p95",
    "raw_file",
]

write_header = not summary_csv_path.exists()
with summary_csv_path.open("a", encoding="utf-8", newline="") as fh:
    writer = csv.DictWriter(fh, fieldnames=csv_header)
    if write_header:
        writer.writeheader()
    for row in summary_rows:
        writer.writerow(row)

print(str(summary_json_path))
PY

log "Prometheus summary written to: $SUMMARY_JSON"
