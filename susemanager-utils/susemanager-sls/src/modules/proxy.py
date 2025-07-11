# -*- coding: utf-8 -*-

# SPDX-FileCopyrightText: 2016-2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
Utility module to backup Uyuni and SUSE Multi-Linux Manager RPM-based proxies
"""

from __future__ import absolute_import

import configparser
import logging
import os
import re
import shutil
import subprocess
import tempfile
import yaml

from salt.exceptions import CommandExecutionError


log = logging.getLogger(__name__)

# Just for lint and static analysis, will be replaced by salt's loader
__grains__ = {}
__salt__ = {}


def _yaml_str_presenter(dumper, data):
    """configures yaml for dumping multiline strings
    Ref: https://stackoverflow.com/questions/8640959/how-can-i-control-what-scalar-form-pyyaml-uses-for-my-data"""
    if len(data.splitlines()) > 1:  # check for multiline string
        return dumper.represent_scalar('tag:yaml.org,2002:str', data, style='|')
    return dumper.represent_scalar('tag:yaml.org,2002:str', data)

yaml.add_representer(str, _yaml_str_presenter)
yaml.representer.SafeRepresenter.add_representer(str, _yaml_str_presenter) # to use with safe_dump

def backup():
    """
    Save the RPM-based proxy configuration on the server for future import as container proxy configuration.
    """
    required_files = [
        "/etc/rhn/rhn.conf",
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT",
        "/etc/squid/squid.conf",
        "/etc/sysconfig/rhn/systemid",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/authorized_keys",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push.pub",
    ]
    missing_files =[path for path in required_files if not os.path.isfile(path)]
    if any(missing_files):
        raise CommandExecutionError("{0} files are not available".format(" ".join(missing_files)))

    # Create temporary folder
    tmp_path = tempfile.mkdtemp()

    # Extract the config to temporary folder
    config_path = os.path.join(tmp_path, "config")
    try:
        os.makedirs(config_path)
        err = _extract_config(config_path)
        if err:
            raise CommandExecutionError(err)

        # TODO Copy the squid config to temporary folder
        # Finding all squid config files can be done with:
        # squid -k parse 2>&1 | sed -n '/Processing Configuration File:/{s/^.* Processing Configuration File: \([^ ]\+\).*/\1/ p}'

        # TODO Copy the apache config to temporary folder
        # Finding all the apache config files can be done with:
        # apache2-find-directives -v 2>/dev/null | sed -n '/CONFIG FILE:/{s/CONFIG FILE: \(.*\)$/\1/ p}'

        # TODO The problem with those sets of config files is how to find what the user added?
        # Otherwise it would shadow the default values from the container config.

        # Push the temporary folder to the server
        # TODO Nail down the proper upload_path
        __salt__["cp.push_dir"](tmp_path, upload_path="/backup")
    except Exception as e:
        shutil.rmtree(tmp_path)
        raise CommandExecutionError(e)

def _extract_config(dest):
    """
    Create the container proxy configuration files in dest path from an existing installation.
    An error message will be returned or an empty string.
    """

    # config.yaml
    with open("/etc/rhn/rhn.conf", "r") as conf_fd:
        conf_content = conf_fd.read()
        rhn_conf = configparser.ConfigParser()
        rhn_conf.read_string(f"[{configparser.DEFAULTSECT}]\n{conf_content}")

    ca_cert = _file_content("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT")

    cache_size = ""
    cache_dir = ""

    with open("/etc/squid/squid.conf", "r") as fd:
        for line in fd.readlines():
            parts = line.split(" ")
            if parts[0] == "cache_dir":
                cache_size = parts[3]
                cache_dir = parts[2]
                break

    if not cache_size or not cache_dir:
        return "Could not parse the cache_dir squid instruction to get the cache size and path"

    default_section = rhn_conf[configparser.DEFAULTSECT]
    config = {
        "server": default_section.get("proxy.rhn_parent"),
        "proxy_fqdn": __grains__["fqdn"],
        "max_cache_size_mb": cache_size,
        "ca_crt": ca_cert,
        "email": default_section.get("traceback_mail", ""),
        # Let the server_version be added by the server itself later when generating the new config
    }

    debug = default_section.get("debug", "")
    if debug:
        config["log_level"] = debug

    with open(os.path.join(dest, "config.yaml"), "w") as fd:
        yaml.safe_dump(config, fd, width=float("inf"))

    # httpd.yaml
    httpd = {
        # No need for systemid: this will be need to be updated later
        "server_crt": _find_apache_cert_file("SSLCertificateFile"),
        "server_key": _find_apache_cert_file("SSLCertificateKeyFile"),
    }

    with open(os.path.join(dest, "httpd.yaml"), "w") as fd:
        yaml.safe_dump(httpd, fd, width=float("inf"))
    
    # ssh.yaml
    ssh = {
        "server_ssh_push_pub": _file_content("/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push.pub"),
        "server_ssh_push": _file_content("/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push"),
        "server_ssh_key_pub": _file_content("/var/lib/spacewalk/mgrsshtunnel/.ssh/authorized_keys"),
    }

    with open(os.path.join(dest, "ssh.yaml"), "w") as fd:
        yaml.safe_dump(ssh, fd, width=float("inf"))

    return ""

def _find_apache_cert_file(directive):
    """
    Returns the content of a file based on a directive name.
    """
    out = subprocess.getoutput(f"apache2-find-directives  -d {directive} -v")
    path = ""
    for line in out.splitlines():
        matcher = re.match(f" *Output: \\[{directive} ([^]]+)\\]", line)
        if matcher is not None:
            path = matcher.group(1)
            break
    content = _file_content(path)
    return content

def _file_content(path):
    """
    Return the content of the file at the given path.
    """
    content = ""
    if path:
        with open(path, "r") as fd:
            content = fd.read()
    return content.strip()
