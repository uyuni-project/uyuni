"""Module that manages the supportconfig exporter container"""

from health_check import config
from health_check.utils import console
from health_check.containers.manager import (
    podman,
    container_is_running,
)


def prepare_exporter(supportconfig_path: str, verbose: bool):
    """
    Build the exporter image and deploy it on the server

    :param server: the server to deploy the exporter on
    """
    exporter_name = config.load_prop("exporter.container_name")
    image = config.load_prop("exporter.image")
    console.log("[bold]Deploying supportconfig exporter")

    if container_is_running(f"{exporter_name}"):
        console.log(f"[yellow]Skipped; {exporter_name} container already exists")
        return

    create_supportconfig_exporter_cfg(supportconfig_path)

    console.log(f"[bold]Deploying {exporter_name} container")
    podman_args = [
        "run",
        "--replace",
        "--detach",
        "--network",
        config.load_prop("podman.network_name"),
        "--publish",
        "9000:9000",
        "--volume",
        f"{supportconfig_path}:{supportconfig_path}",
        "--volume",
        f'{config.get_sources_dir("exporters")}:/opt',
        "--volume",
        f'{config.get_config_file_path("exporter")}:/opt/config.yml',
        "--name",
        exporter_name,
        image,
    ]

    podman(
        podman_args,
        verbose,
    )


def create_supportconfig_exporter_cfg(supportconfig_path: str):
    exporter_template = config.load_jinja_template("exporter/exporter.yaml.j2")
    opts = {"supportconfig_path": supportconfig_path}
    config.write_config("exporter", "config.yaml", exporter_template.render(**opts))
