from uyuni_health_check.containers.manager import podman
from uyuni_health_check.utils import run_command, HealthException, console
from uyuni_health_check.outputter import outputter
from uyuni_health_check.config_loader import ConfigLoader
from rich.markdown import Markdown
from datetime import datetime, timedelta
from rich import print
import json
from json.decoder import JSONDecodeError

conf = ConfigLoader()


def show_full_error_logs(from_datetime, to_datetime, since, console: "Console", loki=None):
    """
    Get and show the error logs stats
    """
    print(
        Markdown(f"- Getting summary of errors in logs")
    )
    print()
    query = f"{{job=~\".+\"}} |~ \"(?i)error|(?i)severe|(?i)critical|(?i)fatal\""
    stdout, stderr = query_loki(from_dt=from_datetime, to_dt=to_datetime, since = since, query=query)
    lines = stdout.strip().split("\n")
    json_objects = []

    if len(lines[0]) != 0:
        for line in lines:
            try:
                json_data = json.loads(line)
                json_objects.append(json_data)
            except json.JSONDecodeError as e:
                console.print(f"[red]Failed to parse JSON:[/red] {e}")
                console.print(f"[yellow]Raw output:[/yellow] {line}")

        combined_json = json.dumps(json_objects, indent=4)
        outputter.print_paginated_json(combined_json)
    

def show_error_logs_stats(from_datetime, to_datetime, since, console: "Console", loki=None):
    """
    Get and show the error logs stats
    """
    print(
        Markdown(f"- Getting summary of errors in logs")
    )
    print()
    query = f'count_over_time({{job=~".+"}} |~ "(?i)error|(?i)severe|(?i)critical|(?i)fatal" [{since}d])'
    # Returns a JSON.
    stdout, stderr = query_loki(from_dt=from_datetime, to_dt=to_datetime, since = since, query=query)

    try:
        data = json.loads(stdout)
    except JSONDecodeError:
        raise HealthException(f"Invalid logcli response: {stdout}")
    
    outputter.show(data)

def query_loki(from_dt, to_dt, since, query):

    loki_container_name = conf.global_config['loki']['loki_container_name']
    loki_port = conf.global_config['loki']['loki_port']
    loki_url = f"http://{loki_container_name}:{loki_port}"

    network_name = conf.global_config['podman']['network_name']
    logcli_container_name = conf.global_config['logcli']['logcli_container_name']

    logcli_image_name = conf.global_config['logcli']['logcli_image_name']

    podman_args =  [
            "run",
            "--rm",
            "--replace",
            "--network",
            network_name,
            "--name",
            logcli_container_name,
            logcli_image_name,
            "query",
            "--quiet",
            "--output=jsonl",
            f"--addr={loki_url}",
            "--limit=150",
    ]

    if to_dt and not from_dt:
        # Doesn't make sense to have to without from
        raise HealthException
    
    if from_dt:
        podman_args.extend([
            f"--from={from_dt}",
        ])

        if to_dt:
            # to_dt has priority over since if the two parameters are present
            podman_args.extend([
                f"--to={to_dt}"
            ])
        
        else:
            # Since should always have a default value
            podman_args.extend([
                f"--since={since}"
            ])
    else:
        # The default "from" is "since" days ago. Since should always have a default value.
        from_time = (datetime.utcnow() - timedelta(days=since)).isoformat()
    podman_args.append(query)
    stdout, stderr, _ = podman(cmd=podman_args)
    return [stdout, stderr]
    
