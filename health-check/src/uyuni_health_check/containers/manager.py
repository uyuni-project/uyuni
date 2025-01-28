from typing import List
from uyuni_health_check import config
from uyuni_health_check.utils import run_command, HealthException, console


def podman(cmd: List[str], verbose=False, raise_exc=True) -> List:
    """
    Run a podman command

    :param cmd: the command in an array format without the initial "podman" part
    """
    return run_command(["podman"] + cmd, verbose, raise_exc)


def build_image(name: str, containerfile_path: str, build_args: List[str] | None = None, verbose: bool = False) -> None:
    """
    Build a container image
    """
    podman_args = ["build", "-t", f"{name}"]
    if build_args:
        [podman_args.append(f'--build-arg="{param}"') for param in build_args]
    podman_args.append(containerfile_path)

    podman(podman_args, verbose)


def image_exists(image):
    """
    Check if the image is present in podman images result
    """
    stdout, _, _ =  podman(["images", "--quiet", "-f", f"reference={image}"], verbose=False, raise_exc=False)
    return stdout.strip() != ""


def network_exists(network):
    """
    Check if the podman network is up and running
    """
    _, _, returncode = podman(["network", "exists", f"{network}"], verbose=False, raise_exc=False)
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
        console.log("[bold]Removing application containers")
        network = config.load_prop("podman.network_name")

        if network_exists(network):
            podman(
                [
                    "network",
                    "rm",
                    "-f",
                    network,
                ],
                verbose,
            )
            console.log("[green]Containers have been removed")

        console.log("[bold]Removing all container images")
        for image in config.get_all_container_image_names():
            if image_exists(image):
                podman(
                    [
                        "rmi",
                        image,
                    ],
                    verbose,
                )
                console.log(f"[green]Image {image} has been removed")


def create_podman_network(verbose=False):
    """
    Create uyuni-health-check pod where we run the containers

    :param server: the Uyuni server to create the pod on or localhost
    """
    console.log("[bold]Creating podman network")

    network = config.load_prop("podman.network_name")
    if network_exists(network):
        console.log(f"[yellow]Skipped; {network} already exists")
    else:
        podman(
            [
                "network",
                "create",
                network,
            ],
            verbose,
        )


def container_is_running(name):
    """
    Check if a container with a given name is running in podman
    """
    stdout, _, _ = podman(["ps", "--quiet", "-f", f"name={name}"])
    return stdout != ""
