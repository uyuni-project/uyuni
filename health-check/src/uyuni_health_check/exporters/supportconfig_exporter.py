# SPDX-FileCopyrightText: 2023 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import os
from pathlib import Path
import re
import signal
import sys
import yaml
import json

import http.server
import socketserver

import static_metrics

def sigterm_handler(**kwargs):
    print("Detected SIGTERM. Exiting.")
    sys.exit(0)


signal.signal(signal.SIGTERM, sigterm_handler)


class SupportConfigMetricsCollector:
    def __init__(self, supportconfig_path=None):
        if not supportconfig_path:
            raise ValueError("A 'supportconfig_path' must be set via config.yml file")

        self.supportconfig_path = supportconfig_path
        self.static_metrics_collection = static_metrics.create_static_metrics_collection(self.supportconfig_path)
        self.disk_layout = []
        self.parse()

    def parse(self):
        if self.exists_salt_jobs_file():
            self.salt_jobs = self.read_salt_jobs()
        if self.exists_salt_keys_file():
            self.salt_keys = self.read_salt_keys()
        if self.exists_salt_configuration_file():
            self.salt_configuration = self.read_salt_configuration()

        self.get_static_metrics()
        self.parse_disk_layout()

    def _parse_command(self, command_block):
        lines = command_block.strip().split("\n")
        command = lines[0][2:]
        return command, lines[1:]

    def parse_disk_layout(self):
        diskinfo_path = os.path.join(self.supportconfig_path, "spacewalk-debug/diskinfo")
        if not os.path.isfile(diskinfo_path):
            return
        with open(diskinfo_path) as f:
            # skip header
            next(f)
            for line in f:
                # Filesystem, size, used, avail, use%, mounted on
                cols = line.split()
                if len(cols) < 6:
                    continue
                self.disk_layout.append({
                    "mounted on": cols[5],
                    "size": cols[1],
                    "available": cols[3],
                    "use %": cols[4],
                    "filesystem": cols[0],
                })

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
        ret = []
        for attr in attrs_to_expose:
            prop = {
                "name": attr,
                "value": int(re.findall(f"^{attr}: ([0-9]+)$", content, re.MULTILINE)[-1]),
            }
            ret.append(prop)
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
        ret = []
        parsed = re.findall(
            r"^Accepted Keys:$((?:\n.*)*)\nDenied Keys:$((?:\n.*)*)\n"
            r"Unaccepted Keys:$((?:\n.*)*)\nRejected Keys:$((?:\n.*)*)#==",
            content,
            re.MULTILINE,
        )[0]
        key_types = [
            "accepted",
            "denied",
            "unaccepted",
            "rejected",
        ]
        for i, key_type in enumerate(key_types):
            prop = {
                "name": key_type,
                "value": parsed[i].strip().split("\n") if parsed[i].strip() else []
            }
            ret.append(prop)
        return ret
    
    def exists_salt_jobs_file(self):
        if os.path.isfile(os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")):
            return True

    def read_salt_jobs(self):
        content = None
        with open(os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")) as f:
            content = f.read()
        res = []
        job_matches = re.findall(
            "^'([0-9]+)':[\s\S]*?Function:\s+([\w.]+)[\s\S]*?StartTime:\s+(\d{4},\s\w{3}\s\d{2}\s\d{2}:\d{2}:\d{2}\.\d{6})", content, re.MULTILINE
        )
        for job_match in job_matches:
            res.append({
                "id": job_match[0],
                "fun": job_match[1],
                "start_time": job_match[2],
            })
        return res

    def get_static_metrics(self):
        for name, static_metric in self.static_metrics_collection.items():
            if static_metric.is_present():
                val = static_metric.get_value()
                setattr(self, name, val)

    def merge_metrics(self):
        ret = {
            "tomcat": [],
            "hw": [],
            "memory": [],
            "disk": self.disk_layout,
            "salt_configuration": {},
            "salt_keys": {},
            "salt_jobs": {},
        }
        if hasattr(self,'tomcat_xmx_size'):
            ret["tomcat"].append({"name": "tomcat_xmx_size", "value": self.tomcat_xmx_size})

        if hasattr(self, 'max_threads_ipv4'):
            ret["tomcat"].append({"name": "max_threads_ipv4", "value": self.max_threads_ipv4})

        if hasattr(self, 'cpu_count'):
            ret["hw"].append({"name": "cpu_count", "value": self.cpu_count})

        if hasattr(self,'salt_configuration'):
            ret["salt_configuration"] = self.salt_configuration

        if hasattr(self,'salt_keys'):
            ret["salt_keys"] = self.salt_keys

        if hasattr(self,'salt_jobs'):
            ret["salt_jobs"] = self.salt_jobs

        ret['memory'] = self._append_memory_props()

        return ret

    def _append_memory_props(self):
        ret = []
        for prop in ["mem", "swap"]:
            for prop_type in ["total", "used", "free"]:
                prop_name = f"{prop}_{prop_type}"
                if hasattr(self, prop_name):
                    ret.append({
                        "name": prop_name,
                        "value": getattr(self, prop_name),
                    })
        return ret

    def write_metrics(self):
        metrics = self.merge_metrics()
        filename = Path("/opt/metrics/metrics.json")
        filename.parent.mkdir(parents=True, exist_ok=True)

        with open(filename, "w", encoding="UTF-8") as f:
            json.dump(metrics, f, indent=4)

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="/opt/metrics", **kwargs)

def main():
    print("Supportconfig Exporter started")
    if os.path.exists("config.yml"):
        with open("config.yml", "r") as config_file:
            try:
                config = yaml.safe_load(config_file)
                port = int(config["port"])
                supportconfig_path = config["supportconfig_path"]
            except yaml.YAMLError as error:
                print(f"Could not load {config_file}: {error}")

    collector = SupportConfigMetricsCollector(supportconfig_path)
    collector.write_metrics()
    with socketserver.TCPServer(("", port), Handler) as httpd:
        print("serving at port", port)
        httpd.serve_forever()

if __name__ == "__main__":
    main()
