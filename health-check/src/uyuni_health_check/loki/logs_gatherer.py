from uyuni_health_check.containers.manager import podman
from uyuni_health_check.utils import run_command, HealthException, console
from uyuni_health_check.outputter import outputter
from config_loader import ConfigLoader
from rich.markdown import Markdown
from datetime import datetime, timedelta
from rich import print
import json
from json.decoder import JSONDecodeError

conf = ConfigLoader()

def show_full_error_logs(from_datetime=None, to_datetime=None, since=7, loki=None):
    """
    Get and show the error logs
    """
    print()
    print(Markdown(f"- Getting error messages over the last {since} days..."))
    #from_time = (datetime.utcnow() - timedelta(days=since)).isoformat()
    #loki_url = loki or "http://uyuni_health_check_loki:3100"
    loki_container_name = conf.global_config['loki']['loki_container_name']
    loki_port = conf.global_config['loki']['loki_port']
    loki_url = f"http://{loki_container_name}:{loki_port}"
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
            f"--from={from_datetime}",
            f"--to={to_datetime}",
            "--limit=150",
            '{job=~".+"} |~ `(?i)error|(?i)severe|(?i)critical|(?i)fatal`',
        ],
        quiet=False,
        use_print=True,
    )
    print()
    

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
            'health-check-network',
            "--name",
            f"{logcli_container_name}",
            f"{logcli_image_name}",
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
    
