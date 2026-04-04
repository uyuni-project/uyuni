# -*- coding: utf-8 -*-

# SPDX-FileCopyrightText: 2025 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

"""
Utility module to back up Uyuni and SUSE Multi-Linux Manager RPM-based proxies
"""

from __future__ import absolute_import

import configparser
import logging
import glob
import os
import re
import shutil
import subprocess
import tempfile
import yaml

from salt.exceptions import CommandExecutionError


log = logging.getLogger(__name__)

# Taken from Kiwi sources https://github.com/OSInside/kiwi/blob/eb2b1a84bf7/kiwi/schema/kiwi.rng#L81
KIWI_ARCH_REGEX = r"(x86_64|i586|i686|ix86|aarch64|arm64|armv5el|armv5tel|armv6hl|armv6l|armv7hl|armv7l|ppc|ppc64|ppc64le|s390|s390x|riscv64)"

# PXE entries matchers. We keep grub and pxelinux entries identical.
# Parse pxelinux.cfg entries as they are easier
kernel_line_match = re.compile(r" *kernel (([^/]+)/.+)$")
initrd_line_match = re.compile(r" *append initrd=([^ ]+) (.*)$")
image_name_match = re.compile(r"([^.]+)." + KIWI_ARCH_REGEX + "(.+)$")

# Just for lint and static analysis, will be replaced by salt's loader
__grains__ = {}
__salt__ = {}


def _yaml_str_presenter(dumper, data):
    """configures yaml for dumping multiline strings
    Ref: https://stackoverflow.com/questions/8640959/how-can-i-control-what-scalar-form-pyyaml-uses-for-my-data
    """
    if len(data.splitlines()) > 1:  # check for multiline string
        return dumper.represent_scalar("tag:yaml.org,2002:str", data, style="|")
    return dumper.represent_scalar("tag:yaml.org,2002:str", data)


yaml.add_representer(str, _yaml_str_presenter)
yaml.representer.SafeRepresenter.add_representer(
    str, _yaml_str_presenter
)  # to use with safe_dump


def backup():
    """
    Save the RPM-based proxy configuration on the server for future import as container proxy configuration.
    """
    required_files = [
        "/etc/rhn/rhn.conf",
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT",
        "/etc/squid/squid.conf",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/authorized_keys",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push",
        "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push.pub",
    ]

    missing_files = [path for path in required_files if not os.path.isfile(path)]
    if any(missing_files):
        raise CommandExecutionError(
            f'{" ".join(missing_files)} files are not available'
        )

    optional_pxe_files = _get_available_files(["/srv/saltboot/boot/pxelinux.cfg/01-*"])

    # Create temporary folder
    tmp_path = tempfile.mkdtemp()

    # Extract the config to temporary folder
    config_path = os.path.join(tmp_path, "proxy_config")
    try:
        os.makedirs(config_path)
        err = _extract_config(config_path)
        if err:
            raise CommandExecutionError(err)

        if len(optional_pxe_files) > 0:
            # This is retail with Saltboot pxe entries
            _extract_pxe_entries(
                optional_pxe_files, os.path.join(config_path, "pxe_entries.yaml")
            )

        # Push the temporary folder to the server
        __salt__["cp.push_dir"](tmp_path, upload_path="/")

        uploaded_files = [
            os.path.join("proxy_config", f) for f in os.listdir(config_path)
        ]
        # Send event to the master about finished backup
        __salt__["event.send"]("suse/proxy/backup_finished", {"files": uploaded_files})
        return uploaded_files
    except Exception as e:
        raise CommandExecutionError(e) from e
    finally:
        shutil.rmtree(tmp_path)


def _get_available_files(files):
    """
    Checks a list of file paths and glob patterns and returns a list
    of all files that actually exist on the filesystem.

    Returns:
        list: A list of unique file paths for all files that were found.
    """
    found_files = set()
    for item in files:
        matches = glob.glob(item)
        if matches:
            found_files.update(matches)
    return sorted(list(found_files))


def _extract_pxe_entries(files, outfile):
    """
    Parses the PXE boot entries.

    Creates a PXE entries YAML outfile file.
    Raises an exception if parsing fails
    """
    entries = []
    for f in files:
        res = _parse_single_pxe(f)
        entries.append(res)

    data = {
        "branch_id": __salt__["grains.get"]("pxe:branch_id"),
        "pxe_entries": entries,
    }

    with open(outfile, "x", encoding="utf-8") as fd:

        yaml.safe_dump(data, fd, width=float("inf"))


def _parse_single_pxe(filename):
    """
    Parses the content of a single PXE boot entry.

    Returns:
        A dictionary containing the parsed information with keys:
        'mac', 'kernel', 'probable_boot_image', 'initrd', and 'args'.
        Raises an exception if parsing fails
    """
    result = {}

    # Extract the MAC address from the filename by removing the '01-' prefix.
    base_filename = os.path.basename(filename)
    if base_filename.startswith("01-"):
        # Store the MAC in the cobbler format for easier matching later
        result["mac"] = base_filename[3:].lower().replace("-", ":")
    else:
        raise ValueError("Not a valid PXE entry name")

    content = _file_content(filename)
    for line in content.splitlines():
        line = line.strip()

        # Find the line with the kernel information
        if line.lower().startswith("kernel"):
            kernel_match = kernel_line_match.match(line)
            if kernel_match:
                result["kernel"] = kernel_match.group(1)
                image_match = image_name_match.match(kernel_match.group(2))
                if image_match:
                    # This skips the arch part which is part of the filename, but not the image
                    result["probable_boot_image"] = image_match.group(
                        1
                    ) + image_match.group(3)
                else:
                    raise ValueError("Not a valid boot image name format")
            else:
                raise ValueError("Not a valid kernel definition")

        # Find the line with the append arguments
        elif line.lower().startswith("append"):
            initrd_match = initrd_line_match.match(line)
            if initrd_match:
                result["initrd"] = initrd_match.group(1)
                result["args"] = initrd_match.group(2).strip()
            else:
                raise ValueError("Not a valid initrd definition")
    return result


def _extract_config(dest):
    """
    Create the container proxy configuration files in dest path from an existing installation.
    An error message will be returned or an empty string.
    """

    # config.yaml
    with open("/etc/rhn/rhn.conf", "r", encoding="utf-8") as conf_fd:
        conf_content = conf_fd.read()
        rhn_conf = configparser.ConfigParser()
        rhn_conf.read_string(f"[{configparser.DEFAULTSECT}]\n{conf_content}")

    ca_cert = _file_content("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT")

    cache_size = ""
    cache_dir = ""

    with open("/etc/squid/squid.conf", "r", encoding="utf-8") as fd:
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
        "max_cache_size_mb": int(cache_size),
        "ca_crt": ca_cert,
        "email": default_section.get("traceback_mail", ""),
        # Let the server_version be added by the server itself later when generating the new config
    }

    debug = default_section.get("debug", "")
    if debug:
        config["log_level"] = debug

    with open(os.path.join(dest, "config.yaml"), "w", encoding="utf-8") as fd:
        yaml.safe_dump(config, fd, width=float("inf"))

    # httpd.yaml
    httpd = {
        # No need for systemid: this will be need to be updated later
        "server_crt": _find_apache_cert_file("SSLCertificateFile"),
        "server_key": _find_apache_cert_file("SSLCertificateKeyFile"),
    }

    with open(os.path.join(dest, "httpd.yaml"), "w", encoding="utf-8") as fd:
        yaml.safe_dump(httpd, fd, width=float("inf"))

    # ssh.yaml
    ssh = {
        "server_ssh_push_pub": _file_content(
            "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push.pub"
        ),
        "server_ssh_push": _file_content(
            "/var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push"
        ),
        "server_ssh_key_pub": _file_content(
            "/var/lib/spacewalk/mgrsshtunnel/.ssh/authorized_keys"
        ),
    }

    with open(os.path.join(dest, "ssh.yaml"), "w", encoding="utf-8") as fd:
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
        with open(path, "r", encoding="utf-8") as fd:
            content = fd.read()
    return content.strip()


def info():
    """
    Return information on containerized proxy and its config.
    """
    err, out = subprocess.getstatusoutput("mgrpxy --version")
    proxy_dir = "/etc/uyuni/proxy"
    conf_files = [
        f"{proxy_dir}/httpd.yaml",
        f"{proxy_dir}/ssh.yaml",
        f"{proxy_dir}/config.yaml",
    ]
    has_config = all([os.path.isfile(conf) for conf in conf_files])
    return {
        "mgrpxy_version": out if err == 0 else None,
        "has_config": has_config,
    }
