#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/run_phase3.sh [options]

Options:
  --profile smoke|full        Profile to run (default: smoke)
  --storage-class <name>      StorageClass context label (default from env)
  --outdir <dir>              Session output parent directory (default: benchmark-phase3/results)
  --skip-monitoring           Skip monitoring bootstrap step
  --skip-pgbench              Skip pgbench benchmark matrix
  --skip-uyuni                Skip Uyuni workload benchmarks
USAGE
}

PROFILE="smoke"
OUTDIR=""
SKIP_MONITORING=0
SKIP_PGBENCH=0
SKIP_UYUNI=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"
      shift 2
      ;;
    --storage-class)
      STORAGE_CLASS="$2"
      shift 2
      ;;
    --outdir)
      OUTDIR="$2"
      shift 2
      ;;
    --skip-monitoring)
      SKIP_MONITORING=1
      shift
      ;;
    --skip-pgbench)
      SKIP_PGBENCH=1
      shift
      ;;
    --skip-uyuni)
      SKIP_UYUNI=1
      shift
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

phase3_load_env
require_cmd kubectl python3 jq

if [[ -z "$OUTDIR" ]]; then
  OUTDIR="$PHASE3_ROOT/results"
fi

SESSION_DIR="$(phase3_new_session_dir "$OUTDIR" "$PROFILE" "$STORAGE_CLASS")"
mkdir -p "$SESSION_DIR"
LOG_FILE="$SESSION_DIR/phase3-run.log"

{
  log "Phase 3 run start"
  log "Session directory: $SESSION_DIR"
  log "Profile: $PROFILE"
  log "StorageClass context: $STORAGE_CLASS"

  # 1) create session directory and metadata snapshot
  phase3_save_metadata_snapshot "$SESSION_DIR"

  # 2) verify storage class context exists
  kubectl get sc "$STORAGE_CLASS" >/dev/null 2>&1 || die "StorageClass not found: $STORAGE_CLASS"

  # 3) monitoring bootstrap
  if [[ "$SKIP_MONITORING" == "0" ]]; then
    bash "$SCRIPT_DIR/bootstrap_monitoring.sh" --session-dir "$SESSION_DIR"
  else
    warn "Skipping monitoring bootstrap (--skip-monitoring)"
  fi

  # 4) verify metrics (still useful even if monitoring bootstrap is skipped)
  bash "$SCRIPT_DIR/verify_metrics.sh" --session-dir "$SESSION_DIR"

  # 5) detect PostgreSQL capabilities and optional instrumentation toggles
  bash "$SCRIPT_DIR/detect_pg_capabilities.sh" --session-dir "$SESSION_DIR"

  # 6) run pgbench matrix
  if [[ "$SKIP_PGBENCH" == "0" ]]; then
    bash "$SCRIPT_DIR/run_pgbench_matrix.sh" --profile "$PROFILE" --session-dir "$SESSION_DIR"
  else
    warn "Skipping pgbench matrix (--skip-pgbench)"
  fi

  # 7) run Uyuni workloads
  if [[ "$SKIP_UYUNI" == "0" ]]; then
    bash "$SCRIPT_DIR/run_uyuni_workloads.sh" --profile "$PROFILE" --session-dir "$SESSION_DIR"
  else
    warn "Skipping Uyuni workloads (--skip-uyuni)"
  fi

  # 8) summarize everything
  python3 "$SCRIPT_DIR/summarize_phase3.py" "$SESSION_DIR"

  log "Phase 3 run complete"
  log "Top-level summary: $SESSION_DIR/phase3-summary.md"
} | tee "$LOG_FILE"
