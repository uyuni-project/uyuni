import json
import re
import time
from datetime import datetime, timedelta
from json.decoder import JSONDecodeError

import requests
from rich import print
from rich.columns import Columns
from rich.console import Console
from rich.markdown import Markdown
from rich.panel import Panel
from rich.table import Table
from rich.text import Text

from uyuni_health_check.utils import HealthException, run_command
from uyuni_health_check.containers.manager import podman


def show_supportconfig_metrics(metrics: dict, console: "Console"):
    if metrics:
        tables = []
        tables.append(show_salt_jobs_summary(metrics))
        tables.append(show_salt_keys_summary(metrics))
        tables.append(show_salt_master_configuration_summary(metrics))
        console.print(Columns(tables), justify="center")
    else:
        console.print(
            "[yellow]Some metrics are still missing. Wait some seconds and execute again",
            justify="center",
        )


def show_error_logs_stats(since, console: "Console", loki=None):
    """
    Get and show the error logs stats
    """
    print(
        Markdown(f"- Getting summary of errors in logs over the last {since} days...")
    )
    print()
    loki_url = loki or "http://uyuni_health_check_loki:3100"
    process = podman(
        [
            "run",
            "-ti",
            "--rm",
            "--network",
            "health-check-network",
            "--name",
            "uyuni_health_check_logcli",
            "logcli",
            "--quiet",
            f"--addr={loki_url}",
            "instant-query",
            "--limit",
            "150",
            'count_over_time({job=~".+"} |~ `(?i)error|(?i)severe|(?i)critical|(?i)fatal` ['
            + str(since)
            + "d])",
        ],
    )
    response = process.stdout.read()
    try:
        data = json.loads(response)
    except JSONDecodeError:
        raise HealthException(f"Invalid logcli response: {response}")

    if data:
        console.print(
            Panel(
                Text(
                    f"Ooops! Errors found in the last {since} days.",
                    justify="center",
                )
            ),
            style="italic red blink",
        )
        table = Table(show_header=True, header_style="bold magenta", expand=True)
        table.add_column("File")
        table.add_column("Errors")

        for metric in data:
            table.add_row(metric["metric"]["filename"], metric["value"][1])

        print(table)
    else:
        console.print(
            Panel(
                Text(
                    f"Good news! No errors detected in logs in the last {since} days.",
                    justify="center",
                )
            ),
            style="italic green",
        )


def show_salt_jobs_summary(metrics: dict):
    table = Table(show_header=True, header_style="bold magenta")
    table.add_column("Salt function name")
    table.add_column("Total")

    for metric, value in sorted(
        metrics["salt_jobs"].items(), reverse=True, key=lambda item: item[1]
    ):
        table.add_row(metric, str(int(value)))

    return table


def show_salt_keys_summary(metrics: dict):
    table = Table(show_header=True, header_style="bold magenta")
    table.add_column("Salt keys")
    table.add_column("Total")

    for metric, value in sorted(
        metrics["salt_keys"].items(), reverse=True, key=lambda item: item[1]
    ):
        table.add_row(metric, str(int(value)))

    return table


def show_salt_master_configuration_summary(metrics: dict):
    table = Table(show_header=True, header_style="bold magenta")
    table.add_column("Salt Master Configuration")
    table.add_column("Value")

    for metric, value in sorted(
        metrics["salt_master_config"].items(), reverse=True, key=lambda item: item[1]
    ):
        table.add_row(metric, str(int(value)))

    return table


def _fetch_metrics_from_exporter(
    console: "Console", host="localhost", port=9000, max_retries=5
):
    for i in range(max_retries):
        try:
            metrics_raw = requests.get(f"http://{host}:{port}").content.decode()
            return metrics_raw
        except requests.exceptions.RequestException as exc:
            if i < max_retries - 1:
                time.sleep(1)
                console.log("[italic]retrying...")
            else:
                console.log(
                    "[italic red]There was an error while fetching metrics from exporter[/italic red]"
                )
                print(f"{exc}")
                exit(1)


def fetch_metrics_from_supportconfig_exporter(
    console: "Console", host="localhost", port=9000, max_retries=5
):
    if not host:
        host = "localhost"

    metrics_raw = _fetch_metrics_from_exporter(console, host, port, max_retries)

    salt_jobs = re.findall(r'salt_jobs{fun="(.+)",jid="(.+)"} (.+)', metrics_raw)
    salt_keys = re.findall(r'salt_keys{name="(.+)"} (.+)', metrics_raw)
    salt_master_config = re.findall(
        r'salt_master_config{name="(.+)"} (.+)', metrics_raw
    )

    if not salt_jobs or not salt_keys or not salt_master_config:
        console.log(
            "[yellow]Some metrics might be still missing. Wait some seconds and execute again"
        )

    metrics = {
        "salt_jobs": {},
        "salt_keys": {},
        "salt_master_config": {},
    }

    for m in salt_jobs:
        if m[0] in metrics["salt_jobs"]:
            metrics["salt_jobs"][m[0]] += 1
        else:
            metrics["salt_jobs"][m[0]] = 1

    for m in salt_master_config:
        metrics["salt_master_config"][m[0]] = float(m[1])

    for m in salt_keys:
        metrics["salt_keys"][m[0]] = float(m[1])

    console.log("[green]metrics have been successfully collected")
    return metrics
