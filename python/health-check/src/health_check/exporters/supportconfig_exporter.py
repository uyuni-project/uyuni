# SPDX-FileCopyrightText: 2023 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0
"""
Main supportconfig exporter module that collects metrics into a json file
and serves the file by using an HTTP server.
"""

from collections import defaultdict, namedtuple
import os
from pathlib import Path
import re
import signal
import sys
from typing import Dict, Tuple
import yaml
import json

import http.server

import static_metrics
from static_metrics import metrics_config


def sigterm_handler(**kwargs):
    del kwargs  # unused
    print("Detected SIGTERM. Exiting.")
    sys.exit(0)


signal.signal(signal.SIGTERM, sigterm_handler)


class SupportConfigMetricsCollector:
    """A collector that collects metrics, and exports them into a file."""

    def __init__(self, supportconfig_path: str):
        if not supportconfig_path:
            raise ValueError("A 'supportconfig_path' must be set via config.yml file")

        self.supportconfig_path = supportconfig_path
        self.static_metrics_collection = (
            static_metrics.create_static_metrics_collection(self.supportconfig_path)
        )
        self.disk_layout = []
        self.salt_keys = []
        self.salt_jobs = []
        self.salt_configuration = []
        self.max_clients = -1
        self.server_limit = -1
        self.num_of_channels = -1
        self.shared_buffers_to_mem_ratio = -1
        self.major_version = -1
        self.fs_mount_insufficient = -1
        self.fs_mount_out_of_space = -1
        self.roles = [
            {
                "name": "master",
                "value": 0,
            },
            {
                "name": "proxy",
                "value": 0,
            },
            {
                "name": "client",
                "value": 0,
            },
        ]
        self.parse()

    def parse(self):
        self.salt_jobs = self.read_salt_jobs()
        self.salt_keys = self.read_salt_keys()
        self.salt_configuration = self.read_salt_configuration()

        self.get_static_metrics()

        self.parse_disk_layout()
        self.parse_roles()
        self.parse_prefork_c_params()
        self.parse_num_of_channels()
        if hasattr(self, "mem_total"):
            self.parse_shared_buffers_to_mem_ratio(self.mem_total)
        self.check_space_on_fs()

    def parse_shared_buffers_to_mem_ratio(self, memory: int):
        """
        Parse the ratio of PostgreSQL"s shared_buffers property to the amount of RAM.
        """
        filename = os.path.join(
            self.supportconfig_path, "spacewalk-debug/database/postgresql.conf"
        )
        if not os.path.exists(filename):
            return
        with open(filename, encoding="UTF-8") as f:
            content = f.read()
            regex = r"^shared_buffers\s*=\s*(\d+)(\w+)$"
            pattern = re.compile(regex, flags=re.MULTILINE)
            match = re.search(pattern, content)
            if not match:
                return
            shared_buffers = match.group(1)
            buffer_unit = match.group(2)

            if not shared_buffers.isnumeric():
                print(
                    f"Error when parsing shared_buffers; expected int, got: {shared_buffers}"
                )
                return

            shared_buffers = int(shared_buffers)

            buffer_unit = buffer_unit.lower()
            if buffer_unit == "kb":
                ...  # conversion done
            elif buffer_unit == "mb":
                shared_buffers *= 1024
            elif buffer_unit == "gb":
                shared_buffers *= 1024 * 1024
            else:
                print(f"Error when parsing shared buffer unit: {buffer_unit}")
                return

            self.shared_buffers_to_mem_ratio = round(shared_buffers / memory, 2)

    def parse_prefork_c_params(self):
        # Parse the MaxRequestWorkers and ServerLimit only from prefork.c section of the
        # /etc/apache2/server-tuning.conf file contents (stored in etc.txt)
        filename = os.path.join(self.supportconfig_path, "etc.txt")
        if not os.path.exists(filename):
            return

        with open(filename, encoding="UTF-8") as f:
            content = f.read()
            # Parse contents of server-tuning.conf first
            file_regex = (
                r"(?s)/etc/apache2/server-tuning.conf(.*?)\[ Configuration File \]"
            )
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
                    print(
                        f"Error when parsing max_clients; expected int, got: {max_clients}"
                    )

            if server_lim_match:
                try:
                    server_limit = server_lim_match.groups()[-1]
                    self.server_limit = int(server_limit)
                except ValueError:
                    print(
                        f"Error when parsing ServerLimit; expected int, got: {server_limit}"
                    )

    def parse_roles(self):
        mapping = {
            "plugin-susemanagerclient.txt": "client",
            "plugin-susemanagerproxy.txt": "proxy",
            "plugin-susemanager.txt": "master",
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

    def _check_vol_params(self, mount: str, min_size_gb: int, fs: Dict) -> Dict:
        """
        Given a mount path, e.g. "/a/b/c", recursively search for
        subpaths of the path until we find the mount in the disk layout.

        For example, if "/a/b/c" is not present in disk layout, we search for
        "/a/b", "/a", and finally "/".

        Can return multiple duplicate returns, e.g. when checking for two non-existent mounts
        /foo/bar and /foo/baz, both of which have common path /, then the result will
        contain two "/" entries. However, /foo/bar might have different min_size req., so
        the result for the two entries might be different, e.g. "/foo/bar" reduces to "/", which
        fulfills the min requirement for "/foo/bar" but not for "/foo/baz".

        See self.parse_disk_layout for more information about disk layout parsing.
        Return a dict with "too_small" and "out_of_space" props, where 1 = True and 0 = False.
        This is necessary for Grafana alerts, which cannot work with True/False.
        """
        res = {"mount": mount, "too_small": -1, "out_of_space": -1}

        if mount not in fs:
            if mount == "/":
                return {}
            mount = os.path.dirname(mount)
            return self._check_vol_params(mount, min_size_gb, fs)
        disk = fs[mount]
        used = int(disk["use %"][:-1])

        res["out_of_space"] = 1 if used > 90 else 0

        # size format: 2T, or 560G
        size = disk["size"]
        if size[-1:].isnumeric():
            unit = "n/a"
        else:
            size, unit = float(size[:-1]), size[-1:]

        unit = unit.lower()
        if unit == "k":
            size /= 1024 * 1024
        elif unit == "m":
            size /= 1024
        elif unit == "g":
            ...  # already in GB
        elif unit == "t":
            size *= 1024
        elif unit == "n/a":
            ...  # no unit
        else:
            print(f"Error when parsing shared buffer unit: {unit}")

        res["too_small"] = 1 if min_size_gb > size else 0
        return res

    def _parse_path_data(self, mounts: Dict) -> Tuple:
        """
        Reduce _check_vol_params output dictionary to "is there any mount that is
        too small" and "is there any mount that is running out of space".
        """
        too_small, out_of_space = 0, 0
        for mount_data_list in mounts.values():
            for mount in mount_data_list:
                too_small = max(mount["too_small"], too_small)
                out_of_space = max(mount["out_of_space"], out_of_space)
        return too_small, out_of_space

    def _gen_mounts_for_checking(self) -> Dict:
        """
        Generate a list of mounts and their minimum requirements per SLM version and role.
        """
        res = {
            4: {
                "master": [],
                "proxy": [],
            },
            5: {
                "master": [],
                "proxy": [],
            },
        }
        path_conf = namedtuple(
            "PathConf",
            ["mount", "min_size_gb", "alternate_mount"],
            defaults=(None, None, None),
        )

        # MLM 4.x, master
        res[4]["master"].append(path_conf("/", 40))
        res[4]["master"].append(path_conf("/var/lib/pgsql", 50, "/pgsql_storage"))
        res[4]["master"].append(path_conf("/var/spacewalk", 100, "/manager_storage"))
        res[4]["master"].append(path_conf("/var/cache", 10))
        # MLM 4.x, proxy
        res[4]["proxy"].append(path_conf("/srv", 100))
        res[4]["proxy"].append(path_conf("/var/cache", 100))
        # MLM 5.x, master
        res[5]["master"].append(path_conf("/", 20))
        res[5]["master"].append(
            path_conf(
                "/var/lib/containers/storage/volumes/var-pgsql", 50, "/pgsql_storage"
            )
        )
        res[5]["master"].append(
            path_conf(
                "/var/lib/containers/storage/volumes/var-pgsql", 100, "/manager_storage"
            )
        )
        res[5]["master"].append(
            path_conf("/var/lib/containers/storage/volumes/var-cache", 10)
        )
        # MLM 5.x, proxy
        res[5]["proxy"].append(
            path_conf(
                "/",
                40,
            )
        )
        res[5]["proxy"].append(
            path_conf("/var/lib/containers/storage/volumes/srv-www", 100)
        )
        res[5]["proxy"].append(
            path_conf("/var/lib/containers/storage/volumes/var-cache", 100)
        )
        return res

    def check_space_on_fs(self):
        """
        Check whether a SLM system (master or proxy) contains any mount path that is too small,
        or that is running out of space.
        """
        mounts = defaultdict(list)
        fs = {disk["mounted on"]: disk for disk in self.disk_layout}

        role = [role for role in self.roles if role["value"] == 1]
        if not role:
            print(
                "Cannot determine filesystem requirements; cannot determine server role (master, minion, proxy?)"
            )
            return
        role = role.pop()["name"]

        paths = (
            self._gen_mounts_for_checking().get(self.major_version, {}).get(role, {})
        )
        if not paths:
            print("Cannot determine filesystem requirements")
            return

        for path in paths:
            mount = (
                path.alternate_mount
                if path.alternate_mount and (path.alternate_mount in fs)
                else path.mount
            )
            fs_obj = self._check_vol_params(mount, path.min_size_gb, fs)
            if not fs_obj:
                print(f"Could not find {mount}")
                continue
            mounts[fs_obj["mount"]].append(fs_obj)

        self.fs_mount_insufficient, self.fs_mount_out_of_space = self._parse_path_data(
            mounts
        )

    def parse_disk_layout(self):
        diskinfo_path = os.path.join(
            self.supportconfig_path, "spacewalk-debug/diskinfo"
        )
        if not os.path.isfile(diskinfo_path):
            return
        with open(diskinfo_path, encoding="UTF-8") as f:
            # skip header
            next(f)
            for line in f:
                # Filesystem, size, used, avail, use%, mounted on
                cols = line.split()
                if len(cols) < 6:
                    continue
                self.disk_layout.append(
                    {
                        "mounted on": cols[5],
                        "size": cols[1],
                        "available": cols[3],
                        "use %": cols[4],
                        "filesystem": cols[0],
                    }
                )

    def read_salt_configuration(self):
        salt_conf_path = os.path.join(
            self.supportconfig_path, "plugin-saltconfiguration.txt"
        )
        if not os.path.isfile(salt_conf_path):
            return []
        content = None
        with open(salt_conf_path, encoding="UTF-8") as f:
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
                "value": int(
                    re.findall(f"^{attr}: ([0-9]+)$", content, re.MULTILINE)[-1]
                ),
            }
            ret.append(prop)
        return ret

    def read_salt_keys(self):
        salt_keys_path = os.path.join(
            self.supportconfig_path, "plugin-saltminionskeys.txt"
        )
        if not os.path.isfile(salt_keys_path):
            return []
        content = None
        with open(salt_keys_path, encoding="UTF-8") as f:
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
            }
            prop["length"] = len(
                parsed[i].strip().split("\n") if parsed[i].strip() else []
            )
            ret.append(prop)
        return ret

    def read_salt_jobs(self):
        salt_jobs_path = os.path.join(self.supportconfig_path, "plugin-saltjobs.txt")
        if not os.path.isfile(salt_jobs_path):
            return []
        content = None
        with open(salt_jobs_path, encoding="UTF-8") as f:
            content = f.read()
        jobs_totals = {}
        job_matches = re.findall(
            r"^'([0-9]+)':[\s\S]*?Function:\s+([\w.]+)[\s\S]*?StartTime:\s+(\d{4},\s\w{3}\s\d{2}\s\d{2}:\d{2}:\d{2}\.\d{6})",
            content,
            re.MULTILINE,
        )

        for job_match in job_matches:
            _, fun_, _ = job_match[0], job_match[1], job_match[2]
            if fun_ in jobs_totals:
                jobs_totals[fun_] += 1
            else:
                jobs_totals[fun_] = 1

        return [{"fun": k, "count": v} for k, v in jobs_totals.items()]

    def get_static_metrics(self):
        for name, static_metric in self.static_metrics_collection.items():
            if static_metric.is_present():
                val = static_metric.get_value()
                setattr(self, name, val)

    def merge_metrics(self):
        ret = {
            "java_config": [],
            "config": [],
            "apache": [],
            "postgresql": [],
            "hw": [],
            "memory": [],
            "disk": self.disk_layout,
            "salt_configuration": self.salt_configuration,
            "salt_keys": self.salt_keys,
            "salt_jobs": self.salt_jobs,
            "misc": self.roles,
        }
        self._append_static_properties(ret)
        self._append_value_to_dict(ret, "apache", "max_clients")
        self._append_value_to_dict(ret, "apache", "server_limit")
        self._append_value_to_dict(ret, "misc", "num_of_channels")
        self._append_value_to_dict(ret, "postgresql", "shared_buffers_to_mem_ratio")
        self._append_value_to_dict(ret, "memory", "fs_mount_insufficient")
        self._append_value_to_dict(ret, "memory", "fs_mount_out_of_space")

        return ret

    def _append_static_properties(self, res_dict: Dict):
        for metric_name, metric_obj in metrics_config.items():
            self._append_value_to_dict(
                res_dict, metric_obj.get("label", "config"), metric_name
            )

    def parse_num_of_channels(self) -> None:
        """
        Approximate the number of active channels by counting reposync log files modified within 24h
        of the most recently modified file.
        """
        reposync_log_path = Path(
            f"{self.supportconfig_path}/spacewalk-debug/rhn-logs/rhn/reposync"
        )
        if not reposync_log_path.exists():
            return
        log_files = sorted(
            reposync_log_path.iterdir(), key=os.path.getmtime, reverse=True
        )
        most_recent_mtime = os.path.getmtime(log_files[0])
        one_day_seconds = 86400

        log_files = [
            log_f
            for log_f in log_files
            if most_recent_mtime - os.path.getmtime(log_f) <= one_day_seconds
        ]
        self.num_of_channels = len(log_files)

    def _append_value_to_dict(
        self, res_dict: Dict, dict_property: str, prop: str
    ) -> None:
        """
        Mutate res_dict such that any `self.prop`, if it exists, is added to res_dict[dict_property]
        as a {"name": prop, "value": self.prop} object.

        This is required to parse the resulting JSON by Grafana.
        """
        if not res_dict.get(dict_property):
            res_dict[dict_property] = []
        if hasattr(self, prop):
            prop_val = getattr(self, prop)
            res_dict[dict_property].append({"name": prop, "value": prop_val})

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
    if not os.path.exists("config.yml"):
        print("Could not find config.yml")
        exit(1)

    with open("config.yml", "r", encoding="UTF-8") as config_file:
        config = yaml.safe_load(config_file)
        port = int(config["port"])
        supportconfig_path = config["supportconfig_path"]

    collector = SupportConfigMetricsCollector(supportconfig_path)
    collector.write_metrics()
    with http.server.ThreadingHTTPServer(("", port), Handler) as httpd:
        print("serving at port", port)
        httpd.serve_forever()


if __name__ == "__main__":
    main()
