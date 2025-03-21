"""Main module for the health check tool"""

import click
import os
from rich.markdown import Markdown
from rich.panel import Panel
from rich.text import Text

from health_check.grafana.grafana_manager import prepare_grafana
import health_check.utils as utils
from health_check.utils import console, HealthException
from health_check.loki.loki_manager import run_loki
from health_check.exporters import exporter
from health_check.containers.manager import (
    create_podman_network,
    clean_containers,
)


@click.group()
@click.option(
    "-s",
    "--supportconfig_path",
    help="Path to supportconfig path as the data source",
)
@click.option(
    "-v",
    "--verbose",
    is_flag=True,
    help="Show more stdout, including image building",
)
@click.pass_context
def cli(ctx: click.Context, supportconfig_path: str, verbose: bool):
    ctx.ensure_object(dict)
    ctx.obj["verbose"] = verbose
    ctx.obj["supportconfig_path"] = supportconfig_path

    try:
        console.log("[bold]Checking connection with podman:")
        utils.run_command(cmd=["podman", "--version"], verbose=True)
    except HealthException as err:
        console.log("[red bold]" + str(err))
        console.print(Markdown("# Execution Finished"))
        exit(1)


@cli.command()
@click.option(
    "--since",
    default=7,
    type=int,
    help="Show logs from last X days (default: 7)",
)
@click.option(
    "--from_datetime",
    help="Look for logs from this date (in ISO 8601 format)",
    callback=utils.validate_date,
)
@click.option(
    "--to_datetime",
    help="Exclude logs after this date (in ISO 8601 format)",
    callback=utils.validate_date,
)
@click.pass_context
def start(ctx: click.Context, from_datetime: str, to_datetime: str, since: int):
    """
    Start execution of Health Check

    Build the necessary containers, deploy them, get the metrics and display them

    """
    verbose: bool = ctx.obj["verbose"]
    supportconfig_path: str | None = ctx.obj["supportconfig_path"]

    # Try to resolve an absolute path to the supportconfig
    if supportconfig_path:
        supportconfig_path = os.path.abspath(os.path.expanduser(supportconfig_path))

    if not supportconfig_path or not os.path.exists(supportconfig_path):
        console.log("[red bold]Supportconfig path not accessible, exitting")
        exit(1)

    if not os.path.exists(os.path.join(supportconfig_path, "basic-environment.txt")):
        console.log(
            "[red bold]A valid supportconfig cannot be found in the provided path, exitting"
        )
        exit(1)

    period_start, period_end = utils.get_dates(since)

    if not from_datetime:
        from_datetime = period_start
    if not to_datetime:
        to_datetime = period_end

    try:
        with console.status(status=None):
            create_podman_network(verbose=verbose)
            run_loki(supportconfig_path, verbose)
            exporter.prepare_exporter(supportconfig_path, verbose)
            prepare_grafana(from_datetime, to_datetime, verbose)

        console.print(
            Panel(
                Text(
                    "You can visit now the Grafana dashboard to see metrics and relevant errors at http://localhost:3000/d/AvmqWWUik/",
                    justify="center",
                )
            ),
            style="italic green",
        )
        console.print(Markdown("# Execution Finished"))

    except HealthException as err:
        console.log("[red bold]" + str(err))
        if verbose:
            raise err


@cli.command()
@click.pass_context
def stop(ctx: click.Context):
    """
    Stop execution of Health Check and clean containers

    """
    verbose = ctx.obj["verbose"]
    clean_containers(verbose=verbose)
    console.print(Markdown("# Execution Finished"))


def main():
    console.print(Markdown("# Health Check"))
    cli()  # pylint: disable=no-value-for-parameter


if __name__ == "__main__":
    main()
