import subprocess
from rich.console import Console
from rich.text import Text


console = Console()


def run_command(cmd, console=None, quiet=True, use_print=False):
    """
    Runs a command
    """
    process = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        stdin=subprocess.DEVNULL,
        universal_newlines=True,
    )

    if console and not quiet:
        while True:
            line = process.stdout.readline() or process.stderr.readline()
            if not line:
                break
            if use_print:
                console.print_json(line.strip())
            else:
                console.log(Text.from_ansi(line.strip()))

    returncode = process.wait()
    if returncode == 127:
        raise OSError(f"Command not found: {cmd[0]}")
    elif returncode == 125:
        raise HealthException(
            "An error had happened while running Podman. Maybe you don't have enough privileges to run it."
        )
    elif returncode == 255:
        raise HealthException(f"There has been an error running: {cmd}")
    return process


class HealthException(Exception):
    def __init__(self, message):
        super().__init__(message)
