#!/usr/bin/env python3
from __future__ import annotations

import argparse
import csv
import json
import math
import statistics
from collections import defaultdict
from pathlib import Path
from typing import Any


def read_csv(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        return []
    with path.open(encoding="utf-8") as fh:
        return list(csv.DictReader(fh))


def safe_float(value: str | None) -> float | None:
    if value is None or value == "":
        return None
    try:
        return float(value)
    except Exception:
        return None


def parse_session_env(path: Path) -> dict[str, str]:
    out: dict[str, str] = {}
    if not path.exists():
        return out
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        if "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


def summarize_pgbench(rows: list[dict[str, str]]) -> list[dict[str, str]]:
    groups: dict[tuple[str, str], dict[str, list[float]]] = defaultdict(lambda: {"tps": [], "lat": []})
    errors = 0
    for r in rows:
        if r.get("exit_code") not in {"0", ""}:
            errors += 1
            continue
        key = (r.get("scenario", "unknown"), r.get("clients", ""))
        tps = safe_float(r.get("tps"))
        lat = safe_float(r.get("latency_ms"))
        if tps is not None:
            groups[key]["tps"].append(tps)
        if lat is not None:
            groups[key]["lat"].append(lat)

    out: list[dict[str, str]] = []
    for (scenario, clients), vals in sorted(groups.items()):
        tps_vals = vals["tps"]
        lat_vals = vals["lat"]

        tps_mean = statistics.fmean(tps_vals) if tps_vals else math.nan
        tps_std = statistics.pstdev(tps_vals) if len(tps_vals) > 1 else 0.0
        tps_cv = (tps_std / tps_mean * 100.0) if tps_vals and tps_mean else math.nan

        lat_mean = statistics.fmean(lat_vals) if lat_vals else math.nan
        lat_std = statistics.pstdev(lat_vals) if len(lat_vals) > 1 else 0.0
        lat_cv = (lat_std / lat_mean * 100.0) if lat_vals and lat_mean else math.nan

        out.append(
            {
                "scenario": scenario,
                "clients": clients,
                "runs": str(max(len(tps_vals), len(lat_vals))),
                "tps_mean": f"{tps_mean:.4f}" if tps_vals else "",
                "tps_min": f"{min(tps_vals):.4f}" if tps_vals else "",
                "tps_max": f"{max(tps_vals):.4f}" if tps_vals else "",
                "tps_stddev": f"{tps_std:.4f}" if tps_vals else "",
                "tps_cv_pct": f"{tps_cv:.2f}" if tps_vals else "",
                "latency_mean_ms": f"{lat_mean:.4f}" if lat_vals else "",
                "latency_min_ms": f"{min(lat_vals):.4f}" if lat_vals else "",
                "latency_max_ms": f"{max(lat_vals):.4f}" if lat_vals else "",
                "latency_stddev_ms": f"{lat_std:.4f}" if lat_vals else "",
                "latency_cv_pct": f"{lat_cv:.2f}" if lat_vals else "",
                "errors": str(errors),
            }
        )
    return out


def summarize_uyuni(rows: list[dict[str, str]]) -> list[dict[str, str]]:
    groups: dict[str, list[float]] = defaultdict(list)
    errors: dict[str, int] = defaultdict(int)
    for r in rows:
        workload = r.get("workload", "unknown")
        dur = safe_float(r.get("duration_sec"))
        if dur is not None:
            groups[workload].append(dur)
        if r.get("exit_code") not in {"0", ""}:
            errors[workload] += 1

    out: list[dict[str, str]] = []
    for workload, vals in sorted(groups.items()):
        if not vals:
            continue
        mean = statistics.fmean(vals)
        std = statistics.pstdev(vals) if len(vals) > 1 else 0.0
        cv = (std / mean * 100.0) if mean else math.nan
        out.append(
            {
                "workload": workload,
                "runs": str(len(vals)),
                "duration_avg_s": f"{mean:.4f}",
                "duration_min_s": f"{min(vals):.4f}",
                "duration_max_s": f"{max(vals):.4f}",
                "duration_stddev_s": f"{std:.4f}",
                "duration_cv_pct": f"{cv:.2f}",
                "errors": str(errors.get(workload, 0)),
            }
        )
    return out


def summarize_prom(rows: list[dict[str, str]]) -> tuple[list[dict[str, str]], list[str]]:
    groups: dict[str, dict[str, list[float]]] = defaultdict(lambda: {"avg": [], "max": [], "p95": []})
    missing_queries: set[str] = set()

    for r in rows:
        q = r.get("query_name", "unknown")
        missing = str(r.get("missing", "")).lower() in {"true", "1", "yes"}
        if missing:
            missing_queries.add(q)
            continue
        for metric in ("avg", "max", "p95"):
            val = safe_float(r.get(metric))
            if val is not None and not math.isnan(val):
                groups[q][metric].append(val)

    out: list[dict[str, str]] = []
    for q, vals in sorted(groups.items()):
        out.append(
            {
                "query_name": q,
                "samples": str(len(vals["avg"])),
                "avg_of_avg": f"{statistics.fmean(vals['avg']):.6f}" if vals["avg"] else "",
                "max_of_max": f"{max(vals['max']):.6f}" if vals["max"] else "",
                "avg_p95": f"{statistics.fmean(vals['p95']):.6f}" if vals["p95"] else "",
                "missing_in_some_runs": str(q in missing_queries).lower(),
            }
        )
    return out, sorted(missing_queries)


def write_csv(path: Path, rows: list[dict[str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if not rows:
        path.write_text("", encoding="utf-8")
        return
    header = list(rows[0].keys())
    with path.open("w", encoding="utf-8", newline="") as fh:
        writer = csv.DictWriter(fh, fieldnames=header)
        writer.writeheader()
        for row in rows:
            writer.writerow(row)


def main() -> None:
    parser = argparse.ArgumentParser(description="Summarize benchmark phase 3 session")
    parser.add_argument("session_dir", help="Path to one phase3 session directory")
    args = parser.parse_args()

    session_dir = Path(args.session_dir).resolve()
    if not session_dir.exists():
        raise SystemExit(f"Session dir not found: {session_dir}")

    metadata_env = parse_session_env(session_dir / "metadata" / "session-env.txt")

    pg_caps_path = session_dir / "pg-capabilities.json"
    pg_caps = {}
    if pg_caps_path.exists():
        pg_caps = json.loads(pg_caps_path.read_text(encoding="utf-8"))

    pgbench_rows = read_csv(session_dir / "pgbench" / "pgbench-runs.csv")
    uyuni_rows = read_csv(session_dir / "uyuni" / "uyuni-workload-runs.csv")
    prom_rows = read_csv(session_dir / "prometheus" / "prometheus-runs.csv")

    pgbench_summary = summarize_pgbench(pgbench_rows)
    uyuni_summary = summarize_uyuni(uyuni_rows)
    prom_summary, missing_metrics = summarize_prom(prom_rows)

    pgbench_summary_csv = session_dir / "pgbench-summary.csv"
    uyuni_summary_csv = session_dir / "uyuni-workload-summary.csv"
    prom_summary_csv = session_dir / "prometheus-summary.csv"
    phase3_summary_md = session_dir / "phase3-summary.md"

    write_csv(pgbench_summary_csv, pgbench_summary)
    write_csv(uyuni_summary_csv, uyuni_summary)
    write_csv(prom_summary_csv, prom_summary)

    missing_views: list[str] = []
    if pg_caps:
        caps = pg_caps.get("capabilities", {})
        for view_name in ("pg_stat_wal", "pg_stat_io", "pg_stat_checkpointer", "pg_stat_statements"):
            if not caps.get(view_name, False):
                missing_views.append(view_name)

    errors = [r for r in pgbench_rows + uyuni_rows if r.get("exit_code") not in {"0", ""}]

    with phase3_summary_md.open("w", encoding="utf-8") as fh:
        fh.write("# Phase 3 Summary\n\n")
        fh.write(f"- session_dir: `{session_dir}`\n")
        fh.write(f"- generated_utc: `{metadata_env.get('snapshot_time_utc', 'unknown')}`\n")
        fh.write(f"- storage_class: `{metadata_env.get('storage_class', 'unknown')}`\n")
        fh.write(f"- namespace: `{metadata_env.get('uyuni_namespace', 'unknown')}`\n")
        fh.write("\n")

        fh.write("## Run Inventory\n\n")
        fh.write(f"- pgbench_runs: {len(pgbench_rows)}\n")
        fh.write(f"- uyuni_workload_runs: {len(uyuni_rows)}\n")
        fh.write(f"- prometheus_query_windows: {len(prom_rows)}\n")
        fh.write("\n")

        fh.write("## PostgreSQL Capabilities\n\n")
        if pg_caps:
            fh.write(f"- server_version_num: `{pg_caps.get('server_version_num', 'unknown')}`\n")
            fh.write("- capabilities:\n")
            for k, v in sorted(pg_caps.get("capabilities", {}).items()):
                fh.write(f"  - {k}: `{v}`\n")
        else:
            fh.write("No pg-capabilities.json found.\n")
        fh.write("\n")

        fh.write("## PGbench Summary\n\n")
        fh.write(f"CSV: `{pgbench_summary_csv}`\n\n")
        if pgbench_summary:
            fh.write("| Scenario | Clients | Runs | TPS mean | TPS CV% | Lat mean ms | Lat CV% | Errors |\n")
            fh.write("| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |\n")
            for r in pgbench_summary:
                fh.write(
                    f"| {r['scenario']} | {r['clients']} | {r['runs']} | {r['tps_mean']} | {r['tps_cv_pct']} | {r['latency_mean_ms']} | {r['latency_cv_pct']} | {r['errors']} |\n"
                )
        else:
            fh.write("No successful pgbench rows found.\n")
        fh.write("\n")

        fh.write("## Uyuni Workload Summary\n\n")
        fh.write(f"CSV: `{uyuni_summary_csv}`\n\n")
        if uyuni_summary:
            fh.write("| Workload | Runs | Duration avg (s) | Duration CV% | Errors |\n")
            fh.write("| --- | ---: | ---: | ---: | ---: |\n")
            for r in uyuni_summary:
                fh.write(
                    f"| {r['workload']} | {r['runs']} | {r['duration_avg_s']} | {r['duration_cv_pct']} | {r['errors']} |\n"
                )
        else:
            fh.write("No workload rows found.\n")
        fh.write("\n")

        fh.write("## Prometheus Summary\n\n")
        fh.write(f"CSV: `{prom_summary_csv}`\n\n")
        if prom_summary:
            fh.write("| Query | Samples | Avg of avg | Max of max | Avg p95 | Missing in some runs |\n")
            fh.write("| --- | ---: | ---: | ---: | ---: | --- |\n")
            for r in prom_summary:
                fh.write(
                    f"| {r['query_name']} | {r['samples']} | {r['avg_of_avg']} | {r['max_of_max']} | {r['avg_p95']} | {r['missing_in_some_runs']} |\n"
                )
        else:
            fh.write("No Prometheus summaries found.\n")
        fh.write("\n")

        fh.write("## Errors Encountered\n\n")
        if not errors:
            fh.write("No non-zero exits recorded in run inventories.\n")
        else:
            for e in errors:
                fh.write(f"- run_id={e.get('run_id','')} workload={e.get('workload','')} exit_code={e.get('exit_code','')}\n")
        fh.write("\n")

        fh.write("## Missing Metrics / Views\n\n")
        if missing_metrics:
            fh.write("Missing Prometheus query results observed for:\n")
            for m in missing_metrics:
                fh.write(f"- {m}\n")
        else:
            fh.write("No missing Prometheus query series detected in parsed summaries.\n")

        if missing_views:
            fh.write("\nMissing PostgreSQL views/extensions:\n")
            for v in missing_views:
                fh.write(f"- {v}\n")

    print(str(phase3_summary_md))


if __name__ == "__main__":
    main()
