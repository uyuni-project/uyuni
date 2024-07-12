from containers.manager import podman
from rich.markdown import Markdown
from datetime import datetime, timedelta
from rich import print

def show_full_error_logs(loki, since, console: "Console"):
    """
    Get and show the error logs
    """
    print()
    print(Markdown(f"- Error logs of the last {since} days:"))
    from_time = (datetime.utcnow() - timedelta(days=since)).isoformat()
    loki_url = loki or "http://loki:3100"
    import pdb; pdb.set_trace()
    podman(
        [
            "run",
            "--rm",
            "--replace",
            "--network",
            "health-check-network",            
            "--name",
            "logcli",
            "logcli",
            "query",
            f"--addr={loki_url}",
            f"--from={from_time}Z",
            "--limit=150",
            '{job=~".+"}',
        ],
        console=console,
    )
    print()