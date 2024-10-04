import subprocess
from config_loader import ConfigLoader
from uyuni_health_check.utils import run_command, HealthException, console
from containers.manager import (
    console,
    build_image,
    image_exists,
    container_is_running,
    podman,
)

conf = ConfigLoader()


def prepare_grafana(verbose=False):
    if container_is_running("uyuni-health-check-grafana"):
        console.log(
            "Skipped as the uyuni-health-check-grafana container is already running"
        )
    else:

        grafana_cfg = conf.get_grafana_config_dir()
        # Run the container
        podman(
            [
                "run",
                "--replace",
                "-d",
                "--network",
                "health-check-network",
                "-p",
                "3000:3000",
                "-v",
                f"{grafana_cfg}/datasources.yaml:/etc/grafana/provisioning/datasources/ds.yaml",
                "-v",
                f"{grafana_cfg}/dashboard.yaml:/etc/grafana/provisioning/dashboards/main.yaml",
                "-v",
                f"{grafana_cfg}/dashboards:/var/lib/grafana/dashboards",
                "-e",
                "GF_PATHS_PROVISIONING=/etc/grafana/provisioning",
                "-e",
                "GF_AUTH_ANONYMOUS_ENABLED=true",
                "-e",
                "GF_AUTH_ANONYMOUS_ORG_ROLE=Admin",
                "--name",
                "uyuni-health-check-grafana",
                "docker.io/grafana/grafana:9.2.1",
                "run.sh",
            ],
        )
