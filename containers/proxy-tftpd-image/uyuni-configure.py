#!/usr/bin/python3

import os
import subprocess
import re
import yaml
import sys

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)
   
    server_version = config.get("server_version")
    # Only check version for SUSE Manager, not Uyuni
    matcher = re.fullmatch(r"([0-9]+\.[0-9]+\.)[0-9]+", server_version)
    if matcher:
        major_version = matcher.group(1)
        container_version = subprocess.run(["rpm", "-q", "--queryformat", "%{version}", "spacewalk-proxy-common"],
                stdout=subprocess.PIPE, universal_newlines=True).stdout
        if not container_version.startswith(major_version):
            print("FATAL: Proxy container image version (%s) doesn't match server major version (%s)".format(
                container_version, major_version), file=sys.stderr)
            sys.exit(1)
    
    tftp_config = "/etc/sysconfig/tftp"
    tftp_root = "/srv/tftpboot"
    with open(tftp_config, "w") as file:
        file.write(f'''# Automatically generated Uyuni Proxy Server configuration file.
        TFTP_USER="tftp"
        TFTP_OPTIONS="{config.get('tftp_options', '')} "
        TFTP_DIRECTORY="{tftp_root}"''')

    os.system("chown root:tftp {}".format(tftp_config))
    os.system("chmod 640 {}".format(tftp_config))

# Make sure we can read 
if not os.access(tftp_root, os.R_OK | os.X_OK):
   print("FATAL: TFTP root directory does not have correct permissions.")
   sys.exit(1)

print("DEBUG: configuration finished")
sys.exit(0)
