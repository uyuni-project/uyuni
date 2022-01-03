#
# Copyright (c) 2021 SUSE LLC
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#

import subprocess
import xml.etree.ElementTree as ET
from urllib.parse import urlparse, parse_qs
import json
import sys
from pathlib import Path
import glob
import os
from collections import namedtuple

INPUT_TEMPLATE = """RESOLVEURL
credentials: %s
path: %s

\0
"""

CREDENTIALS_NAME = "SCCcredentials"

def system_exit(code, messages=None):
    "Exit with a code and optional message(s). Saved a few lines of code."

    for message in messages:
        print(message, file=sys.stderr)
    sys.exit(code)

def is_payg_instance():
    return os.path.isfile('/usr/sbin/registercloudguest')

SuseCloudInfo = namedtuple('SuseCloudInfo', ['header_auth', 'hostname'])

def _get_suse_cloud_info():
    input = INPUT_TEMPLATE % (CREDENTIALS_NAME, "/")
    try:
        auth_data_output = subprocess.check_output("/usr/lib/zypp/plugins/urlresolver/susecloud", input=input, stderr=subprocess.PIPE, universal_newlines=True)
    except subprocess.CalledProcessError as e:
        system_exit(3, ["Got error when getting repo processed URL and headers(error {}):".format(e)])

    full_output = auth_data_output.split("\n")
    _, header_auth, _, repository_url = full_output
    repository_url_parsed = urlparse(repository_url)

    return SuseCloudInfo(header_auth, repository_url_parsed.netloc)

def _extract_http_auth(credentials):
    credentials_file = '/etc/zypp/credentials.d/' + credentials
    if not Path(credentials_file).exists():
        system_exit(5, ["Credentials file not found ({})".format(credentials_file)])
    with open(credentials_file) as credFile:
        username = ""
        password = ""
        for line in credFile:
            name, _, var = line.partition("=")
            if "username" == name.strip():
                username = var.strip()
            elif "password" == name.strip():
                password = var.strip()
    return {"username": username, "password": password}

def _extract_rmt_server_info(netloc):
    try:
        # we need to find the IP address, since it is not resolvable in any DNS. It is hardcoded in the hosts file
        host_ip_output = subprocess.check_output(["getent", "hosts", netloc], stderr=subprocess.PIPE, universal_newlines=True)
    except subprocess.CalledProcessError as e:
        system_exit(4, ["unable to get ip for repository server (error {}):".format(e)])

    server_ip = host_ip_output.split(" ")[0].strip()
    ca_cert_path = "/usr/share/pki/trust/anchors/registration_server_%s.pem" % server_ip.replace('.','_')
    if not Path(ca_cert_path).exists():
        system_exit(6, ["CA file for server {} not found ({})".format( server_ip, ca_cert_path)])
    with open(ca_cert_path) as f:
        server_ca = f.read()
    return {
        "hostname": netloc,
        "ip": server_ip,
        "server_ca": server_ca
    }


def _get_installed_suse_products():
    products= []
    for product_file in glob.glob("/etc/products.d/*.prod"):
        product_xml = ET.parse(product_file)
        if product_xml.find("./vendor").text == 'SUSE':
            product = {
                "name": product_xml.find("./name").text,
                "version": product_xml.find("./version").text,
                "arch": product_xml.find("./arch").text
            }
            products.append(product)
    return products

def load_instance_info():
    header_auth, hostname = _get_suse_cloud_info()

    rmt_host_data = _extract_rmt_server_info(hostname)
    credentials_data = _extract_http_auth(CREDENTIALS_NAME)
    products = _get_installed_suse_products()

    return { "products": products,
             "basic_auth": credentials_data,
             "header_auth": header_auth,
             "rmt_host": rmt_host_data}

def main():
    if not is_payg_instance():
        system_exit(1, ["instance is not pay-as-you-go"])

    payg_data = load_instance_info()
    print(json.dumps(payg_data))


if __name__ == '__main__':
    try:
        main()
        sys.exit(0)
    except KeyboardInterrupt:
        system_exit(9, ["User interrupted process."])
    except SystemExit as e:
        sys.exit(e.code)
    except Exception as e:
        system_exit(9, ["ERROR: {}".format(e)])

# Error codes
# 1- system is not a SUSE PAYG instance
# 2- error returning existing repositories
# 3- error when getting processed URL and Header from cloud plugin
# 4- unable to get ip for repository server
# 5- repository credentials file not found
# 6- CA file for cloud RMT server not found
# 9- generic error