# pylint: disable=missing-module-docstring
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

# pylint: disable-next=unused-import
import csv
import subprocess
import xml.etree.ElementTree as ET

# pylint: disable-next=unused-import
from urllib.parse import urlparse, parse_qs
import json
import sys
from pathlib import Path
import glob
import os

# pylint: disable-next=unused-import
import platform
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
    flavor_check = "/usr/bin/instance-flavor-check"
    if not os.path.isfile(flavor_check) or not os.access(flavor_check, os.X_OK):
        system_exit(
            1,
            [
                "instance-flavor-check tool is not available.",
                "For a correct PAYG detection please install the 'python-instance-billing-flavor-check' package",
            ],
        )

    try:
        result = subprocess.call(
            flavor_check, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
        )
    except subprocess.CalledProcessError as e:
        system_exit(1, ["Failed to execute instance-flavor-check tool.", e])

    # instance-flavor-check return 10 for PAYG. Other possible values are 11 -> BYOS and 12 -> Unknown
    return result == 10


SuseCloudInfo = namedtuple("SuseCloudInfo", ["header_auth", "hostname"])


def _get_suse_cloud_info():
    # pylint: disable-next=redefined-builtin
    input = INPUT_TEMPLATE % (CREDENTIALS_NAME, "/")
    try:
        auth_data_output = subprocess.check_output(
            "/usr/lib/zypp/plugins/urlresolver/susecloud",
            input=input,
            stderr=subprocess.PIPE,
            universal_newlines=True,
        )
    except subprocess.CalledProcessError as e:
        system_exit(
            3,
            [
                # pylint: disable-next=consider-using-f-string
                "Got error when getting repo processed URL and headers(error {}):".format(
                    e
                )
            ],
        )

    full_output = auth_data_output.split("\n")
    _, header_auth, _, repository_url = full_output
    repository_url_parsed = urlparse(repository_url)
    k, v = header_auth.split(":", 1)

    headers = _get_instance_identification()
    headers[k] = v

    return SuseCloudInfo(headers, repository_url_parsed.netloc)


def _get_instance_identification():
    product_xml = ET.parse("/etc/products.d/baseproduct")
    if product_xml.find("./vendor").text == "SUSE":
        return {
            "X-Instance-Identifier": product_xml.find("./name").text,
            "X-Instance-Version": product_xml.find("./version").text,
            "X-Instance-Arch": product_xml.find("./arch").text,
        }

    return {}


def _extract_http_auth(credentials):
    credentials_file = "/etc/zypp/credentials.d/" + credentials
    if not Path(credentials_file).exists():
        # pylint: disable-next=consider-using-f-string
        system_exit(5, ["Credentials file not found ({})".format(credentials_file)])
    # pylint: disable-next=unspecified-encoding,invalid-name
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
        host_ip_output = subprocess.check_output(
            ["getent", "hosts", netloc], stderr=subprocess.PIPE, universal_newlines=True
        )
    except subprocess.CalledProcessError as e:
        # pylint: disable-next=consider-using-f-string
        system_exit(4, ["unable to get ip for repository server (error {}):".format(e)])

    # pylint: disable-next=use-maxsplit-arg
    server_ip = host_ip_output.split(" ")[0].strip()
    ca_cert_path = (
        # pylint: disable-next=consider-using-f-string
        "/etc/pki/trust/anchors/registration_server_%s.pem"
        % server_ip.replace(".", "_")
    )
    if not Path(ca_cert_path).exists():
        ca_cert_path = (
            # pylint: disable-next=consider-using-f-string
            "/usr/share/pki/trust/anchors/registration_server_%s.pem"
            % server_ip.replace(".", "_")
        )
        if not Path(ca_cert_path).exists():
            system_exit(
                6,
                [
                    # pylint: disable-next=consider-using-f-string
                    "CA file for server {} not found (location '/etc/pki/trust/anchors/' or '/usr/share/pki/trust/anchors/')".format(
                        server_ip
                    )
                ],
            )
    # pylint: disable-next=unspecified-encoding
    with open(ca_cert_path) as f:
        server_ca = f.read()
    return {"hostname": netloc, "ip": server_ip, "server_ca": server_ca}


def _get_installed_suse_products():
    products = []
    for product_file in glob.glob("/etc/products.d/*.prod"):
        product_xml = ET.parse(product_file)
        if product_xml.find("./vendor").text == "SUSE":
            product = {
                "name": product_xml.find("./name").text,
                "version": product_xml.find("./version").text,
                "arch": product_xml.find("./arch").text,
            }
            if product["name"] == "sle-manager-tools":
                # no payg product has manager tools. When it appears, it comes from
                # a registration against SUSE Manager
                continue
            products.append(product)
    return products


def load_instance_info():
    header_auth, hostname = _get_suse_cloud_info()

    rmt_host_data = _extract_rmt_server_info(hostname)
    credentials_data = _extract_http_auth(CREDENTIALS_NAME)
    products = _get_installed_suse_products()

    return {
        "type": "CLOUDRMT",
        "products": products,
        "basic_auth": credentials_data,
        "header_auth": header_auth,
        "rmt_host": rmt_host_data,
    }


def main():
    if not is_payg_instance():
        system_exit(1, ["instance is not PAYG"])

    payg_data = load_instance_info()
    print(json.dumps(payg_data))


if __name__ == "__main__":
    try:
        main()
        sys.exit(0)
    except KeyboardInterrupt:
        system_exit(9, ["User interrupted process."])
    except SystemExit as e:
        sys.exit(e.code)
    # pylint: disable-next=broad-exception-caught
    except Exception as e:
        # pylint: disable-next=consider-using-f-string
        system_exit(9, ["ERROR: {}".format(e)])

# Error codes
# 1- system is not a SUSE PAYG instance
# 2- error returning existing repositories
# 3- error when getting processed URL and Header from cloud plugin
# 4- unable to get ip for repository server
# 5- repository credentials file not found
# 6- CA file for cloud RMT server not found
# 9- generic error
