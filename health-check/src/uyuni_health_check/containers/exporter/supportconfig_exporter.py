# SPDX-FileCopyrightText: 2023 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import os
import re
import signal
import sys
import time
from datetime import datetime

import yaml
from prometheus_client import start_http_server
from prometheus_client.core import REGISTRY, GaugeMetricFamily
from prometheus_client.registry import Collector
from prometheus_client.samples import Timestamp
import xml.etree.ElementTree as ET


def sigterm_handler(**kwargs):
    print("Detected SIGTERM. Exiting.")
    sys.exit(0)


signal.signal(signal.SIGTERM, sigterm_handler)


class SupportConfigMetricsCollector(Collector):
    def __init__(self, supportconfig_path=None):
        if not supportconfig_path:
            raise ValueError("A 'supportconfig_path' must be set via config.yml file")

        self.supportconfig_path = supportconfig_path
        self.refresh()

    def refresh(self):
        if self.exists_salt_jobs_file():
            self.salt_jobs = self.read_salt_jobs()
        if self.exists_salt_keys_file():
            self.salt_keys = self.read_salt_keys()
        if self.exists_salt_configuration_file():
            self.salt_configuration = self.read_salt_configuration()
        if self.exists_tomcat_java_opts():
            self.xmx_size = self.get_tomcat_xmx_size()

        self.max_threads_ipv4 = self.get_tomcat_max_threads_ipv4()

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

    def exists_salt_configuration_file(self):
        if os.path.isfile(os.path.join(self.supportconfig_path, "plugin-saltconfiguration.txt")):
            return True
        
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
    
    def exists_salt_keys_file(self):
        if os.path.isfile(os.path.join(self.supportconfig_path, "plugin-saltminionskeys.txt")):
            return True

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
    
    def exists_salt_jobs_file(self):
        if os.path.isfile(os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")):
            return True

    def read_salt_jobs(self):
        content = None
        with open(os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")) as f:
            content = f.read()
        return re.findall(
            "^'([0-9]+)':[\s\S]*?Function:\s+([\w.]+)[\s\S]*?StartTime:\s+(\d{4},\s\w{3}\s\d{2}\s\d{2}:\d{2}:\d{2}\.\d{6})", content, re.MULTILINE
        )

    def get_connector_attribute(self, file_path, attribute_name):

        with open(os.path.join(self.supportconfig_path, "spacewalk-debug/conf/tomcat/tomcat/server.xml")) as f:
            tree = ET.parse(file_path)
            root = tree.getroot()

            max_threads_values = []
            for connector in root.findall(".//Connector"):
                if 'maxThreads' in connector.attrib:
                    max_threads_values.append(connector.attrib['maxThreads'])

            return max_threads_values

    def exists_tomcat_java_opts(self):
        if os.path.isfile(os.path.join(self.supportconfig_path, "spacewalk-debug/conf/tomcat/tomcat/conf.d/tomcat_java_opts.conf")):
            return True

    def get_tomcat_xmx_size(self):
        with open(os.path.join(self.supportconfig_path, "spacewalk-debug/conf/tomcat/tomcat/conf.d/tomcat_java_opts.conf")) as f:
            first_line = f.readline().strip()
            xmx_match = re.search(r'-Xmx(\d)+([kKmMgG])', first_line)
            xmx_value = xmx_match.group(1) if xmx_match else None
            xmx_unit = xmx_match.group(2) if xmx_match else None
            if xmx_value is None:
                return 0
            if xmx_unit == 'm' or xmx_unit == 'M':
                return int(xmx_value)
            if xmx_unit == 'k' or xmx_unit == 'K':
                return int(xmx_value) * 1024
            if xmx_unit == 'g' or xmx_unit == 'G':
                return int(xmx_value) * 1024 * 1024

    def get_tomcat_max_threads_ipv4(self):
        with open(os.path.join(self.supportconfig_path, "spacewalk-debug/conf/tomcat/tomcat/server.xml")) as f:
            content = f.read()
            max_threads_match = re.search(r'<Connector[^>]*address="127\.0\.0\.1"[^>]*maxThreads="(\d+)"[^>]*\/>', content)
            return int(max_threads_match.group(1))

    def get_tomcat_max_threads_ipv6(self):
        with open(os.path.join(self.supportconfig_path), "spacewalk-debug/conf/tomcat/tomcat/server.xml") as f:
            content = f.read()
            max_threads_match = re.search(r'<Connector[^>]*address="::1"[^>]*maxThreads="(\d+)"[^>]*\/>', content)
            return max_threads_match.group(1)

    def collect(self):

        gauge_statics = GaugeMetricFamily("statics", "Static values", labels=["statics"])
        if hasattr(self,'xmx_size'):
            gauge_statics.add_metric(["xmx_size"], self.xmx_size)
        if hasattr(self, 'max_threads_ipv4'):
            gauge_statics.add_metric(["max_threads_ipv4"], self.max_threads_ipv4)

        yield gauge_statics

        if hasattr(self, ''):
            gauge_statics.add_metric(["xmx_size"], self.xmx_size)

        if hasattr(self,'salt_configuration'):
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

        if hasattr(self,'salt_keys'):
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

        if hasattr(self,'salt_jobs'):
            gauge3 = GaugeMetricFamily(
                "salt_jobs",
                "Information about Salt Jobs",
                labels=["jid", "fun"],
            )

            epoch_time = datetime(1970, 1, 1)
            for jid, fun, timestamp_sc in self.salt_jobs:
                #dt = datetime.strptime(timestamp_sc, "%Y, %b %d %H:%M:%S.%f")
                #delta = dt - epoch_time
                #total_seconds = delta.total_seconds()
                #gauge3.add_metric([jid, fun], 1, timestamp=total_seconds)

                ##seconds = int(total_seconds)
                ##nanoseconds = int((total_seconds - seconds) * 1e9)
                ##timestamp_obj = Timestamp(seconds, nanoseconds)
                ##gauge3.add_metric([jid, fun], 1, timestamp=timestamp_obj)

                gauge3.add_metric([jid, fun], 1)
            yield gauge3

        gauge_statics.add_metric(["xmx_size"], self.xmx_size)
        yield gauge_statics


def main():
    print("Supportconfig Exporter started")
    port = 9000
    frequency = 10
    supportconfig_path = "/home/ygutierrez/Documents/L3/1227859/scc_lxms-cp-suma01_240828_1131"
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
