#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/run_uyuni_workloads.sh --profile smoke|full --session-dir <path>
USAGE
}

PROFILE=""
SESSION_DIR=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"
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

[[ "$PROFILE" == "smoke" || "$PROFILE" == "full" ]] || die "--profile must be smoke or full"
[[ -n "$SESSION_DIR" ]] || die "--session-dir is required"

phase3_load_env
require_cmd kubectl python3 jq gzip

mkdir -p "$SESSION_DIR/uyuni"
RUNS_CSV="$SESSION_DIR/uyuni/uyuni-workload-runs.csv"
SUMMARY_MD="$SESSION_DIR/uyuni/uyuni-workloads-summary.md"

if [[ ! -f "$RUNS_CSV" ]]; then
  cat > "$RUNS_CSV" <<CSV
run_id,workload,profile,start_time_utc,end_time_utc,start_epoch,end_epoch,duration_sec,storage_class,namespace,parameters_json,exit_code,raw_log,result_json,pre_sql_json,post_sql_json,prom_summary_json,node_metadata
CSV
fi

record_run() {
  local run_id="$1"
  local workload="$2"
  local start_time_utc="$3"
  local end_time_utc="$4"
  local start_epoch="$5"
  local end_epoch="$6"
  local duration_sec="$7"
  local parameters_json="$8"
  local exit_code="$9"
  local raw_log="${10}"
  local result_json="${11}"
  local pre_sql_json="${12}"
  local post_sql_json="${13}"
  local prom_summary_json="${14}"
  local node_meta="${15}"

  append_csv_row "$RUNS_CSV" \
    "$run_id" "$workload" "$PROFILE" \
    "$start_time_utc" "$end_time_utc" "$start_epoch" "$end_epoch" "$duration_sec" \
    "$STORAGE_CLASS" "$UYUNI_NAMESPACE" "$parameters_json" "$exit_code" \
    "$raw_log" "$result_json" "$pre_sql_json" "$post_sql_json" "$prom_summary_json" "$node_meta"
}

run_with_common_artifacts() {
  local run_id="$1"
  local workload="$2"
  local parameters_json="$3"
  local command_func="$4"
  local run_dir="$5"

  mkdir -p "$run_dir"

  local node_meta="$run_dir/node-metadata.txt"
  local raw_log="$run_dir/${workload}.raw.log"
  local result_json="$run_dir/${workload}.result.json"

  kubectl get nodes -o wide > "$node_meta"

  bash "$SCRIPT_DIR/collect_sql_snapshot.sh" --phase pre --run-id "$run_id" --session-dir "$SESSION_DIR" --db "$DB_BENCH_DB"
  local pre_sql_json="$SESSION_DIR/sql/pre-sql-${run_id}.json"

  local start_time_utc
  local start_epoch
  start_time_utc="$(now_utc)"
  start_epoch="$(now_epoch)"

  local exit_code=0
  "$command_func" "$run_dir" > "$raw_log" 2>&1 || exit_code=$?

  local end_time_utc
  local end_epoch
  end_time_utc="$(now_utc)"
  end_epoch="$(now_epoch)"

  bash "$SCRIPT_DIR/collect_sql_snapshot.sh" --phase post --run-id "$run_id" --session-dir "$SESSION_DIR" --db "$DB_BENCH_DB"
  local post_sql_json="$SESSION_DIR/sql/post-sql-${run_id}.json"

  bash "$SCRIPT_DIR/collect_prometheus_range.sh" \
    --start-epoch "$start_epoch" \
    --end-epoch "$end_epoch" \
    --workload "$workload" \
    --run-id "$run_id" \
    --session-dir "$SESSION_DIR"
  local prom_summary_json="$SESSION_DIR/prometheus/${workload}-${run_id}-summary.json"

  local duration_sec=$((end_epoch - start_epoch))

  jq -n \
    --arg run_id "$run_id" \
    --arg workload "$workload" \
    --arg profile "$PROFILE" \
    --arg start_time_utc "$start_time_utc" \
    --arg end_time_utc "$end_time_utc" \
    --argjson start_epoch "$start_epoch" \
    --argjson end_epoch "$end_epoch" \
    --argjson duration_sec "$duration_sec" \
    --arg storage_class "$STORAGE_CLASS" \
    --arg namespace "$UYUNI_NAMESPACE" \
    --argjson parameters "$parameters_json" \
    --arg raw_log "$raw_log" \
    --arg pre_sql_json "$pre_sql_json" \
    --arg post_sql_json "$post_sql_json" \
    --arg prom_summary_json "$prom_summary_json" \
    --arg node_metadata "$node_meta" \
    --argjson exit_code "$exit_code" \
    '{
      run_id: $run_id,
      workload_name: $workload,
      profile: $profile,
      start_time_utc: $start_time_utc,
      end_time_utc: $end_time_utc,
      start_epoch: $start_epoch,
      end_epoch: $end_epoch,
      duration_sec: $duration_sec,
      storage_class: $storage_class,
      namespace: $namespace,
      workload_parameters: $parameters,
      raw_output_path: $raw_log,
      sql_snapshots: { pre: $pre_sql_json, post: $post_sql_json },
      prometheus_summary_path: $prom_summary_json,
      node_metadata_path: $node_metadata,
      exit_code: $exit_code
    }' > "$result_json"

  record_run "$run_id" "$workload" "$start_time_utc" "$end_time_utc" "$start_epoch" "$end_epoch" "$duration_sec" \
    "$parameters_json" "$exit_code" "$raw_log" "$result_json" "$pre_sql_json" "$post_sql_json" "$prom_summary_json" "$node_meta"

  return "$exit_code"
}

repo_sync_channels() {
  local mcp
  mcp="$(get_micro_client_pod)"
  kubectl -n "$UYUNI_NAMESPACE" exec "$mcp" -- sh -lc "grep '^baseurl=' /etc/zypp/repos.d/susemanager:channels.repo | sed -E 's#^.*/download/([^?]+).*$#\\1#'" | sed '/^$/d' | sort -u
}

run_repo_sync_minimal_impl() {
  local run_dir="$1"
  local upod
  upod="$(get_uyuni_pod)"

  local channels
  channels="$(repo_sync_channels || true)"
  [[ -n "$channels" ]] || die "Could not detect channels for repo_sync_minimal"

  local ch
  while IFS= read -r ch; do
    [[ -n "$ch" ]] || continue
    echo "Syncing channel: $ch"
    kubectl -n "$UYUNI_NAMESPACE" exec "$upod" -- sh -lc "spacewalk-repo-sync -c '$ch'"
  done <<< "$channels"

  kubectl -n "$UYUNI_NAMESPACE" logs deploy/uyuni --since=30m > "$run_dir/uyuni-server.log" || true
}

run_metadata_refresh_impl() {
  local _run_dir="$1"
  local upod
  upod="$(get_uyuni_pod)"

  kubectl -n "$UYUNI_NAMESPACE" exec "$upod" -- salt 'micro-client' saltutil.refresh_pillar
  kubectl -n "$UYUNI_NAMESPACE" exec "$upod" -- salt 'micro-client' state.apply channels
  kubectl -n "$UYUNI_NAMESPACE" exec "$upod" -- salt 'micro-client' pkg.refresh_db
}

run_ui_locust_impl() {
  local run_dir="$1"
  local users="$2"
  local duration="$3"

  local job_name="locust-${users}-$(date -u +%H%M%S)"
  local cm_name="${job_name}-cm"

  cat > "$run_dir/locustfile.py" <<'PY'
from __future__ import annotations

import os
import re
import urllib3
from urllib.parse import urlparse
from locust import HttpUser, between, task

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

class UyuniUser(HttpUser):
    wait_time = between(1, 3)

    def on_start(self) -> None:
        self.client.verify = False
        username = os.getenv("UYUNI_USER", "admin")
        password = os.getenv("UYUNI_PASS", "admin")

        resp = self.client.get("/rhn/manager/login", name="login_page", allow_redirects=True)
        token_match = re.search(r'name="csrf_token"\s+value="([^"]+)"', resp.text)
        action = "/rhn/manager/login"
        action_match = re.search(r'<form[^>]+action="([^"]+)"', resp.text, flags=re.IGNORECASE)
        if action_match:
            action = action_match.group(1).strip()
            if action.startswith("http://") or action.startswith("https://"):
                parsed = urlparse(action)
                action = parsed.path or "/rhn/manager/login"
                if parsed.query:
                    action = f"{action}?{parsed.query}"
            if not action.startswith("/"):
                action = "/" + action.lstrip("./")
        form = {"username": username, "password": password}
        if token_match:
            form["csrf_token"] = token_match.group(1)
        self.client.post(action, data=form, name="login_post", allow_redirects=True)

    @task(3)
    def dashboard(self) -> None:
        self.client.get("/rhn/manager/overview", name="dashboard_overview", verify=False)

    @task(2)
    def systems(self) -> None:
        self.client.get("/rhn/manager/systems/overview", name="systems_list", verify=False)
PY

  kubectl -n "$BENCH_NAMESPACE" create configmap "$cm_name" --from-file=locustfile.py="$run_dir/locustfile.py" --dry-run=client -o yaml | kubectl apply -f -

  cat <<YAML | kubectl -n "$BENCH_NAMESPACE" apply -f -
apiVersion: batch/v1
kind: Job
metadata:
  name: ${job_name}
spec:
  backoffLimit: 0
  ttlSecondsAfterFinished: 600
  template:
    spec:
      restartPolicy: Never
      containers:
        - name: locust
          image: python:3.12-slim
          env:
            - name: UYUNI_USER
              value: "${UYUNI_GUI_USER}"
            - name: UYUNI_PASS
              value: "${UYUNI_GUI_PASS}"
          command:
            - sh
            - -lc
            - >-
              python -m pip install --disable-pip-version-check --no-cache-dir 'locust==2.31.6' &&
              python -m locust -f /mnt/locust/locustfile.py --headless -u ${users} -r ${LOCUST_SPAWN_RATE}
              -t ${duration}s --host https://${UYUNI_FQDN} --csv=/results/locust --csv-full-history
          volumeMounts:
            - name: locustfile
              mountPath: /mnt/locust
            - name: results
              mountPath: /results
      volumes:
        - name: locustfile
          configMap:
            name: ${cm_name}
        - name: results
          emptyDir: {}
YAML

  local wait_exit=0
  local deadline now_ts succeeded failed
  deadline=$(( $(now_epoch) + duration + 240 ))
  succeeded=0
  failed=0
  while true; do
    succeeded="$(kubectl -n "$BENCH_NAMESPACE" get job "$job_name" -o jsonpath='{.status.succeeded}' 2>/dev/null || true)"
    failed="$(kubectl -n "$BENCH_NAMESPACE" get job "$job_name" -o jsonpath='{.status.failed}' 2>/dev/null || true)"
    succeeded="${succeeded:-0}"
    failed="${failed:-0}"
    if [[ "$succeeded" != "0" ]]; then
      break
    fi
    if [[ "$failed" != "0" ]]; then
      wait_exit=1
      break
    fi
    now_ts="$(now_epoch)"
    if [[ "$now_ts" -ge "$deadline" ]]; then
      wait_exit=1
      break
    fi
    sleep 2
  done

  local pod
  pod="$(kubectl -n "$BENCH_NAMESPACE" get pod -l job-name="$job_name" -o jsonpath='{.items[0].metadata.name}')"
  [[ -n "$pod" ]] || die "Could not locate locust pod for job ${job_name}"

  kubectl -n "$BENCH_NAMESPACE" logs "$pod" > "$run_dir/locust.log" || true
  mkdir -p "$run_dir/locust-csv"
  local copied_any=0
  if kubectl -n "$BENCH_NAMESPACE" cp "$pod:/results/." "$run_dir/locust-csv" >/dev/null 2>&1; then
    copied_any=1
  else
    local lf
    for lf in locust_stats.csv locust_failures.csv locust_exceptions.csv locust_stats_history.csv; do
      if kubectl -n "$BENCH_NAMESPACE" exec "$pod" -- sh -lc "test -f /results/${lf}" >/dev/null 2>&1; then
        kubectl -n "$BENCH_NAMESPACE" exec "$pod" -- sh -lc "cat /results/${lf}" > "$run_dir/locust-csv/${lf}" 2>/dev/null || true
      fi
    done
    if [[ -s "$run_dir/locust-csv/locust_stats.csv" ]]; then
      copied_any=1
    fi
  fi

  local run_exit=0

  if [[ "$wait_exit" -ne 0 || "$failed" != "0" || "$succeeded" == "0" ]]; then
    run_exit=1
  fi

  if [[ "$copied_any" -eq 0 || ! -s "$run_dir/locust-csv/locust_stats.csv" ]]; then
    run_exit=1
  fi

  if [[ -s "$run_dir/locust-csv/locust_stats.csv" ]]; then
    python3 - "$run_dir/locust-csv/locust_stats.csv" "$run_dir/locust-summary.md" <<'PY'
from __future__ import annotations

import csv
import sys
from pathlib import Path

stats_csv = Path(sys.argv[1])
summary_md = Path(sys.argv[2])
rows = list(csv.DictReader(stats_csv.open(encoding="utf-8")))
agg = next((r for r in rows if r.get("Name") == "Aggregated"), None)

with summary_md.open("w", encoding="utf-8") as fh:
    fh.write("# Locust Summary\n\n")
    fh.write(f"Source: `{stats_csv}`\n\n")
    if not agg:
        fh.write("No aggregated row found in locust stats CSV.\n")
    else:
        fh.write("| Requests | Failures | Median ms | Avg ms | Max ms | RPS |\n")
        fh.write("| ---: | ---: | ---: | ---: | ---: | ---: |\n")
        fh.write(
            f"| {agg.get('# requests','')} | {agg.get('# failures','')} | {agg.get('Median response time','')} | "
            f"{agg.get('Average response time','')} | {agg.get('Max response time','')} | {agg.get('Requests/s','')} |\n"
        )
PY
  fi

  kubectl -n "$BENCH_NAMESPACE" delete job "$job_name" --ignore-not-found=true
  kubectl -n "$BENCH_NAMESPACE" delete configmap "$cm_name" --ignore-not-found=true

  return "$run_exit"
}

run_package_download_impl() {
  local run_dir="$1"
  local concurrency="$2"

  local mcp
  mcp="$(get_micro_client_pod)"
  local baseurl
  baseurl="$(kubectl -n "$UYUNI_NAMESPACE" exec "$mcp" -- sh -lc "grep '^baseurl=' /etc/zypp/repos.d/susemanager:channels.repo | head -n1 | cut -d= -f2-")"
  [[ -n "$baseurl" ]] || die "Could not determine package repo baseurl from micro-client"

  python3 - "$baseurl" "$concurrency" "$PACKAGE_DOWNLOAD_LIMIT" "$run_dir" <<'PY'
from __future__ import annotations

import gzip
import json
import os
import ssl
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

baseurl = sys.argv[1]
concurrency = int(sys.argv[2])
limit = int(sys.argv[3])
outdir = Path(sys.argv[4])
outdir.mkdir(parents=True, exist_ok=True)

split = urllib.parse.urlsplit(baseurl)
base_no_query = urllib.parse.urlunsplit((split.scheme, split.netloc, split.path.rstrip("/") + "/", "", ""))
query = split.query

def with_query(url: str) -> str:
    if not query:
        return url
    sep = "&" if "?" in url else "?"
    return f"{url}{sep}{query}"

repomd_url = with_query(urllib.parse.urljoin(base_no_query, "repodata/repomd.xml"))
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

repomd_xml = urllib.request.urlopen(repomd_url, timeout=60, context=ctx).read()
(outdir / "repomd.xml").write_bytes(repomd_xml)

ns = {"repo": "http://linux.duke.edu/metadata/repo"}
root = ET.fromstring(repomd_xml)
primary_href = None
for data in root.findall("repo:data", ns):
    if data.attrib.get("type") == "primary":
        loc = data.find("repo:location", ns)
        if loc is not None:
            primary_href = loc.attrib.get("href")
            break
if not primary_href:
    raise RuntimeError("Could not find primary metadata href in repomd.xml")

primary_url = with_query(urllib.parse.urljoin(base_no_query, primary_href))
primary_bytes = urllib.request.urlopen(primary_url, timeout=120, context=ctx).read()
primary_path = outdir / Path(primary_href).name
primary_path.write_bytes(primary_bytes)

if primary_href.endswith(".gz"):
    primary_xml = gzip.decompress(primary_bytes)
elif primary_href.endswith(".zst"):
    try:
        import zstandard as zstd  # type: ignore

        dctx = zstd.ZstdDecompressor()
        primary_xml = dctx.decompress(primary_bytes)
    except Exception:
        proc = subprocess.run(["zstd", "-dc", str(primary_path)], check=True, capture_output=True)
        primary_xml = proc.stdout
else:
    primary_xml = primary_bytes

(outdir / "primary.xml").write_bytes(primary_xml)

ns_common = {"c": "http://linux.duke.edu/metadata/common"}
proot = ET.fromstring(primary_xml)

rpm_urls: list[str] = []
for pkg in proot.findall("c:package", ns_common):
    loc = pkg.find("c:location", ns_common)
    if loc is None:
        continue
    href = loc.attrib.get("href")
    if not href:
        continue
    rpm_urls.append(with_query(urllib.parse.urljoin(base_no_query, href)))
    if len(rpm_urls) >= limit:
        break

(outdir / "selected-urls.json").write_text(json.dumps(rpm_urls, indent=2), encoding="utf-8")


def fetch_one(url: str) -> dict[str, object]:
    start = time.time()
    rec: dict[str, object] = {"url": url, "ok": False, "status": None, "bytes": 0, "seconds": 0.0, "error": ""}
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=120, context=ctx) as resp:
            total = 0
            while True:
                chunk = resp.read(1024 * 128)
                if not chunk:
                    break
                total += len(chunk)
            rec["status"] = int(resp.status)
            rec["bytes"] = total
            rec["ok"] = int(resp.status) < 400
    except Exception as exc:  # noqa: BLE001
        rec["error"] = str(exc)
    rec["seconds"] = round(time.time() - start, 4)
    return rec

results: list[dict[str, object]] = []
with ThreadPoolExecutor(max_workers=concurrency) as ex:
    futs = [ex.submit(fetch_one, url) for url in rpm_urls]
    for fut in as_completed(futs):
        results.append(fut.result())

success = sum(1 for r in results if r.get("ok"))
failed = len(results) - success
total_bytes = sum(int(r.get("bytes", 0)) for r in results)

summary = {
    "baseurl": baseurl,
    "repomd_url": repomd_url,
    "primary_url": primary_url,
    "concurrency": concurrency,
    "limit": limit,
    "selected_count": len(rpm_urls),
    "success_count": success,
    "failure_count": failed,
    "total_bytes": total_bytes,
    "downloads": sorted(results, key=lambda x: str(x.get("url", ""))),
}

(outdir / "download-summary.json").write_text(json.dumps(summary, indent=2), encoding="utf-8")

with (outdir / "download-results.csv").open("w", encoding="utf-8") as fh:
    fh.write("url,ok,status,bytes,seconds,error\n")
    for r in summary["downloads"]:
        err = str(r.get("error", "")).replace('"', '""')
        fh.write(
            f"{r['url']},{str(r['ok']).lower()},{r['status']},{r['bytes']},{r['seconds']},\"{err}\"\n"
        )

print(json.dumps(summary))
PY
}

# Workload execution plan by profile.
repo_sync_runs=0
metadata_runs=3
locust_users="5,10"
locust_duration="120"
package_conc="1,4"

if [[ "$PROFILE" == "full" ]]; then
  repo_sync_runs=3
  metadata_runs=3
  locust_users="5,10,20"
  locust_duration="180"
  package_conc="1,4,8"
fi

# A) repo_sync_minimal
if [[ "$repo_sync_runs" -gt 0 ]]; then
  for run_n in $(seq 1 "$repo_sync_runs"); do
    run_id="repo_sync_minimal-r${run_n}"
    run_dir="$SESSION_DIR/uyuni/repo_sync_minimal/run${run_n}"
    params='{"workload":"repo_sync_minimal","run":'"$run_n"'}'
    run_with_common_artifacts "$run_id" "repo_sync_minimal" "$params" run_repo_sync_minimal_impl "$run_dir" || true
  done
fi

# B) metadata_refresh
for run_n in $(seq 1 "$metadata_runs"); do
  run_id="metadata_refresh-r${run_n}"
  run_dir="$SESSION_DIR/uyuni/metadata_refresh/run${run_n}"
  params='{"workload":"metadata_refresh","run":'"$run_n"'}'
  run_with_common_artifacts "$run_id" "metadata_refresh" "$params" run_metadata_refresh_impl "$run_dir" || true
done

# C) ui_locust
while IFS= read -r users; do
  [[ -n "$users" ]] || continue
  run_id="ui_locust-u${users}"
  run_dir="$SESSION_DIR/uyuni/ui_locust/u${users}"
  params='{"workload":"ui_locust","users":'"$users"',"spawn_rate":'"$LOCUST_SPAWN_RATE"',"duration":'"$locust_duration"'}'

  ui_locust_wrapper() {
    run_ui_locust_impl "$1" "$users" "$locust_duration"
  }

  run_with_common_artifacts "$run_id" "ui_locust" "$params" ui_locust_wrapper "$run_dir" || true
done < <(csv_to_lines "$locust_users")

# D) package_download
while IFS= read -r conc; do
  [[ -n "$conc" ]] || continue
  run_id="package_download-c${conc}"
  run_dir="$SESSION_DIR/uyuni/package_download/c${conc}"
  params='{"workload":"package_download","concurrency":'"$conc"',"limit":'"$PACKAGE_DOWNLOAD_LIMIT"'}'

  package_download_wrapper() {
    run_package_download_impl "$1" "$conc"
  }

  run_with_common_artifacts "$run_id" "package_download" "$params" package_download_wrapper "$run_dir" || true
done < <(csv_to_lines "$package_conc")

python3 - "$RUNS_CSV" "$SUMMARY_MD" <<'PY'
from __future__ import annotations

import csv
import statistics
import sys
from collections import defaultdict
from pathlib import Path

runs_csv = Path(sys.argv[1])
summary_md = Path(sys.argv[2])

rows = []
with runs_csv.open(encoding="utf-8") as fh:
    reader = csv.DictReader(fh)
    rows = list(reader)

by_workload: dict[str, list[float]] = defaultdict(list)
failures: list[tuple[str, str]] = []
for r in rows:
    try:
        dur = float(r.get("duration_sec", "") or 0)
        by_workload[r.get("workload", "unknown")].append(dur)
    except Exception:
        pass
    if r.get("exit_code") not in {"0", ""}:
        failures.append((r.get("run_id", ""), r.get("exit_code", "")))

with summary_md.open("w", encoding="utf-8") as fh:
    fh.write("# Uyuni Workload Summary\n\n")
    fh.write(f"Source runs CSV: `{runs_csv}`\n\n")
    fh.write("| Workload | Runs | Duration avg (s) | Duration min (s) | Duration max (s) |\n")
    fh.write("| --- | ---: | ---: | ---: | ---: |\n")
    for workload, durs in sorted(by_workload.items()):
        if not durs:
            continue
        fh.write(
            f"| {workload} | {len(durs)} | {statistics.fmean(durs):.2f} | {min(durs):.2f} | {max(durs):.2f} |\n"
        )
    fh.write("\n## Failures\n\n")
    if not failures:
        fh.write("No non-zero workload exits recorded.\n")
    else:
        for run_id, code in failures:
            fh.write(f"- {run_id}: exit_code={code}\n")

print(summary_md)
PY

log "Uyuni workload summary: $SUMMARY_MD"
