#!/usr/bin/python3
# pylint: disable=invalid-name
"""Configure script for proxy TFTP"""

import logging
import os
import yaml
import sys

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    # log_level is the value for rhn.conf and should be a positive integer
    log_level = logging.WARNING if config.get("log_level") == 1 else logging.DEBUG
    logging.getLogger().setLevel(log_level)

    # store SSL CA certificate
    os.mkdir("/usr/share/uyuni/")
    with open("/usr/share/uyuni/ca.crt", "w", encoding="utf-8") as file:
        file.write(config.get("ca_crt"))

    tftp_config = "/etc/sysconfig/tftp"
    tftp_root = "/srv/tftpboot"
    with open(tftp_config, "w", encoding="utf-8") as file:
        file.write(
            f'''# Automatically generated Uyuni Proxy Server configuration file.
TFTP_USER="tftp"
TFTP_OPTIONS="{config.get('tftp_options', '')} "
TFTP_DIRECTORY="{tftp_root}"'''
        )

    os.system(f"chmod 640 {tftp_config}")

# Make sure we can read
if not os.access(tftp_root, os.R_OK | os.X_OK):
    logging.critical("TFTP root directory does not have correct permissions.")
    sys.exit(1)

sys.exit(0)
