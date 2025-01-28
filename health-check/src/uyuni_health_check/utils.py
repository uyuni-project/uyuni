import subprocess
from typing import List
from rich.console import Console
from rich.text import Text


console = Console()

def run_command(cmd: List[str], verbose=False, raise_exc=True) -> List:
    """
    Runs a command
    """
    if verbose:
        console.log(f"Executing: {' '.join(cmd)}")

    process = subprocess.run(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        stdin=subprocess.DEVNULL,
        universal_newlines=True,
    )

    stdout, stderr, retcode = process.stdout, process.stderr, process.returncode

    _handle_text_from_process(verbose, stdout, stderr)
    if raise_exc:
        _check_retcode(retcode)

    return [stdout, stderr, retcode]

def _handle_text_from_process(verbose: bool, *objs: str):
    if verbose:
        for obj in objs:
           console.log(Text.from_ansi(obj.strip()))

def _check_retcode(retcode: int):
    match retcode:
        case 0: ... # success
        case 127: raise OSError("Command not found; podman is required")
        case _:
            raise HealthException("An error happened while running Podman")


class HealthException(Exception):
    def __init__(self, message):
        super().__init__(message)
