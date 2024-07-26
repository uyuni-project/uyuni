#! /usr/bin/python3
#  pylint: disable=missing-module-docstring, invalid-name
#
# Copyright (c) 2024 SUSE LLC
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
import time
import xml.etree.ElementTree as ET
from urllib.parse import urlparse
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
    flavor_check = "/usr/bin/instance-flavor-check"
    if not os.path.isfile(flavor_check) or not os.access(flavor_check, os.X_OK):
        print(
            "instance-flavor-check tool is not available. "
            + "For a correct PAYG detection please install the 'python-instance-billing-flavor-check' package",
            file=sys.stderr,
        )
        return False

    try:
        result = subprocess.call(
            flavor_check, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL
        )
    except subprocess.CalledProcessError as e:
        print(
            f"Failed to execute instance-flavor-check tool. {e}",
            file=sys.stderr,
        )
        return False

    # instance-flavor-check return 10 for PAYG. Other possible values are 11 -> BYOS and 12 -> Unknown
    return result == 10


SuseCloudInfo = namedtuple("SuseCloudInfo", ["header_auth", "hostname"])


def _get_suse_cloud_info():
    inputTmpl = INPUT_TEMPLATE % (CREDENTIALS_NAME, "/")
    try:
        auth_data_output = subprocess.check_output(
            "/usr/lib/zypp/plugins/urlresolver/susecloud",
            input=inputTmpl,
            stderr=subprocess.PIPE,
            universal_newlines=True,
        )
    except subprocess.CalledProcessError as e:
        system_exit(
            3,
            [f"Got error when getting repo processed URL and headers(error {e}):"],
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
        system_exit(5, [f"Credentials file not found ({credentials_file})"])
    with open(credentials_file, encoding="utf-8") as credFile:
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
        system_exit(4, [f"Unable to get IP for repository server (error {e}):"])

    server_ip = host_ip_output.split(" ", maxsplit=1)[0].strip()
    server_ip_uds = server_ip.replace(".", "_")
    ca_cert_path = f"/etc/pki/trust/anchors/registration_server_{server_ip_uds}.pem"

    if not Path(ca_cert_path).exists():
        ca_cert_path = (
            f"/usr/share/pki/trust/anchors/registration_server_{server_ip_uds}.pem"
        )

        if not Path(ca_cert_path).exists():
            system_exit(
                6,
                [
                    f"CA file for server {server_ip} not found (location '/etc/pki/trust/anchors/' or '/usr/share/pki/trust/anchors/')"
                ],
            )
    with open(ca_cert_path, encoding="utf-8") as f:
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
        "timestamp": int(time.time()),
    }


def check_billing_adapter_status():
    cnf = "/var/lib/csp-billing-adapter/csp-config.json"
    if os.path.exists(cnf):
        try:
            with open(cnf, encoding="utf-8") as f:
                cspConfig = json.load(f)
                if (
                    not cspConfig["billing_api_access_ok"]
                    and len(cspConfig["errors"]) > 0
                ):
                    errstr = "\n  ".join(cspConfig["errors"])
                    print(
                        f"CPS Billing Adapter reported errors:\n  {errstr}",
                        file=sys.stderr,
                    )
                return cspConfig["billing_api_access_ok"]
        except json.JSONDecodeError as e:
            print(
                f"Unable to read CSP Billing Adapter status file: {e}", file=sys.stderr
            )

    return False


def perform_compliants_checks():
    # defaults
    cloudProvider = "None"
    modified_packages = False
    billing_service_running = False
    billing_status = False
    has_metering_access = False
    is_payg = is_payg_instance()

    if is_payg:
        if os.path.isfile("/usr/bin/ec2metadata"):
            cloudProvider = "AWS"
        elif os.path.isfile("/usr/bin/azuremetadata"):
            cloudProvider = "AZURE"
        elif os.path.isfile("/usr/bin/gcemetadata"):
            cloudProvider = "GCE"

        check_pkgs = [
            "python-instance-billing-flavor-check",
            "csp-billing-adapter-service",
            "python3-csp-billing-adapter-local",
        ]
        if cloudProvider == "AWS":
            check_pkgs.append("python3-csp-billing-adapter-amazon")
        elif cloudProvider == "AZURE":
            check_pkgs.append("python3-csp-billing-adapter-microsoft")

        modified_packages = any(has_package_modifications(pkg) for pkg in check_pkgs)

        billing_service_running = is_service_running("csp-billing-adapter.service")

        if billing_service_running:
            billing_status = check_billing_adapter_status()

        has_metering_access = can_access_metering()

    compliant = (
        has_metering_access
        and billing_service_running
        and billing_status
        and not modified_packages
    )

    return {
        "isPaygInstance": is_payg,
        "compliant": compliant,
        "cloudProvider": cloudProvider,
        "hasModifiedPackages": modified_packages,
        "billingServiceRunning": billing_service_running,
        "billingServiceStatus": billing_status,
        "hasMeteringAccess": has_metering_access,
        "timestamp": int(time.time()),
    }


def can_access_metering():
    p = subprocess.run(
        [
            "curl",
            "--fail",
            "--no-progress-meter",
            "--connect-timeout",
            "3",
            "--max-time",
            "5",
            "http://localhost:18888/metering",
        ],
        stdout=subprocess.DEVNULL,
        stderr=subprocess.PIPE,
        universal_newlines=True,
        encoding="utf-8",
        check=False,
    )
    if p.returncode != 0:
        print(
            f"Failed to get metering information: {p.stderr}",
            file=sys.stderr,
        )
        return False
    return True


def is_service_running(service):
    try:
        result = subprocess.call(
            ["/usr/bin/systemctl", "-q", "is-active", service],
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
    except subprocess.CalledProcessError as e:
        print(
            f"Checking for running service {service} failed: {e}",
            file=sys.stderr,
        )
        return False
    return result == 0


def has_package_modifications(pkg):
    p = subprocess.run(
        ["rpm", "-V", pkg],
        stdout=subprocess.PIPE,
        universal_newlines=True,
        encoding="utf-8",
        check=False,
    )

    for line in p.stdout.split("\n"):
        if "is not installed" in line:
            return True
        if len(line) < 3 or line.endswith(".pyc"):
            continue
        if line[2] == "5":
            return True

    return False


def get_volume_path(volume):
    path = None
    try:
        out = subprocess.check_output(
            ["podman", "volume", "inspect", volume],
            stderr=subprocess.PIPE,
            universal_newlines=True,
            encoding="utf-8",
        )
    except subprocess.CalledProcessError as e:
        system_exit(1, [f"Unable to find volume: {e}"])
    volumes = json.loads(out)
    for v in volumes:
        if v["Name"] == volume and v["Driver"] == "local" and v["Mountpoint"]:
            path = v["Mountpoint"]
            break
    return path


def main():
    volume_path = get_volume_path("var-cache")
    if not os.path.isdir(os.path.join(volume_path, "rhn")):
        system_exit(1, ["Container not yet initialized"])

    compliance_data = perform_compliants_checks()
    with open(
        os.path.join(volume_path, "rhn", "payg_compliance.json"), "w", encoding="utf-8"
    ) as f:
        json.dump(compliance_data, f, ensure_ascii=False, indent=2)
    os.chmod(os.path.join(volume_path, "rhn", "payg_compliance.json"), 0o644)

    if not is_payg_instance():
        if os.path.isfile(os.path.join(volume_path, "rhn", "payg.json")):
            os.remove(os.path.join(volume_path, "rhn", "payg.json"))
        return

    payg_data = load_instance_info()
    with open(
        os.path.join(volume_path, "rhn", "payg.json"), "w", encoding="utf-8"
    ) as f:
        json.dump(payg_data, f, ensure_ascii=False, indent=2)
    os.chmod(os.path.join(volume_path, "rhn", "payg.json"), 0o644)


if __name__ == "__main__":
    try:
        main()
        sys.exit(0)
    except KeyboardInterrupt:
        system_exit(9, ["User interrupted process."])
    except SystemExit as ex:
        sys.exit(ex.code)
    #  pylint: disable=broad-exception-caught
    except Exception as ex:
        system_exit(9, [f"ERROR: {ex}"])

# Error codes
# 1- error detecting volumes
# 2- error returning existing repositories
# 3- error when getting processed URL and Header from cloud plugin
# 4- unable to get ip for repository server
# 5- repository credentials file not found
# 6- CA file for cloud RMT server not found
# 9- generic error
