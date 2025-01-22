from uyuni_health_check import config
from uyuni_health_check.utils import console
from uyuni_health_check.containers.manager import (
    image_exists,
    build_image,
    podman,
    container_is_running,
)


def prepare_exporter(verbose=False, supportconfig_path=None):
    """
    Build the prometheus exporter image and deploy it on the server

    :param server: the Uyuni server to deploy the exporter on
    """
    exporter_name = "supportconfig-exporter"
    exporter_dir = config.load_dockerfile_dir("exporter")
    create_supportconfig_exporter_cfg(
        supportconfig_path=supportconfig_path
    )
    exporter_config = config.get_config_file_path("exporter")
    exporter_sources = config.get_sources_dir("exporters")

    console.log(f"[bold]Building {exporter_name} image")
    if image_exists(f"{exporter_name}"):
        console.log(f"[yellow]Skipped as the {exporter_name} image is already present")
    else:
        build_image(f"{exporter_name}", exporter_dir, verbose=verbose)
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
        "--replace",
        "-d",
        "--network=health-check-network",
        "-p",
        "9000:9000",
        "-v",
        f"{supportconfig_path}:{supportconfig_path}",
        "-v",
        f"{exporter_sources}:/opt",
        "-v",
        f"{exporter_config}:/opt/config.yml",
    ]

    podman_args.extend(
        [
            "--name",
            f"uyuni_health_check_{exporter_name}",
            f"{exporter_name}",
        ]
    )
    console.log(f"Running this command: podman " + ' '.join(podman_args))
    # Run the container
    podman(
        podman_args,
        quiet=not verbose,
    )


def create_supportconfig_exporter_cfg(supportconfig_path=None):
    exporter_template = config.load_jinja_template("exporter/exporter.yaml.j2")
    opts = {"supportconfig_path": supportconfig_path}
    config.write_config("exporter", "config.yaml", exporter_template.render(**opts))
