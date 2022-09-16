#!/usr/bin/python3

import logging
import os
import yaml
import sys

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)
   
<<<<<<< HEAD
    if config.get('log_level') is not None:
        logging.getLogger().setLevel(logging.getLevelName(config.get('log_level')))

=======
>>>>>>> 2ad6b420a6d9d7f5ddd431d26346efbe2f72b840
    # store SSL CA certificate
    os.mkdir("/usr/share/uyuni/")
    with open("/usr/share/uyuni/ca.crt", "w") as file:
        file.write(config.get("ca_crt"))

    tftp_config = "/etc/sysconfig/tftp"
    tftp_root = "/srv/tftpboot"
    with open(tftp_config, "w") as file:
        file.write(f'''# Automatically generated Uyuni Proxy Server configuration file.
TFTP_USER="tftp"
TFTP_OPTIONS="{config.get('tftp_options', '')} "
TFTP_DIRECTORY="{tftp_root}"''')

<<<<<<< HEAD
    os.system(f"chmod 640 {tftp_config}")
=======
    os.system("chmod 640 {}".format(tftp_config))
>>>>>>> 2ad6b420a6d9d7f5ddd431d26346efbe2f72b840

# Make sure we can read 
if not os.access(tftp_root, os.R_OK | os.X_OK):
   logging.critical("TFTP root directory does not have correct permissions.")
   sys.exit(1)

sys.exit(0)
