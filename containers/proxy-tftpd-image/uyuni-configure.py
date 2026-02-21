#!/usr/bin/python3
# pylint: disable=invalid-name
"""Configure script for proxy TFTP container."""

import os
import yaml
import sys

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    # store SSL CA certificate
    if "ca_crt" in config and not os.path.exists("/etc/uyuni/ca.crt"):
        with open("/etc/uyuni/ca.crt", "w", encoding="utf-8") as file:
            file.write(config.get("ca_crt"))

sys.exit(0)
