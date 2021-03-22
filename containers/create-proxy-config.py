#!/usr/bin/python3

import os
from distutils.dir_util import copy_tree

# Create the config path if it doesn't exist
config_path = "./proxy-config"
if not os.path.exists(config_path):
    os.mkdir(config_path)

# Execute mgr-ssl-tool and copy the generated files to proxy-config
os.system('mgr-ssl-tool --gen-server --dir="/root/ssl-build" --set-country="US" \
    --set-state="STATE" --set-city="CITY" --set-org="ORGANIZATION" --set-org-unit="ORGANIZATION UNIT" \
    --set-email="name@example.com" --set-hostname="proxy.example.com" --set-cname="example.com"')

copy_tree("/root/ssl-build/proxy", config_path)
print ("The proxy config files were created, exiting.")

