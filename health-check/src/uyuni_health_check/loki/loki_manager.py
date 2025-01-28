import io
import os
import requests
import zipfile
import re
import time
from uyuni_health_check import config
from uyuni_health_check.utils import HealthException, console
from uyuni_health_check.containers.manager import (
    build_image,
    image_exists,
    container_is_running,
    podman,
)

# Update this number if adding more targets to the promtail config
PROMTAIL_TARGETS = 6

# Max number of seconds to wait for Loki to be ready
LOKI_WAIT_TIMEOUT = 120


def download_component_build_image(image:str, verbose=False):
    if image_exists(image):
        return

    console.log("[bold]Building Promtail image")
    url = "https://github.com/grafana/loki/releases/download/v3.3.0/promtail-linux-amd64.zip"
    dest_dir = config.load_dockerfile_dir("promtail")
    response = requests.get(url)
    zip_archive = zipfile.ZipFile(io.BytesIO(response.content))
    zip_archive.extract("promtail-linux-amd64", dest_dir)
    build_image(image, dest_dir, verbose=verbose)
    console.log(f"[green]The {image} image was built successfully")


def run_loki(supportconfig_path=None, verbose=False):
    """
    Run promtail and loki to aggregate the logs
    """
    loki_name = config.load_prop("loki.container_name")
    network = config.load_prop("podman.network_name")

    console.log("[bold]Deploying Promtail and Loki containers")

    if container_is_running(loki_name):
        console.log("[yellow]Skipped, Loki container already exists")
        return

    promtail_template = config.load_jinja_template("promtail/promtail.yaml.j2")
    render_promtail_cfg(supportconfig_path, promtail_template)
    podman(
        [
            "run",
            "--replace",
            "--detach",
            "--network",
            network,
            "--publish",
            "3100:3100",
            "--name",
            loki_name,
            "--volume",
            f"{config.get_config_file_path('loki')}:/etc/loki/local-config.yaml",
            config.load_prop("loki.image"),
        ],
        verbose,
    )

    # Run promtail only now since it pushes data to loki
    promtail_image = config.load_prop("promtail.image")

    download_component_build_image(promtail_image, verbose=verbose)
    podman_args = [
        "run",
        "--replace",
        "--network",
        network,
        "--publish",
        "9081:9081",
        "--detach",
        "--volume",
        f"{config.get_config_file_path('promtail')}:/etc/promtail/config.yml",
        "--volume",
        f"{supportconfig_path}:{supportconfig_path}",
        "--name",
        config.load_prop("promtail.container_name"),
        promtail_image,
    ]

    podman(podman_args, verbose)


def render_promtail_cfg(supportconfig_path=None, promtail_template=None):
    """
    Render promtail configuration file

    :param supportconfig_path: render promtail configuration based on this path to a supportconfig
    """

    if supportconfig_path:
        opts = {
            "rhn_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/rhn-logs/rhn/"
            ),
            "cobbler_logs_file": os.path.join(
                supportconfig_path, "spacewalk-debug/cobbler-logs/cobbler.log"
            ),
            "salt_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/salt-logs/salt/"
            ),
            "postgresql_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/database/"
            ),
            "apache2_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/httpd-logs/apache2/"
            ),
        }
    else:
        opts = {
            "rhn_logs_path": "/var/log/rhn/",
            "cobbler_logs_file": "/var/log/cobbler.log",
            "salt_logs_path": "/var/log/salt/",
            "apache2_logs_path": "/var/log/apache2/",
            "postgresql_logs_path": "/var/lib/pgsql/data/log/",
        }

    # Write rendered promtail configuration file
    config.write_config("promtail", "config.yaml", promtail_template.render(**opts))



def check_series_in_loki(loki_url, job_name="promtail-complete-job", flag="complete", message="Promtail finished!d"):
    query = f'{{job="{job_name}", flag="{flag}"}} |= "{message}"'
    end = int(time.time())
    start = end - 60 * 60

    response = requests.get(
        f"{loki_url}/loki/api/v1/query",
        params={"query": query, "start": start * 1_000_000_000, "end": end * 1_000_000_000}
    )

    if response.status_code == 200:
        data = response.json()
        return len(data['data']['result']) > 0
    else:
        print("Failed to query Loki:", response.text)
        return False

def wait_promtail_init():
    loki_url = "http://localhost:3100"
    start_time = time.time()
    timeout = 60
    console.log("[bold]Waiting for Promtail to process logs")

    while not check_series_in_loki(loki_url):
        elapsed_time = time.time() - start_time
        if elapsed_time >= timeout:
            console.log("Timeout waiting for promtail to finish!")
            break
        time.sleep(10)
    console.log("Promtail finished processing logs")

def wait_loki_init(verbose=False):
    """
    Try to figure out when loki is ready to answer our requests.
    There are two things to wait for:
      - loki to be up
      - promtail to have read the logs and the loki ingester having handled them
    """
    metrics = None
    timeout = False
    request_message_bytes_sum = 0
    loki_ingester_chunk_entries_count = 0
    start_time = time.time()
    ready = False

    # Wait for promtail to be ready
    # TODO Add a timeout here in case something went really bad
    # TODO checking the lags won't work when working on older logs,
    # we could try to compare the positions with the size of the files in such a case
    while (
        not metrics
        or metrics["active"] < PROMTAIL_TARGETS
        or (not metrics["lags"] and metrics["active_files"] == 0)
        or any([v >= 10 for v in metrics["lags"].values()])
        or (metrics["lags"] and metrics["active_files"])
        or (metrics["encoded_bytes_total"] == metrics["sent_bytes_total"] == 0)
        or (metrics["encoded_bytes_total"] != metrics["sent_bytes_total"] != 0)
        or (
            metrics["encoded_bytes_total"]
            != metrics["sent_bytes_total"]
            != request_message_bytes_sum
        )
        or loki_ingester_chunk_entries_count == 0
        or not ready
        and not timeout
    ):
        if verbose:
            console.log("Waiting for promtail metrics to be collected")
        time.sleep(5)
        response = requests.get("http://localhost:3100/metrics")
        if verbose:
            console.log("loki metrics 3100 status code", response.status_code)
        if response.status_code == 200:
            content = response.content.decode()
            request_message_bytes_sum = re.findall(
                'loki_request_message_bytes_sum{.*"loki_api_v1_push"} (\d+)', content
            )
            request_message_bytes_sum = (
                int(request_message_bytes_sum[0]) if request_message_bytes_sum else 0
            )
            loki_ingester_chunk_entries_count = re.findall(
                "loki_ingester_chunk_entries_count (\d+)", content
            )
            loki_ingester_chunk_entries_count = (
                int(loki_ingester_chunk_entries_count[0])
                if loki_ingester_chunk_entries_count
                else 0
            )

        response = requests.get("http://localhost:9081/metrics")
        if verbose:
            console.log("promtail metrics 9081 status code", response.status_code)
        if response.status_code == 200:
            content = response.content.decode()
            active = re.findall("promtail_targets_active_total (\d+)", content)
            encoded_bytes_total = re.findall(
                "promtail_encoded_bytes_total{.*} (\d+)", content
            )
            sent_bytes_total = re.findall(
                "promtail_sent_bytes_total{.*} (\d+)", content
            )
            active_files = re.findall("promtail_files_active_total (\d+)", content)
            lags = re.findall(
                'promtail_stream_lag_seconds{filename="([^"]+)".*} ([0-9.]+)', content
            )
            metrics = {
                "lags": {row[0]: float(row[1]) for row in lags},
                "active": int(active[0]) if active else 0,
                "active_files": int(active_files[0]) if active_files else 0,
                "encoded_bytes_total": (
                    int(encoded_bytes_total[0]) if encoded_bytes_total else 0
                ),
                "sent_bytes_total": int(sent_bytes_total[0]) if sent_bytes_total else 0,
            }

        # check if loki is ready
        if verbose:
            console.log("Waiting for loki to be ready")
        response = requests.get("http://localhost:3100/ready")
        if verbose:
            console.log("loki 3100 status code", response.status_code)
        if response.status_code == 200:
            content = response.content.decode()
            if content == "ready\n":
                ready = True

        # check if promtail is ready
        if verbose:
            console.log("Waiting for promtail to be ready")
        response = requests.get(f"http://localhost:9081/ready")
        if verbose:
            console.log("promtail ready 9081 status code", response.status_code)
        if response.status_code == 200:
            content = response.content.decode()
            if content == "Ready":
                ready = True
            else:
                ready = False

        
        # check timeout
        if (time.time() - start_time) > LOKI_WAIT_TIMEOUT:
            timeout = True
    if timeout:
        raise HealthException(
            "[red bold]Timeout has been reached waiting for Loki and promtail. Something unexpected may happen. Please check and try again."
        )
    else:
        console.print(metrics)
        console.print(loki_ingester_chunk_entries_count)
        console.print(request_message_bytes_sum)
        console.log("[bold]Loki and promtail are now ready to receive requests")

        
    