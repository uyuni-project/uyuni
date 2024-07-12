import os
from jinja2 import Environment, FileSystemLoader
from rich.console import Console
from containers.manager import *

console = Console()

def prepare_exporter(config=None, verbose=False, supportconfig_path=None):
    """
    Build the prometheus exporter image and deploy it on the server

    :param server: the Uyuni server to deploy the exporter on
    """
    if supportconfig_path:
        exporter_name = "supportconfig-exporter"
        exporter_dir = config.load_dockerfile_dir("exporter")
        create_supportconfig_exporter_cfg(config=config, supportconfig_path=supportconfig_path)

    console.log(f"[bold]Building {exporter_name} image")
    if image_exists(f"{exporter_name}"):
        console.log(f"[yellow]Skipped as the {exporter_name} image is already present")
    else:
        sources_dir = config.get_sources_path()
        build_image(f"{exporter_name}", exporter_dir, build_args=[f"sources_dir={sources_dir}"],verbose=verbose)
        console.log(f"[green]The {exporter_name} image was built successfully")

    # Run the container
    console.log(f"[bold]Deploying {exporter_name} container")
    if container_is_running(f"{exporter_name}"):
        console.log(
            f"[yellow]Skipped as the {exporter_name} container is already running"
        )
        return
    
    # Prepare arguments for Podman call
    podman_args = [
        "run",
        "-d",
        "--network=health-check-network",
        "-v",
        f"{supportconfig_path}:{supportconfig_path}",
    ]

    podman_args.extend(
        [
            "--name",
            f"{exporter_name}",
            f"{exporter_name}",
        ]
    )

    # Run the container
    podman(
        podman_args,
        console=console,
    )


def create_supportconfig_exporter_cfg(config=None, supportconfig_path=None):
    console.log("EXPORTER TEMPLATE:")
    exporter_template = config.load_jinja_template("exporter/exporter.yaml.j2")
    opts = {"supportconfig_path": supportconfig_path}
    exporter_config_file_path = config.get_config_file_path("exporter")
    config.write_config("exporter", exporter_template.render(**opts))
