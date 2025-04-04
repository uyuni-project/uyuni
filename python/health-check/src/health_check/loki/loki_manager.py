"""Module that controls the Loki and Promtail containers"""

import os
from health_check import config
from health_check.utils import console
from health_check.containers.manager import (
    container_is_running,
    podman,
)

# Update this number if adding more targets to the promtail config
PROMTAIL_TARGETS = 6

# Max number of seconds to wait for Loki to be ready
LOKI_WAIT_TIMEOUT = 120


def run_loki(supportconfig_path=None, verbose=False):
    """
    Run promtail and loki to aggregate the logs
    """
    loki_name = config.load_prop("loki.container_name")
    network = config.load_prop("podman.network_name")

    console.log("[bold]Deploying Promtail and Loki containers")

    if container_is_running(loki_name):
        console.log("[yellow]Skipped, Loki container already exists")
        return

    promtail_template = config.load_jinja_template("promtail/promtail.yaml.j2")
    render_promtail_cfg(supportconfig_path, promtail_template)
    podman(
        [
            "run",
            "--replace",
            "--detach",
            "--network",
            network,
            "--publish",
            "3100:3100",
            "--name",
            loki_name,
            "--volume",
            f'{config.get_config_file_path("loki")}:/etc/loki/local-config.yaml',
            config.load_prop("loki.image"),
        ],
        verbose,
    )

    # Run promtail only now since it pushes data to loki
    promtail_image = config.load_prop("promtail.image")

    podman_args = [
        "run",
        "--replace",
        "--network",
        network,
        "--publish",
        "9081:9081",
        "--detach",
        "--volume",
        f'{config.get_config_file_path("promtail")}:/etc/promtail/config.yml',
        "--volume",
        f"{supportconfig_path}:{supportconfig_path}",
        "--name",
        config.load_prop("promtail.container_name"),
        promtail_image,
    ]

    podman(podman_args, verbose)


def render_promtail_cfg(supportconfig_path=None, promtail_template=None):
    """
    Render promtail configuration file

    :param supportconfig_path: render promtail configuration based on this path to a supportconfig
    """

    if supportconfig_path:
        opts = {
            "rhn_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/rhn-logs/rhn/"
            ),
            "cobbler_logs_file": os.path.join(
                supportconfig_path, "spacewalk-debug/cobbler-logs/cobbler.log"
            ),
            "salt_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/salt-logs/salt/"
            ),
            "postgresql_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/database/"
            ),
            "apache2_logs_path": os.path.join(
                supportconfig_path, "spacewalk-debug/httpd-logs/apache2/"
            ),
        }
    else:
        opts = {
            "rhn_logs_path": "/var/log/rhn/",
            "cobbler_logs_file": "/var/log/cobbler.log",
            "salt_logs_path": "/var/log/salt/",
            "apache2_logs_path": "/var/log/apache2/",
            "postgresql_logs_path": "/var/lib/pgsql/data/log/",
        }

    # Write rendered promtail configuration file
    config.write_config("promtail", "config.yaml", promtail_template.render(**opts))
