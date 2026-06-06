# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

"""CLI for Uyuni real-world benchmark workloads."""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Sequence, Tuple

from uyuni_bench import __version__
from uyuni_bench.utils import read_json, utc_now, write_json
from uyuni_bench.workloads.repodata import (
    clean_repodata,
    inspect_dataset,
    prepare_dataset,
    run_repodata_benchmark,
)


def _default_base_dir() -> Path:
    return Path(__file__).resolve().parents[1]


def _load_dataset(path: Optional[Path]) -> Dict[str, Any]:
    if path is None:
        return {}
    return read_json(path)


def _dataset_options(args: argparse.Namespace) -> Tuple[str, List[str], Optional[int], Optional[str]]:
    dataset = _load_dataset(getattr(args, "dataset", None))
    repo_url = args.repo_url or dataset.get("repo_url")
    if not repo_url:
        raise SystemExit("error: --repo-url is required unless --dataset provides repo_url")

    arches = args.arch or dataset.get("arches") or []
    limit = args.limit if args.limit is not None else dataset.get("limit")
    dataset_name = args.dataset_name or dataset.get("name")
    return repo_url, list(arches), limit, dataset_name


def _print_json(data: Dict[str, Any]) -> None:
    print(json.dumps(data, indent=2, sort_keys=True))


def _cmd_repodata_inspect(args: argparse.Namespace) -> int:
    repo_url, arches, limit, dataset_name = _dataset_options(args)
    result = inspect_dataset(repo_url, arches=arches, limit=limit)
    if dataset_name:
        result["dataset_name"] = dataset_name
    _print_json(result)
    return 0


def _cmd_repodata_prepare(args: argparse.Namespace) -> int:
    repo_url, arches, limit, dataset_name = _dataset_options(args)
    result = prepare_dataset(
        repo_url=repo_url,
        repo_dir=args.repo_dir,
        arches=arches,
        limit=limit,
        workers=args.workers,
        force=args.force,
        verify_checksum=args.verify_checksum,
        dataset_name=dataset_name,
    )
    print(f"Dataset ready: {args.repo_dir}")
    print(f"Manifest: {args.repo_dir / '.uyuni-bench' / 'dataset.json'}")
    if args.output:
        write_json(args.output, result)
    return 0


def _cmd_repodata_run(args: argparse.Namespace) -> int:
    results_dir = args.results_dir
    if results_dir is None:
        timestamp = utc_now().replace(":", "").replace("+00:00", "Z")
        results_dir = _default_base_dir() / "results" / "repodata" / f"{args.storage_backend}-{timestamp}"

    result = run_repodata_benchmark(
        repo_dir=args.repo_dir,
        results_dir=results_dir,
        storage_backend=args.storage_backend,
        mode=args.mode,
        iterations=args.iterations,
        drop_caches=args.drop_caches,
        extra_createrepo_args=args.createrepo_arg,
    )
    print(f"Benchmark summary: {results_dir / 'summary.json'}")
    if args.output:
        write_json(args.output, result)
    return 0


def _cmd_repodata_clean(args: argparse.Namespace) -> int:
    result = clean_repodata(args.repo_dir)
    _print_json(result)
    return 0


def _add_dataset_arguments(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--dataset",
        type=Path,
        help="JSON dataset definition containing repo_url, arches and optional limit",
    )
    parser.add_argument("--repo-url", help="base URL of an RPM repository")
    parser.add_argument(
        "--arch",
        action="append",
        help="package architecture to select; can be passed multiple times",
    )
    parser.add_argument(
        "--limit",
        type=int,
        help="limit selected packages, useful for smoke tests",
    )
    parser.add_argument("--dataset-name", help="human-readable dataset name for result metadata")


def _build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        prog="uyuni-bench",
        description="Uyuni real-world storage benchmark workloads",
    )
    parser.add_argument("--version", action="version", version=f"uyuni-bench {__version__}")

    subcommands = parser.add_subparsers(dest="workload", required=True)

    repodata = subcommands.add_parser(
        "repodata",
        help="RPM repository metadata generation workload",
    )
    repodata_subcommands = repodata.add_subparsers(dest="repodata_command", required=True)

    inspect = repodata_subcommands.add_parser(
        "inspect",
        help="inspect remote repository metadata without downloading RPMs",
    )
    _add_dataset_arguments(inspect)
    inspect.set_defaults(func=_cmd_repodata_inspect)

    prepare = repodata_subcommands.add_parser(
        "prepare",
        help="download a fixed RPM dataset into a benchmark repository directory",
    )
    _add_dataset_arguments(prepare)
    prepare.add_argument("--repo-dir", type=Path, required=True, help="target repository directory")
    prepare.add_argument("--workers", type=int, default=8, help="parallel RPM downloads")
    prepare.add_argument("--force", action="store_true", help="re-download existing RPMs")
    prepare.add_argument(
        "--verify-checksum",
        action="store_true",
        help="verify package checksums after download; slower for large datasets",
    )
    prepare.add_argument("--output", type=Path, help="optional copy of the dataset manifest JSON")
    prepare.set_defaults(func=_cmd_repodata_prepare)

    run = repodata_subcommands.add_parser(
        "run",
        help="run timed createrepo_c repodata generation against an existing dataset",
    )
    run.add_argument("--repo-dir", type=Path, required=True, help="repository directory containing RPMs")
    run.add_argument(
        "--results-dir",
        type=Path,
        help="directory for benchmark logs and summary.json",
    )
    run.add_argument(
        "--storage-backend",
        required=True,
        help="label for the storage backend under test, e.g. local-path, longhorn-3replica",
    )
    run.add_argument(
        "--mode",
        choices=["full", "update"],
        default="full",
        help="full removes repodata first; update runs createrepo_c --update",
    )
    run.add_argument("--iterations", type=int, default=1, help="number of timed repetitions")
    run.add_argument(
        "--drop-caches",
        action="store_true",
        help="try to drop Linux page cache before each iteration; requires privileges",
    )
    run.add_argument(
        "--createrepo-arg",
        action="append",
        default=[],
        help="extra argument passed to createrepo_c; can be passed multiple times",
    )
    run.add_argument("--output", type=Path, help="optional copy of summary JSON")
    run.set_defaults(func=_cmd_repodata_run)

    clean = repodata_subcommands.add_parser(
        "clean",
        help="remove generated repodata from a repository directory",
    )
    clean.add_argument("--repo-dir", type=Path, required=True)
    clean.set_defaults(func=_cmd_repodata_clean)

    return parser


def main(argv: Optional[Sequence[str]] = None) -> int:
    parser = _build_parser()
    args = parser.parse_args(argv)
    try:
        return args.func(args)
    except KeyboardInterrupt:
        print("Interrupted", file=sys.stderr)
        return 130
    except Exception as error:  # pylint: disable=broad-exception-caught
        print(f"ERROR: {error}", file=sys.stderr)
        return 1
