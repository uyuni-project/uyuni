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
        run_command(cmd=["podman", "--version"], console=console, quiet=False)
    except HealthException as err:
        console.log("[red bold]" + str(err))
        console.print(Markdown("# Execution Finished"))
        exit(1)


@cli.command()
@click.option(
    "--since",
    default=7,
    type=int,
    help="Show logs from last X days. (Default: 7)",
)
@click.option(
    "--from_datetime",
    help="Start looking for logs at this absolute time ",
)
@click.option(
    "--to_datetime",
    help="Stop looking for logs at this absolute time",
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
        console.log("[red bold]You must provide a path to a supportconfig!")
        exit(1)

    try:
        with console.status(status=None):

            console.log(
                "[bold]Creating health-check-network podman network for containers"
            )
            uyuni_health_check.containers.manager.create_podman_network(verbose=verbose)

            console.log("[bold]Deploying promtail and Loki")
            run_loki(
                supportconfig_path=supportconfig_path, verbose=verbose
            )
            wait_promtail_init()
            #wait_loki_init()

            console.log("[bold]Building exporter")
            exporter.prepare_exporter(
                supportconfig_path=supportconfig_path
            )

            console.log("[bold]Preparing Grafana")
            prepare_grafana(from_datetime, to_datetime, verbose=verbose)

        console.print(Markdown("# Execution Finished"))

    except HealthException as err:
        console.log("[red bold]" + str(err))


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
