import click
from rich.markdown import Markdown

from grafana.grafana_manager import prepare_grafana
from prometheus.prometheus_manager import prepare_prometheus

from uyuni_health_check.utils import console, HealthException, run_command
from uyuni_health_check.loki.loki_manager import (
    run_loki,
    wait_loki_init,
    download_component_build_image,
)
from uyuni_health_check.loki.logs_gatherer import show_full_error_logs, show_error_logs_stats
from uyuni_health_check.config_loader import ConfigLoader
from uyuni_health_check.exporters.supportconfig import exporter
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
    "--from_datetime",
    help="Start looking for logs at this absolute time ",
)
@click.option(
    "--to_datetime",
    help="Stop looking for logs at this absolute time",
)
@click.pass_context
def run(ctx, logs, from_datetime, to_datetime, since):
    """
    Start execution of Uyuni Health Check

    Build the necessary containers, deploy them, get the metrics and display them

    """
    config = ctx.obj["config"]
    verbose = ctx.obj["verbose"]
    supportconfig_path = ctx.obj["supportconfig_path"]

    if not supportconfig_path:
        console.log("[red bold]You must provide a path to a supportconfig!")
        console.print(Markdown("# Execution Finished"))
        exit(1)

    try:
        with console.status(status=None):

            console.log(
                "[bold]Creating health-check-network podman network for containers"
            )
            uyuni_health_check.containers.manager.create_podman_network(verbose=verbose)

            console.log("[bold]Building logcli image")
            download_component_build_image("logcli", config=config, verbose=verbose)

            console.log("[bold]Deploying promtail and Loki")
            run_loki(
                supportconfig_path=supportconfig_path, config=config, verbose=verbose
            )
            wait_loki_init()

            console.log("[bold]Building exporter")
            exporter.prepare_exporter(
                config=config, supportconfig_path=supportconfig_path
            )

            # Fetch metrics from supportconfig-exporter
            console.log("[bold]Fetching metrics from supportconfig-exporter")
            metrics = (
                uyuni_health_check.metrics.fetch_metrics_from_supportconfig_exporter(
                    console
                )
            )

            console.log("[bold]Preparing Prometheus")
            prepare_prometheus(verbose=verbose)

            console.log("[bold]Preparing Grafana")
            prepare_grafana(verbose=verbose)

        console.print(Markdown("# Summary of metrics gatherered from Supportconfig"))
        #uyuni_health_check.metrics.show_supportconfig_metrics(metrics, console)

        console.print(Markdown("## Relevant Errors"))
        show_error_logs_stats(from_datetime, to_datetime, since, console)

        if logs:
            with console.pager():
                show_full_error_logs(from_datetime, to_datetime, since)

        console.print(Markdown("# Execution Finished"))

    except HealthException as err:
        console.log("[red bold]" + str(err))


@cli.command()
@click.pass_context
def clean(ctx):
    verbose = ctx.obj["verbose"]
    uyuni_health_check.containers.manager.clean_containers(verbose=verbose)
    console.print(Markdown("# Execution Finished"))


def main():
    print(Markdown("# Uyuni Health Check"))
    cli()


if __name__ == "__main__":
    main()
