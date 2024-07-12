from json import JSONDecodeError
import json
import click
from loki.loki_manager import HealthException
from rich.console import Console
from rich.markdown import Markdown
from rich.table import Table
import containers.manager
from utils import HealthException, run_command
from loki.loki_manager import *
from loki.logs_gatherer import *
from config_loader import ConfigLoader
from exporters.supportconfig import exporter

console = Console()

@click.group()
@click.option(
    "-i",
    "--supportconfig_path",
    default=None,
    help="Use a supportconfig path as the data source",
)
@click.option(
    "-v",
    "--verbose",
    is_flag=True,
    help="Show more stdout, including image building",
)
@click.pass_context
def cli(ctx, supportconfig_path, verbose):
    # ensure that ctx.obj exists and is a dict (in case `cli()` is called
    # by means other than the `if` block below)
    ctx.ensure_object(dict)
    ctx.obj["verbose"] = verbose
    ctx.obj["supportconfig_path"] = supportconfig_path
    ctx.obj["config"] = ConfigLoader()

    try:
        console.log("[bold]Checking connection with podman:")
        run_command(cmd=["podman", "--version"], console=console, quiet=False)
    except HealthException as err:
        console.log("[red bold]" + str(err))
        console.print(Markdown("# Execution Finished"))
        exit(1)

@cli.command()
@click.option(
    "-ep",
    "--exporter-port",
    type=int,
    default=9000,
    help="uyuni health exporter metrics port",
)
@click.option(
    "--loki",
    default=None,
    help="URL of an existing loki instance to use to fetch the logs",
)
@click.option(
    "--logs",
    is_flag=True,
    help="Show the error logs",
)
@click.option(
    "--since",
    default=7,
    type=int,
    help="Show logs from last X days. (Default: 7)",
)
@click.option(
    "-c",
    "--clean",
    is_flag=True,
    help="Remove containers after execution",
)
@click.pass_context
def run(ctx, exporter_port, loki, logs, since, clean):
    """
    Start execution of Uyuni Health Check

    Build the necessary containers, deploy them, get the metrics and display them

    :param exporter_port: uyuni health exporter metrics port
    :param loki: URL to a loki instance. Setting it will skip the promtail and loki deployments
    """
    config = ctx.obj["config"]
    verbose = ctx.obj["verbose"]
    supportconfig_path = ctx.obj["supportconfig_path"]
    console.print(Markdown("# Execution Finished"))
    try:
        with console.status(status=None):
            console.log("[bold]Building logcli image")
            download_component_build_image("logcli", config=config, verbose=verbose)

            console.log("[bold]Deploying promtail and Loki")
            run_loki(supportconfig_path=supportconfig_path, config=config, verbose=verbose)
            wait_loki_init()

            exporter.prepare_exporter(config=config, supportconfig_path=supportconfig_path)

            console.print(Markdown("## Relevant Errors"))
            loki_url = f"http://loki:3100"
            show_full_error_logs(loki_url, 30, console)

    except HealthException as err:
        console.log("[red bold]" + str(err))
    finally:
        if clean:
            clean_server()
    


def main():
    print(Markdown("# Uyuni Health Check"))
    cli()


if __name__ == "__main__":
    main()
