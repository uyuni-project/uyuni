#!/usr/bin/python3

import glob
import os
import shutil
import socket

# Create the config path directory in the current directory
config_path = "./proxy-config"
if os.path.exists(config_path):
    shutil.rmtree(config_path)
os.mkdir(config_path)

# Execute mgr-ssl-tool and copy the generated files to proxy-config
ssl_build_path = "/root/ssl-build"
os.system('mgr-ssl-tool --gen-server --dir="/root/ssl-build" --set-country="US" \
    --set-state="STATE" --set-city="CITY" --set-org="ORGANIZATION" --set-org-unit="ORGANIZATION UNIT" \
    --set-email="name@example.com" --set-hostname="proxy.example.com" --set-cname="example.com"')

# Copy the CA file and the generated RPM
ca_file = ssl_build_path + "/RHN-ORG-TRUSTED-SSL-CERT"
shutil.copy(ca_file, config_path)
shutil.copy(glob.glob('/root/ssl-build/proxy/*.noarch.rpm')[-1], config_path)

# Print some output and environment variables
print("\nThe proxy config files were created, environment variables:")
print("UYUNI_MASTER=%s" % socket.getfqdn())

