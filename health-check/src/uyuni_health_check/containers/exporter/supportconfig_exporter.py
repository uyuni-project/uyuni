# SPDX-FileCopyrightText: 2023 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import os
import re
import signal
import sys
import time

import yaml
from prometheus_client import start_http_server
from prometheus_client.core import REGISTRY, GaugeMetricFamily


def sigterm_handler(**kwargs):
    print("Detected SIGTERM. Exiting.")
    sys.exit(0)


signal.signal(signal.SIGTERM, sigterm_handler)


class SupportConfigMetricsCollector(object):
    def __init__(self, supportconfig_path=None):
        if not supportconfig_path:
            raise ValueError("A 'supportconfig_path' must be set via config.yml file")

        self.supportconfig_path = supportconfig_path
        self.refresh()

    def refresh(self):
        self.salt_jobs = self.read_salt_jobs()
        self.salt_keys = self.read_salt_keys()
        self.salt_configuration = self.read_salt_configuration()

    def _parse_command(self, command_block):
        lines = command_block.strip().split("\n")
        command = lines[0][2:]
        return command, lines[1:]

    def parse_supportconfig_file(self, filein):
        content = None
        with open(os.path.join(self.supportconfig_path, filein)) as f:
            content = f.read()
        parsed = re.findall(r"^#==\[ (.*) \]=+#$((?:\n.+)+)$", content, re.MULTILINE)
        ret = {}
        for _, value in parsed:
            cmd, val = self._parse_command(value)
            ret[cmd] = val
        return ret

    def parse_supportconfig_plugin_file(self, filein):
        content = None
        with open(os.path.join(self.supportconfig_path, filein)) as f:
            content = f.read()
        parsed = re.findall(r"^#==\[ (.*) \]=+#$((?:\n.+)+)$", content, re.MULTILINE)
        ret = {}
        for _, value in parsed:
            cmd, val = self._parse_command(value)
            ret[cmd] = val
        return ret

    def read_salt_configuration(self):
        content = None
        with open(
            os.path.join(self.supportconfig_path, "plugin-saltconfiguration.txt")
        ) as f:
            content = f.read()
        attrs_to_expose = [
            "worker_threads",
            "sock_pool_size",
            "timeout",
            "gather_job_timeout",
        ]
        ret = {}
        for attr in attrs_to_expose:
            ret[attr] = re.findall(f"^{attr}: ([0-9]+)$", content, re.MULTILINE)[-1]
        return ret

    def read_salt_keys(self):
        content = None
        with open(
            os.path.join(self.supportconfig_path, "plugin-saltminionskeys.txt")
        ) as f:
            content = f.read()
        ret = {}
        parsed = re.findall(
            r"^Accepted Keys:$((?:\n.*)*)\nDenied Keys:$((?:\n.*)*)\n"
            r"Unaccepted Keys:$((?:\n.*)*)\nRejected Keys:$((?:\n.*)*)#==",
            content,
            re.MULTILINE,
        )[0]
        ret["accepted"] = parsed[0].strip().split("\n") if parsed[0].strip() else []
        ret["denied"] = parsed[1].strip().split("\n") if parsed[1].strip() else []
        ret["unaccepted"] = parsed[2].strip().split("\n") if parsed[2].strip() else []
        ret["rejected"] = parsed[3].strip().split("\n") if parsed[3].strip() else []
        return ret

    def read_salt_jobs(self):
        content = None
        with open(os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")) as f:
            content = f.read()
        return re.findall(
            "^'([0-9]+)':$(?:\n.*\n.*Function: (.*)\n[^'|#==]*)", content, re.MULTILINE
        )

    def collect(self):
        gauge = GaugeMetricFamily(
            "salt_master_config", "Salt Master Configuration", labels=["name"]
        )
        gauge.add_metric(
            ["worker_threads"],
            self.salt_configuration["worker_threads"],
        )
        gauge.add_metric(
            ["sock_pool_size"],
            self.salt_configuration["sock_pool_size"],
        )
        gauge.add_metric(["timeout"], self.salt_configuration["timeout"])
        gauge.add_metric(
            ["gather_job_timeout"],
            self.salt_configuration["gather_job_timeout"],
        )
        yield gauge

        gauge2 = GaugeMetricFamily(
            "salt_keys",
            "Information about Salt keys",
            labels=["name"],
        )
        gauge2.add_metric(["accepted"], len(self.salt_keys["accepted"]))
        gauge2.add_metric(["denied"], len(self.salt_keys["denied"]))
        gauge2.add_metric(["unaccepted"], len(self.salt_keys["unaccepted"]))
        gauge2.add_metric(["rejected"], len(self.salt_keys["rejected"]))
        yield gauge2

        gauge3 = GaugeMetricFamily(
            "salt_jobs",
            "Information about Salt Jobs",
            labels=["jid", "fun"],
        )
        for jid, fun in self.salt_jobs:
            gauge3.add_metric([jid, fun], 1)
        yield gauge3


def main():
    print("Supportconfig Exporter started")
    port = 9000
    frequency = 60
    supportconfig_path = None
    if os.path.exists("config.yml"):
        with open("config.yml", "r") as config_file:
            try:
                config = yaml.safe_load(config_file)
                port = int(config["port"])
                frequency = config["scrape_frequency"]
                supportconfig_path = config["supportconfig_path"]
            except yaml.YAMLError as error:
                print(error)

    start_http_server(port)
    collector = SupportConfigMetricsCollector(supportconfig_path)
    REGISTRY.register(collector)
    print("Supporconfig Exporter is ready")
    while True:
        # period between collection
        time.sleep(frequency)
        collector.refresh()


if __name__ == "__main__":
    main()
