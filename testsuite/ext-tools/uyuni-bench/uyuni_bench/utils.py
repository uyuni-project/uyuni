# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

"""Shared utility functions for Uyuni benchmark workloads."""

from __future__ import annotations

import datetime as _dt
import hashlib
import json
import os
import shutil
import socket
import subprocess
from pathlib import Path
from typing import Any, Dict, Iterable, Optional


def utc_now() -> str:
    """Return the current time as an ISO-8601 UTC string."""
    return _dt.datetime.now(tz=_dt.timezone.utc).isoformat(timespec="seconds")


def ensure_directory(path: Path) -> Path:
    """Create *path* if needed and return it."""
    path.mkdir(parents=True, exist_ok=True)
    return path


def read_json(path: Path) -> Dict[str, Any]:
    """Read a JSON object from *path*."""
    with path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)
    if not isinstance(data, dict):
        raise ValueError(f"{path} does not contain a JSON object")
    return data


def write_json(path: Path, data: Dict[str, Any]) -> None:
    """Write *data* as pretty JSON."""
    ensure_directory(path.parent)
    with path.open("w", encoding="utf-8") as handle:
        json.dump(data, handle, indent=2, sort_keys=True)
        handle.write("\n")


def command_output(command: Iterable[str]) -> Optional[str]:
    """Return stdout for a command or None if the command fails/is missing."""
    command = list(command)
    if not command or shutil.which(command[0]) is None:
        return None
    try:
        completed = subprocess.run(
            command,
            check=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
        )
    except OSError:
        return None
    if completed.returncode != 0:
        return None
    return completed.stdout.strip()


def directory_size_bytes(path: Path) -> int:
    """Return recursive file size for *path* in bytes."""
    total = 0
    if not path.exists():
        return total
    for root, _dirs, files in os.walk(path):
        root_path = Path(root)
        for file_name in files:
            file_path = root_path / file_name
            try:
                total += file_path.stat().st_size
            except OSError:
                continue
    return total


def count_files(path: Path, pattern: str) -> int:
    """Count files matching *pattern* below *path*."""
    if not path.exists():
        return 0
    return sum(1 for item in path.rglob(pattern) if item.is_file())


def sha256sum(path: Path, chunk_size: int = 1024 * 1024) -> str:
    """Compute the SHA-256 checksum of *path*."""
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        while True:
            chunk = handle.read(chunk_size)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def file_checksum(path: Path, checksum_type: str) -> str:
    """Compute checksum for *path* using *checksum_type*."""
    try:
        digest = hashlib.new(checksum_type)
    except ValueError as error:
        raise ValueError(f"unsupported checksum type: {checksum_type}") from error
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def host_facts() -> Dict[str, Any]:
    """Collect lightweight facts about the host executing the workload."""
    return {
        "hostname": socket.getfqdn(),
        "kernel": command_output(["uname", "-r"]),
        "python": command_output(["python3", "--version"]),
    }


def parse_gnu_time_verbose(path: Path) -> Dict[str, Any]:
    """Parse selected fields from `/usr/bin/time -v` output."""
    if not path.exists():
        return {}

    key_map = {
        "User time (seconds)": "user_time_seconds",
        "System time (seconds)": "system_time_seconds",
        "Percent of CPU this job got": "cpu_percent",
        "Elapsed (wall clock) time (h:mm:ss or m:ss)": "elapsed_raw",
        "Maximum resident set size (kbytes)": "max_rss_kb",
        "File system inputs": "filesystem_inputs",
        "File system outputs": "filesystem_outputs",
        "Major (requiring I/O) page faults": "major_page_faults",
        "Minor (reclaiming a frame) page faults": "minor_page_faults",
        "Voluntary context switches": "voluntary_context_switches",
        "Involuntary context switches": "involuntary_context_switches",
        "Exit status": "exit_status",
    }
    parsed: Dict[str, Any] = {}
    for line in path.read_text(encoding="utf-8", errors="replace").splitlines():
        if ":" not in line:
            continue
        key, value = line.split(":", 1)
        normalized_key = key.strip()
        if normalized_key not in key_map:
            continue
        value = value.strip()
        out_key = key_map[normalized_key]
        if out_key == "cpu_percent":
            parsed[out_key] = value.rstrip("%")
        elif out_key.endswith("_raw"):
            parsed[out_key] = value
        else:
            try:
                parsed[out_key] = int(value)
            except ValueError:
                try:
                    parsed[out_key] = float(value)
                except ValueError:
                    parsed[out_key] = value
    return parsed
