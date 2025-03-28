"""A module that manages the Grafana container"""

import json
from health_check import config
from health_check.utils import console
from health_check.containers.manager import (
    container_is_running,
    podman,
)


def prepare_grafana(from_datetime: str, to_datetime: str, verbose: bool):
    name = config.load_prop("grafana.container_name")
    image = config.load_prop("grafana.image")
    console.log("[bold]Deploying Grafana")

    if container_is_running(name):
        console.log(f"[yellow]Skipped; {name} container is already running")
        return

    grafana_cfg = config.get_config_dir_path("grafana")
    grafana_dasthboard_template = config.get_json_template_filepath(
        "grafana_dashboard/supportconfig_with_logs.template.json"
    )
    render_grafana_dashboard_cfg(
        grafana_dasthboard_template, from_datetime, to_datetime
    )

    podman(
        [
            "run",
            "--replace",
            "--detach",
            "--network",
            config.load_prop("podman.network_name"),
            "--publish",
            "3000:3000",
            "--volume",
            f"{grafana_cfg}/alerts.yaml:/etc/grafana/provisioning/alerting/alerts.yaml",
            "--volume",
            f"{grafana_cfg}/datasources.yaml:/etc/grafana/provisioning/datasources/ds.yaml",
            "--volume",
            f"{grafana_cfg}/dashboard.yaml:/etc/grafana/provisioning/dashboards/main.yaml",
            "--volume",
            f"{grafana_cfg}/dashboards:/var/lib/grafana/dashboards",
            "--name",
            name,
            image,
        ],
        verbose,
    )


def render_grafana_dashboard_cfg(
    grafana_dashboard_template, from_datetime, to_datetime
):
    """
    Render grafana dashboard file
    """

    with open(grafana_dashboard_template, "r", encoding="UTF-8") as f:
        data = json.load(f)
        data["time"]["from"] = from_datetime
        data["time"]["to"] = to_datetime
        config.write_config(
            "grafana/dashboards", "supportconfig_with_logs.json", data, is_json=True
        )
