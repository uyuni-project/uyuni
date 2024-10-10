import os
from jinja2 import Environment, FileSystemLoader
import zipfile
from uyuni_health_check.utils import run_command, HealthException, console

# from utils import run_command, HealthException, console
import requests


def podman(cmd, quiet=True, use_print=False):
    """
    Run a podman command

    :param cmd: the command in an array format without the initial "podman" part
    """
    try:
        if not quiet:
            console.log(f"[italic]Running command {'podman ' + ' '.join(cmd)}[/italic]")
        return run_command(["podman"] + cmd, console, quiet=quiet, use_print=use_print)
    except OSError:
        raise HealthException("podman is required")


def build_image(name, image_path=None, build_args=[], verbose=False):
    """
    Build a container image
    """
    build_options = ["-t", f"{name}"]

    if build_args:
        [build_options.append(f'--build-arg="{param}"') for param in build_args]

    podman_args = ["build"]
    podman_args.extend(build_options)
    podman_args.append(image_path)
    #    console.log("podman_args:", podman_args)
    process = podman(
        podman_args,
        quiet=not verbose,
        #        podman_args,
    )

    if process.returncode != 0:
        raise HealthException(f"Failed to build {name} image")


def image_exists(image):
    """
    Check if the image is present in podman images result
    """
    stdout, stderr, _ =  podman(["images", "--quiet", "-f", f"reference={image}"], quiet=True)
    return stdout.strip() != ""


def network_exists(network):
    """
    Check if the podman network is up and running
    """
    stdout, stderr, returncode = podman(["network", "exists", f"{network}"], quiet=True)
    return returncode == 0


def clean_containers(verbose=False):
    """
    Remove the containers we spawned on the server now that everything is finished

    :param server: server to clean
    """
    # TODO
    # Clean containers
    # Clean network

    with console.status(status=None):
        console.log("[bold]Cleaning up containers after execution")
        if not network_exists("health-check-network"):
            console.log("[yellow]Skipped as the health-check-network is not running")
        else:
            podman(
                [
                    "network",
                    "rm",
                    "-f",
                    "health-check-network",
                ],
                quiet=not verbose,
            )
            console.log("[green]Containers have been removed")

        console.log("[bold]Removing promtail and exporter images")
        images_to_remove = [
            "localhost/promtail",
            "localhost/logcli",
            "localhost/supportconfig-exporter",
            "localhost/uyuni-health-exporter",
        ]
        for image in images_to_remove:
            if image_exists(image):
                podman(
                    [
                        "rmi",
                        image,
                    ],
                    quiet=not verbose,
                )
                console.log(f"[green]{image} image has been removed")


def create_podman_network(verbose=False):
    """
    Create uyuni-health-check pod where we run the containers

    :param server: the Uyuni server to create the pod on or localhost
    """
    if network_exists("health-check-network"):
        console.log("[yellow]Skipped as the health-check-network is already running")
    else:
        podman(
            [
                "network",
                "create",
                "health-check-network",
            ],
            quiet=not verbose,
        )


def container_is_running(name):
    """
    Check if a container with a given name is running in podman
    """
    stdout, stderr, _ = podman(["ps", "--quiet", "-f", f"name={name}"])
    return stdout != ""
