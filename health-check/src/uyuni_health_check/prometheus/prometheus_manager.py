from uyuni_health_check.config_loader import ConfigLoader
from uyuni_health_check.containers.manager import (
    console,
    build_image,
    image_exists,
    container_is_running,
    podman,
)
from uyuni_health_check.utils import run_command, HealthException, console

conf = ConfigLoader()


def prepare_prometheus(verbose=False):
    if container_is_running("uyuni-health-check-prometheus"):
        console.log(
            "Skipped as the uyuni-health-check-prometheus container is already running"
        )
    else:
        # Copy the prometheus config

        prometheus_cfg = conf.get_prometheus_config_dir()
        podman_command = [
            "run",
            "-d",
            "--replace",
            "--network",
            "health-check-network",
            "-p",
            "9090:9090",
            "-v",
            f"{prometheus_cfg}:/etc/prometheus/",
            "--name",
            "uyuni-health-check-prometheus",
            "docker.io/prom/prometheus",
        ]
        console.log(" ".join(podman_command))
        podman(podman_command)
