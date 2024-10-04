from uyuni_health_check.containers.manager import podman
from rich.markdown import Markdown
from datetime import datetime, timedelta
from rich import print


def show_full_error_logs(since, loki=None):
    """
    Get and show the error logs
    """
    print()
    print(Markdown(f"- Getting error messages over the last {since} days..."))
    from_time = (datetime.utcnow() - timedelta(days=since)).isoformat()
    loki_url = loki or "http://uyuni_health_check_loki:3100"
    podman(
        [
            "run",
            "--rm",
            "--replace",
            "--network",
            "health-check-network",
            "--name",
            "uyuni_health_check_logcli",
            "logcli",
            "query",
            "--quiet",
            "--output=jsonl",
            f"--addr={loki_url}",
            f"--from={from_time}Z",
            "--limit=150",
            #            '"{job=~\".+\"}"',
            '{job=~".+"} |~ `(?i)error|(?i)severe|(?i)critical|(?i)fatal`',
        ],
        quiet=False,
        use_print=True,
    )
    print()
