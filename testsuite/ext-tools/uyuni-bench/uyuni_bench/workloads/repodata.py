# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

"""Real-world RPM repodata generation workload."""

from __future__ import annotations

import gzip
import io
import lzma
import os
import shutil
import subprocess
import time
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Any, Dict, Iterable, List, Optional, Sequence, Tuple

from uyuni_bench import __version__
from uyuni_bench.utils import (
    command_output,
    count_files,
    directory_size_bytes,
    ensure_directory,
    file_checksum,
    host_facts,
    parse_gnu_time_verbose,
    read_json,
    utc_now,
    write_json,
)

USER_AGENT = f"uyuni-bench/{__version__}"
REPOMD_NS = {"repo": "http://linux.duke.edu/metadata/repo"}
COMMON_NS = "{http://linux.duke.edu/metadata/common}"


@dataclass
class PackageEntry:
    """Package entry extracted from repository primary metadata."""

    name: str
    arch: str
    href: str
    url: str
    size: int
    checksum_type: Optional[str]
    checksum: Optional[str]


def _urlopen(url: str, timeout: int = 120):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    return urllib.request.urlopen(request, timeout=timeout)


def _download_bytes(url: str, timeout: int = 120) -> bytes:
    with _urlopen(url, timeout=timeout) as response:
        return response.read()


def _primary_metadata_location(repo_url: str) -> str:
    repomd_url = urllib.parse.urljoin(repo_url.rstrip("/") + "/", "repodata/repomd.xml")
    repomd = _download_bytes(repomd_url, timeout=60)
    root = ET.fromstring(repomd)
    for data in root.findall("repo:data", REPOMD_NS):
        if data.get("type") == "primary":
            location = data.find("repo:location", REPOMD_NS)
            if location is None or not location.get("href"):
                break
            return location.get("href")
    raise RuntimeError(f"primary metadata not found in {repomd_url}")


def _decompress_metadata(raw: bytes, location: str) -> bytes:
    if location.endswith(".gz"):
        return gzip.decompress(raw)
    if location.endswith(".xz"):
        return lzma.decompress(raw)
    if location.endswith(".zst"):
        if shutil.which("zstd") is None:
            raise RuntimeError(
                "primary metadata is zstd-compressed, but the 'zstd' command is not installed"
            )
        completed = subprocess.run(
            ["zstd", "-dc"],
            input=raw,
            check=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        if completed.returncode != 0:
            raise RuntimeError(
                "failed to decompress zstd metadata: "
                + completed.stderr.decode("utf-8", errors="replace")
            )
        return completed.stdout
    return raw


def fetch_package_entries(
    repo_url: str,
    arches: Optional[Sequence[str]] = None,
    limit: Optional[int] = None,
) -> Tuple[List[PackageEntry], Dict[str, Any]]:
    """Fetch and parse RPM repository metadata.

    Only metadata is downloaded here, not RPM payloads.
    """
    normalized_repo_url = repo_url.rstrip("/") + "/"
    primary_location = _primary_metadata_location(normalized_repo_url)
    primary_url = urllib.parse.urljoin(normalized_repo_url, primary_location)
    raw_primary = _download_bytes(primary_url, timeout=180)
    primary_xml = _decompress_metadata(raw_primary, primary_location)

    arch_filter = set(arches or [])
    entries: List[PackageEntry] = []
    counts_by_arch: Dict[str, int] = {}
    size_by_arch: Dict[str, int] = {}
    selected_size = 0

    for _event, elem in ET.iterparse(io.BytesIO(primary_xml), events=("end",)):
        if elem.tag != COMMON_NS + "package":
            continue

        arch = elem.findtext(COMMON_NS + "arch") or "unknown"
        size_elem = elem.find(COMMON_NS + "size")
        package_size = int(size_elem.get("package", "0")) if size_elem is not None else 0
        counts_by_arch[arch] = counts_by_arch.get(arch, 0) + 1
        size_by_arch[arch] = size_by_arch.get(arch, 0) + package_size

        if (not arch_filter or arch in arch_filter) and (limit is None or len(entries) < limit):
            location = elem.find(COMMON_NS + "location")
            checksum_elem = elem.find(COMMON_NS + "checksum")
            href = location.get("href") if location is not None else None
            if href:
                entries.append(
                    PackageEntry(
                        name=elem.findtext(COMMON_NS + "name") or "unknown",
                        arch=arch,
                        href=href,
                        url=urllib.parse.urljoin(normalized_repo_url, href),
                        size=package_size,
                        checksum_type=checksum_elem.get("type") if checksum_elem is not None else None,
                        checksum=checksum_elem.text if checksum_elem is not None else None,
                    )
                )
                selected_size += package_size
        elem.clear()

    metadata = {
        "repo_url": normalized_repo_url,
        "primary_metadata_url": primary_url,
        "primary_metadata_compressed_bytes": len(raw_primary),
        "arches_requested": list(arches or []),
        "limit": limit,
        "package_count_total": sum(counts_by_arch.values()),
        "package_count_by_arch": counts_by_arch,
        "package_size_bytes_by_arch": size_by_arch,
        "selected_package_count": len(entries),
        "selected_package_size_bytes": selected_size,
    }
    return entries, metadata


def inspect_dataset(repo_url: str, arches: Optional[Sequence[str]], limit: Optional[int]) -> Dict[str, Any]:
    """Inspect a remote RPM repository dataset using metadata only."""
    _entries, metadata = fetch_package_entries(repo_url, arches=arches, limit=limit)
    return metadata


def _safe_relative_path(href: str) -> Path:
    decoded = urllib.parse.unquote(href).lstrip("/")
    normalized = os.path.normpath(decoded)
    if normalized == "." or normalized.startswith("../") or normalized == "..":
        raise ValueError(f"unsafe package href in repository metadata: {href}")
    return Path(normalized)


def _download_one(
    entry: PackageEntry,
    repo_dir: Path,
    force: bool,
    verify_checksum: bool,
) -> Dict[str, Any]:
    relative_path = _safe_relative_path(entry.href)
    destination = repo_dir / relative_path
    ensure_directory(destination.parent)

    if not force and destination.exists() and destination.stat().st_size == entry.size:
        if verify_checksum and entry.checksum and entry.checksum_type:
            actual = file_checksum(destination, entry.checksum_type)
            if actual != entry.checksum:
                destination.unlink()
            else:
                return {"href": entry.href, "status": "skipped", "bytes": entry.size}
        else:
            return {"href": entry.href, "status": "skipped", "bytes": entry.size}

    temporary = destination.with_suffix(destination.suffix + ".tmp")
    with _urlopen(entry.url, timeout=300) as response, temporary.open("wb") as handle:
        shutil.copyfileobj(response, handle, length=1024 * 1024)

    actual_size = temporary.stat().st_size
    if entry.size and actual_size != entry.size:
        temporary.unlink(missing_ok=True)
        raise RuntimeError(
            f"downloaded size mismatch for {entry.href}: "
            f"expected {entry.size}, got {actual_size}"
        )

    if verify_checksum and entry.checksum and entry.checksum_type:
        actual = file_checksum(temporary, entry.checksum_type)
        if actual != entry.checksum:
            temporary.unlink(missing_ok=True)
            raise RuntimeError(f"checksum mismatch for {entry.href}")

    temporary.replace(destination)
    return {"href": entry.href, "status": "downloaded", "bytes": entry.size}


def prepare_dataset(
    repo_url: str,
    repo_dir: Path,
    arches: Optional[Sequence[str]],
    limit: Optional[int],
    workers: int,
    force: bool = False,
    verify_checksum: bool = False,
    dataset_name: Optional[str] = None,
) -> Dict[str, Any]:
    """Download RPMs for the benchmark dataset into *repo_dir*.

    This is setup work and should not be included in timed benchmark results.
    """
    ensure_directory(repo_dir)
    entries, metadata = fetch_package_entries(repo_url, arches=arches, limit=limit)
    if not entries:
        raise RuntimeError("no packages selected from repository metadata")

    started_at = utc_now()
    print(
        f"Preparing {len(entries)} packages in {repo_dir} "
        f"from {metadata['repo_url']} with {workers} workers"
    )
    counters: Dict[str, int] = {"downloaded": 0, "skipped": 0, "failed": 0}
    bytes_seen = 0
    failures: List[Dict[str, Any]] = []

    with ThreadPoolExecutor(max_workers=workers) as executor:
        future_by_entry = {
            executor.submit(_download_one, entry, repo_dir, force, verify_checksum): entry
            for entry in entries
        }
        for index, future in enumerate(as_completed(future_by_entry), start=1):
            entry = future_by_entry[future]
            try:
                result = future.result()
                counters[result["status"]] = counters.get(result["status"], 0) + 1
                bytes_seen += int(result.get("bytes") or 0)
            except Exception as error:  # pylint: disable=broad-exception-caught
                counters["failed"] += 1
                failures.append({"href": entry.href, "error": str(error)})
            if index == len(entries) or index % 100 == 0:
                print(
                    f"  {index}/{len(entries)} complete "
                    f"downloaded={counters.get('downloaded', 0)} "
                    f"skipped={counters.get('skipped', 0)} "
                    f"failed={counters.get('failed', 0)}"
                )

    finished_at = utc_now()
    manifest = {
        "workload": "repodata_generation",
        "dataset_name": dataset_name,
        "repo_dir": str(repo_dir),
        "started_at": started_at,
        "finished_at": finished_at,
        "download_counters": counters,
        "downloaded_or_existing_bytes": bytes_seen,
        "failures": failures,
        "metadata": metadata,
        "packages": [asdict(entry) for entry in entries],
    }
    state_dir = ensure_directory(repo_dir / ".uyuni-bench")
    write_json(state_dir / "dataset.json", manifest)

    if failures:
        raise RuntimeError(f"{len(failures)} package downloads failed; see dataset manifest")
    return manifest


def _drop_caches() -> None:
    completed = subprocess.run(
        ["sh", "-c", "sync && echo 3 > /proc/sys/vm/drop_caches"],
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    if completed.returncode != 0:
        print("WARNING: failed to drop caches: " + completed.stderr.strip())


def _createrepo_version() -> Optional[str]:
    return command_output(["createrepo_c", "--version"])


def _run_createrepo(
    repo_dir: Path,
    mode: str,
    iteration_dir: Path,
    extra_args: Optional[Sequence[str]] = None,
) -> Dict[str, Any]:
    if shutil.which("createrepo_c") is None:
        raise RuntimeError("createrepo_c is not installed or not in PATH")

    if mode == "full":
        shutil.rmtree(repo_dir / "repodata", ignore_errors=True)

    createrepo_command = ["createrepo_c"]
    if mode == "update":
        createrepo_command.append("--update")
    createrepo_command.extend(extra_args or [])
    createrepo_command.append(str(repo_dir))

    stdout_path = iteration_dir / "createrepo_stdout.log"
    stderr_path = iteration_dir / "createrepo_stderr.log"
    time_path = iteration_dir / "time_verbose.log"

    time_binary = "/usr/bin/time" if Path("/usr/bin/time").exists() else shutil.which("time")
    command = list(createrepo_command)
    if time_binary:
        command = [str(time_binary), "-v", "-o", str(time_path)] + command

    start = time.monotonic()
    completed = subprocess.run(
        command,
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )
    duration = time.monotonic() - start

    stdout_path.write_text(completed.stdout, encoding="utf-8", errors="replace")
    stderr_path.write_text(completed.stderr, encoding="utf-8", errors="replace")

    return {
        "command": command,
        "createrepo_command": createrepo_command,
        "duration_seconds": round(duration, 3),
        "exit_code": completed.returncode,
        "stdout_path": str(stdout_path),
        "stderr_path": str(stderr_path),
        "time_verbose_path": str(time_path) if time_path.exists() else None,
        "time_metrics": parse_gnu_time_verbose(time_path),
    }


def run_repodata_benchmark(
    repo_dir: Path,
    results_dir: Path,
    storage_backend: str,
    mode: str,
    iterations: int,
    drop_caches: bool = False,
    extra_createrepo_args: Optional[Sequence[str]] = None,
) -> Dict[str, Any]:
    """Run the timed repodata generation workload."""
    if mode not in {"full", "update"}:
        raise ValueError("mode must be 'full' or 'update'")
    if iterations < 1:
        raise ValueError("iterations must be >= 1")
    if not repo_dir.exists():
        raise RuntimeError(f"repository directory does not exist: {repo_dir}")

    ensure_directory(results_dir)
    dataset_manifest_path = repo_dir / ".uyuni-bench" / "dataset.json"
    dataset_manifest = read_json(dataset_manifest_path) if dataset_manifest_path.exists() else {}

    summary: Dict[str, Any] = {
        "workload": "repodata_generation",
        "tool_version": __version__,
        "started_at": utc_now(),
        "storage_backend": storage_backend,
        "mode": mode,
        "iterations": iterations,
        "repo_dir": str(repo_dir),
        "results_dir": str(results_dir),
        "host": host_facts(),
        "createrepo_c_version": _createrepo_version(),
        "dataset": {
            "dataset_name": dataset_manifest.get("dataset_name"),
            "metadata": dataset_manifest.get("metadata", {}),
        },
        "results": [],
    }

    for iteration in range(1, iterations + 1):
        print(f"Running repodata benchmark iteration {iteration}/{iterations} ({mode})")
        iteration_dir = ensure_directory(results_dir / f"iteration-{iteration:02d}")
        if drop_caches:
            _drop_caches()
        if mode == "full":
            shutil.rmtree(repo_dir / "repodata", ignore_errors=True)

        iteration_started_at = utc_now()
        before = {
            "rpm_count": count_files(repo_dir, "*.rpm"),
            "repo_size_bytes": directory_size_bytes(repo_dir),
        }
        run_result = _run_createrepo(repo_dir, mode, iteration_dir, extra_createrepo_args)
        after = {
            "repodata_size_bytes": directory_size_bytes(repo_dir / "repodata"),
            "repodata_file_count": count_files(repo_dir / "repodata", "*"),
            "repo_size_bytes": directory_size_bytes(repo_dir),
        }
        result = {
            "iteration": iteration,
            "started_at": iteration_started_at,
            "before": before,
            "run": run_result,
            "after": after,
        }
        summary["results"].append(result)
        write_json(iteration_dir / "result.json", result)
        if run_result["exit_code"] != 0:
            summary["finished_at"] = utc_now()
            write_json(results_dir / "summary.json", summary)
            raise RuntimeError(
                f"createrepo_c failed in iteration {iteration}; "
                f"see {run_result['stderr_path']}"
            )

    summary["finished_at"] = utc_now()
    write_json(results_dir / "summary.json", summary)
    return summary


def clean_repodata(repo_dir: Path) -> Dict[str, Any]:
    """Remove generated repodata from *repo_dir*."""
    repodata_dir = repo_dir / "repodata"
    existed = repodata_dir.exists()
    shutil.rmtree(repodata_dir, ignore_errors=True)
    return {"repo_dir": str(repo_dir), "removed": str(repodata_dir), "existed": existed}
