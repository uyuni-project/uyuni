# SPDX-FileCopyrightText: 2023 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

import os
from pathlib import Path
import re
import signal
import sys
from typing import Dict
import yaml
import json

import http.server
import socketserver

import static_metrics

def sigterm_handler(**kwargs):
    del kwargs # unused
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
        self.salt_keys = []
        self.salt_jobs = []
        self.salt_configuration = []
        self.max_clients = -1
        self.server_limit = -1
        self.roles = [
            {
                'name':'master',
                'value': 0,
            },
            {
                'name':'proxy',
                'value': 0,
            },
            {
                'name': 'client',
                'value': 0,
            },
        ]
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
        self.parse_roles()
        self.parse_prefork_c_params()

    def parse_prefork_c_params(self):
        # Parse the MaxRequestWorkers and ServerLimit only from prefork.c section of the
        # /etc/apache2/server-tuning.conf file contents (stored in etc.txt)
        filename = os.path.join(self.supportconfig_path, "etc.txt")
        if not os.path.exists(filename):
            return

        with open(filename) as f:
            content = f.read()
            # Parse contents of server-tuning.conf first
            file_regex = r"(?s)/etc/apache2/server-tuning.conf(.*?)\[ Configuration File \]"
            # Then, parse the MaxRequestWorkers property from the prefork part
            max_req_regex = r"(?s)<IfModule prefork\.c>(.*?)MaxRequestWorkers\s+(\d+)$"
            # Finally, parse ServerLimit prop
            server_lim_regex = r"(?s)<IfModule prefork\.c>(.*?)ServerLimit\s+(\d+)$"

            file_pattern = re.compile(file_regex, flags=re.MULTILINE)
            match = re.search(file_pattern, content)
            if not match:
                return

            max_req_pattern = re.compile(max_req_regex, flags=re.MULTILINE)
            server_lim_pattern = re.compile(server_lim_regex, flags=re.MULTILINE)

            max_req_match = re.search(max_req_pattern, match.group(0))
            server_lim_match = re.search(server_lim_pattern, match.group(0))

            if max_req_match:
                try:
                    max_clients = max_req_match.groups()[-1]
                    self.max_clients = int(max_clients)
                except ValueError:
                    print(f"Error when parsing max_clients; expected int, got: {max_clients}")

            if server_lim_match:
                try:
                    server_limit = server_lim_match.groups()[-1]
                    self.server_limit = int(server_limit)
                except ValueError:
                    print(f"Error when parsing ServerLimit; expected int, got: {server_limit}")

    def parse_roles(self):
        mapping = {
            'plugin-susemanagerclient.txt': 'client',
            'plugin-susemanagerproxy.txt': 'proxy',
            'plugin-susemanager.txt': 'master'
        }

        for file, role in mapping.items():
            file_path = os.path.join(self.supportconfig_path, file)
            if os.path.exists(file_path):
                for role_obj in self.roles:
                    if role_obj["name"] == role:
                        role_obj["value"] = 1

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
            prop["length"] = len(prop["value"])
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
            "config": [],
            "hw": [],
            "memory": [],
            "disk": self.disk_layout,
            "salt_configuration": self.salt_configuration,
            "salt_keys": self.salt_keys,
            "salt_jobs": self.salt_jobs,
            "misc": self.roles,
        }

        self._append_value_to_dict(ret, "config", "max_clients")
        self._append_value_to_dict(ret, "config", "queued_salt_events")
        self._append_value_to_dict(ret, "config", "server_limit")
        self._append_value_to_dict(ret, "config", "java_salt_batch_size")
        self._append_value_to_dict(ret, "config", "tomcat_xmx_size")
        self._append_value_to_dict(ret, "config", "max_threads")
        self._append_value_to_dict(ret, "hw", "cpu_count")

        ret["memory"] = self._append_memory_props()

        return ret

    def _append_value_to_dict(self, res_dict: Dict, dict_property: str, prop: str) -> None:
        """
        Mutate res_dict such that any `self.prop`, if it exists, is added to res_dict[dict_property]
        as a {"name": prop, "value": self.prop} object.
        """
        if not res_dict.get(dict_property):
            res_dict[dict_property] = []
        if hasattr(self, prop):
            prop_val = getattr(self, prop)
            res_dict[dict_property].append({"name": prop, "value": prop_val})

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
