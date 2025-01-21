import subprocess
from rich.console import Console
from rich.text import Text


console = Console()

def run_command(cmd, console=None, quiet=True, use_print=False):
    """
    Runs a command
    """
    console = Console()
    print("cmd parameter: ", cmd )
    print(" Running: " + ' '.join(cmd))
    process = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        stdin=subprocess.DEVNULL,
        universal_newlines=True,
    )

    stdout, stderr = process.communicate()

    if console and not quiet:
        for line in stdout.splitlines():
            if use_print:
                console.print_json(line.strip())
            else:
                console.log(Text.from_ansi(line.strip()))
        
        for line in stderr.splitlines():
            if use_print:
                console.print_json(line.strip())
            else:
                console.log(Text.from_ansi(line.strip()))

    returncode = process.returncode

    if returncode == 127:
        raise OSError(f"Command not found: {cmd[0]}")
    elif returncode == 125:
        raise HealthException(
            "An error had happened while running Podman. Maybe you don't have enough privileges to run it."
        )
    elif returncode == 255:
        raise HealthException(f"There has been an error running: {cmd}")

    return [stdout, stderr, returncode]


class HealthException(Exception):
    def __init__(self, message):
        super().__init__(message)

if __name__ == "__main__":
    print("hola")
    cmd = ['podman', 'run', '--rm', '--replace', '--network', 'health-check-network', '--name', 'uyuni_health_check_logcli', 'logcli', 'query', '--quiet', '--output=jsonl', '--addr="http://uyuni_health_check_loki:3100"', '--from="2024-09-16T00:00:00Z"', '--to="2024-09-17T20:00:00Z"', '--limit=150', '\'{job="apache"}\'']
    run_command(cmd, use_print=True)