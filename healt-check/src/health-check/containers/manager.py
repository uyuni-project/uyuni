import os
from jinja2 import Environment, FileSystemLoader
from rich.console import Console
import zipfile
from utils import run_command, HealthException
import requests

console = Console()

def podman(cmd, server=None, console=None):
    """
    Run a podman command

    :param cmd: the command in an array format without the initial "podman" part
    """
    console = Console()
    try:
        console.log(f"I'm about to run {['podman']+ cmd}")
        return run_command(["podman"] + cmd, console, quiet=not console)
    except OSError:
        raise HealthException(
            "podman is required {}".format("on " + server if server else "")
        )

def build_image(name, image_path=None, build_args = [], verbose=False):
    """
    Build a container image
    """
    build_options = [
        "-t",
        f"{name}"
    ]

    if build_args:
         [build_options.append(f"--build-arg={param}") for param in build_args]
    
    podman_args = ["build"]
    podman_args.extend(build_options)
    podman_args.append(image_path)
    console.log("podman_args:", podman_args)
    process = podman(
        podman_args,\
        console=console if verbose else None,
    )

    if process.returncode != 0:
        raise HealthException(f"Failed to build {name} image")

def image_exists(image, server=None):
    """
    Check if the image is present in podman images result
    """
    return (
        podman(["images", "--quiet", "-f", f"reference={image}"], server=server)
        .stdout.read()
        .strip()
        != ""
    )

def clean_server(server):
    """
    Remove the containers we spawned on the server now that everything is finished

    :param server: server to clean
    """
    with console.status(status=None):
        console.log("[bold]Cleaning up containers after execution")
        if not pod_exists("uyuni-health-check", server=server):
            console.log("[yellow]Skipped as the uyuni-health-check pod is not running")
        else:
            podman(
                [
                    "pod",
                    "rm",
                    "-f",
                    "uyuni-health-check",
                ],
                server,
                console=console,
            )
            console.log("[green]Containers have been removed")

        console.log("[bold]Removing promtail and exporter images")
        images_to_remove = [
            "localhost/promtail",
            "localhost/supportconfig-exporter",
            "localhost/uyuni-health-exporter",
        ]
        for image in images_to_remove:
            if image_exists(image, server=server):
                podman(
                    [
                        "rmi",
                        image,
                    ],
                    server,
                    console=console,
                )
                console.log(f"[green]{image} image has been removed")

def prepare_exporter(server, verbose=False, supportconfig_path=None):
    """
    Build the prometheus exporter image and deploy it on the server

    :param server: the Uyuni server to deploy the exporter on
    """
    if supportconfig_path:
        exporter_name = "supportconfig-exporter"
        exporter_dir = "supportconfig_exporter"
        render_supportconfig_exporter_cfg(supportconfig_path)
    else:
        exporter_name = "uyuni-health-exporter"
        exporter_dir = "exporter"

    console.log(f"[bold]Building {exporter_name} image")
    if image_exists(f"{exporter_name}"):
        console.log(f"[yellow]Skipped as the {exporter_name} image is already present")
    else:
        build_image(f"{exporter_name}", exporter_dir, verbose=verbose)
        console.log(f"[green]The {exporter_name} image was built successfully")

    # Run the container
    console.log(f"[bold]Deploying {exporter_name} container")
    if container_is_running(f"{exporter_name}", server=server):
        console.log(
            f"[yellow]Skipped as the {exporter_name} container is already running"
        )
        return
    
def container_is_running(name, server=None):
    """
    Check if a container with a given name is running in podman
    """
    process = podman(["ps", "--quiet", "-f", f"name={name}"], server=server)
    return process.stdout.read() != ""

