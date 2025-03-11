"""Utils module for various utility functions"""

from datetime import datetime, timedelta
import subprocess
from typing import List
import click
from rich.console import Console
from rich.text import Text


console = Console()


def validate_date(ctx: click.Context, param: str, date: str | None) -> str | None:
    del ctx, param
    if not date:
        return

    try:
        datetime.fromisoformat(date.replace("Z", "+00:00"))
        return date
    except ValueError as e:
        raise click.BadParameter("Date must be in ISO8601 format") from e


def get_dates(since: int) -> tuple:
    now = datetime.today()
    past = now - timedelta(days=since)
    return (str(past), str(now))


def run_command(cmd: List[str], verbose=False, raise_exc=True) -> List:
    """
    Runs a command
    """
    if verbose:
        console.log(f'Executing: {" ".join(cmd)}')

    process = subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        stdin=subprocess.DEVNULL,
        universal_newlines=True,
        check=False,
    )

    stdout, stderr, retcode = process.stdout, process.stderr, process.returncode

    _handle_text_from_process(verbose, stdout, stderr)
    if raise_exc:
        _check_retcode(retcode)

    return [stdout, stderr, retcode]


def _handle_text_from_process(verbose: bool, *objs: str):
    if verbose:
        for obj in objs:
            if obj.strip():
                console.log(Text.from_ansi(obj.strip()))


def _check_retcode(retcode: int):
    if retcode == 0:
        ...  # success
    elif retcode == 127:
        raise OSError("Command not found; podman is required")
    else:
        raise HealthException("An error happened while running Podman")


class HealthException(Exception):
    def __init__(self, message):
        super().__init__(message)
