from uyuni_health_check import config
from uyuni_health_check.containers.manager import (
    console,
    container_is_running,
    podman,
)
from uyuni_health_check.utils import console


def prepare_prometheus(verbose=False):
    if container_is_running("uyuni-health-check-prometheus"):
        console.log(
            "Skipped as the uyuni-health-check-prometheus container is already running"
        )
    else:
        podman_command = [
            "run",
            "-d",
            "--replace",
            "--network",
            "health-check-network",
            "-p",
            "9090:9090",
            "-v",
            f"{config.get_prometheus_config_dir()}:/etc/prometheus/",
            "--name",
            "uyuni-health-check-prometheus",
            "docker.io/prom/prometheus",
        ]
        console.log(" ".join(podman_command))
        podman(podman_command)
