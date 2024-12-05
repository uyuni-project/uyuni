import subprocess
import json
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


def prepare_grafana(from_datetime=None, to_datetime=None, verbose=False, config=None):
    if container_is_running("uyuni-health-check-grafana"):
        console.log(
            "Skipped as the uyuni-health-check-grafana container is already running"
        )
    else:
        build_grafana_image("health-check-grafana", config)
        grafana_cfg = conf.get_config_dir_path("grafana")
        console.log("GRAFANA CFG DIR: ",grafana_cfg)
        grafana_dasthboard_template = config.get_json_template_filepath("grafana_dashboard/supportconfig_with_logs.template.json")
        render_grafana_dashboard_cfg(grafana_dasthboard_template, from_datetime, to_datetime, config)

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
                "--name",
                "uyuni-health-check-grafana",
                "health-check-grafana",
            ],
        )

def build_grafana_image(image, config):
    console.log(f"Building {image}")
    if image_exists(image):
        console.log(f"[yellow]Skipped as the {image} image is already present")
        return

    image_path = config.load_dockerfile_dir("grafana")
    build_image(image, image_path=image_path)
    console.log(f"[green]The {image} image was built successfully")

def render_grafana_dashboard_cfg(grafana_dashboard_template, from_datetime, to_datetime, config=None):
    """
    Render grafana dashboard file
    """

    with open(grafana_dashboard_template, 'r') as f:
        data = json.load(f)
        data["time"]["from"] = from_datetime
        data["time"]["to"] = to_datetime
        config.write_config("grafana", "dashboards/supportconfig_with_logs.json", data, isjson=True)