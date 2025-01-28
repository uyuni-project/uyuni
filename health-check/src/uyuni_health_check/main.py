import click
from rich.markdown import Markdown

from uyuni_health_check.grafana.grafana_manager import prepare_grafana
from uyuni_health_check.utils import console, HealthException, run_command
from uyuni_health_check.loki.loki_manager import (
    run_loki,
    wait_promtail_init,
)
from uyuni_health_check.exporters import exporter
import uyuni_health_check.containers.manager
import uyuni_health_check.metrics


@click.group()
@click.option(
    "-s",
    "--supportconfig_path",
    default=None,
    help="Path to supportconfig path as the data source",
)
@click.option(
    "-v",
    "--verbose",
    is_flag=True,
    help="Show more stdout, including image building",
)
@click.pass_context
def cli(ctx, supportconfig_path, verbose):
    ctx.ensure_object(dict)
    ctx.obj["verbose"] = verbose
    ctx.obj["supportconfig_path"] = supportconfig_path

    try:
        console.log("[bold]Checking connection with podman:")
        run_command(cmd=["podman", "--version"], verbose=True)
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
)
@click.option(
    "--to_datetime",
    help="Exclude logs after this date (in ISO 8601 format)",
)
@click.pass_context
def run(ctx: click.Context, from_datetime: str, to_datetime: str, since: int):
    """
    Start execution of Uyuni Health Check

    Build the necessary containers, deploy them, get the metrics and display them

    """
    verbose: bool = ctx.obj["verbose"]
    supportconfig_path: str | None = ctx.obj["supportconfig_path"]

    if not supportconfig_path:
        console.log("[red bold]Provide a supportconfig path")
        exit(1)

    try:
        with console.status(status=None):
            uyuni_health_check.containers.manager.create_podman_network(verbose=verbose)

            run_loki(supportconfig_path, verbose)
            wait_promtail_init()
            #wait_loki_init()

            exporter.prepare_exporter(supportconfig_path, verbose)
            prepare_grafana(from_datetime, to_datetime, verbose)

        console.print(Markdown("# Execution Finished"))

    except HealthException as err:
        console.log("[red bold]" + str(err))
        if verbose:
            raise err


@cli.command()
@click.pass_context
def clean(ctx: click.Context):
    verbose = ctx.obj["verbose"]
    uyuni_health_check.containers.manager.clean_containers(verbose=verbose)
    console.print(Markdown("# Execution Finished"))


def main():
    console.print(Markdown("# Uyuni Health Check"))
    cli()


if __name__ == "__main__":
    main()
