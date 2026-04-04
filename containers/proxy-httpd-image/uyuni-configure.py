#!/usr/bin/python3
# pylint: disable=invalid-name,inconsistent-quotes
"""Configure script for Uyuni proxy httpd container."""

import logging
import os
import subprocess
import re
import yaml
import socket
import sys
import urllib.request
import json

from pathlib import Path
from typing import Tuple

config_path = "/etc/uyuni/"


def get_ips(fqdn: str) -> Tuple[str, str]:
    addrinfo = socket.getaddrinfo(fqdn, None)
    ipv4s = set(
        map(lambda r: r[4][0], filter(lambda f: f[0] == socket.AF_INET, addrinfo))
    )
    ipv6s = set(
        map(lambda r: r[4][0], filter(lambda f: f[0] == socket.AF_INET6, addrinfo))
    )
    ipv4, ipv6 = "", ""

    if len(ipv4s) == 0 and len(ipv6s) == 0:
        logging.critical("Cannot determine proxy IPv4 nor IPv6 from FQDN %s", fqdn)
        sys.exit(1)

    try:
        ipv4 = ipv4s.pop()
        if len(ipv4s) > 0:
            logging.warning(
                "Cannot determine unique IPv4 address for the proxy. TFTP sync may not work. Using IPv4 %s",
                ipv4,
            )
    except KeyError:
        logging.warning(
            "No IPv4 address detected for proxy. If this is single stack IPv6 setup this warning can be ignored"
        )

    try:
        ipv6 = ipv6s.pop()
        if len(ipv6s) > 0:
            logging.debug("Multiple IPv6 addresses resolved, using IPv6 %s", ipv6)
    except KeyError:
        logging.warning(
            "No IPv6 address detected for proxy. If this is single stack IPv4 setup this warning can be ignored"
        )

    logging.debug("Detected ips '%s', '%s' for fqdn %s", ipv4, ipv6, fqdn)
    return (ipv4, ipv6)


def get_server_version(fqdn):
    """
    Queries the server version from the API.
    """
    url = f"https://{fqdn}/rhn/manager/api/api/systemVersion"

    with urllib.request.urlopen(url, timeout=10) as response:
        body = response.read().decode("utf-8")
        data = json.loads(body)

        if "result" in data:
            return data["result"]
        else:
            raise KeyError(f"JSON missing 'result' field. Keys: {list(data.keys())}")


# read from files
with open(config_path + "config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    # log_level is the value for rhn.conf and should be a positive integer
    log_level = logging.WARNING if config.get("log_level") == 1 else logging.DEBUG
    logging.getLogger().setLevel(log_level)

with open(config_path + "httpd.yaml", encoding="utf-8") as httpdSource:
    httpdConfig = yaml.safe_load(httpdSource).get("httpd")

    # store the systemid content, but only if it does not exist already
    if not os.path.exists("/etc/sysconfig/rhn/systemid"):
        os.makedirs("/etc/sysconfig/rhn", exist_ok=True)
        with open("/etc/sysconfig/rhn/systemid", "w", encoding="utf-8") as file:
            file.write(httpdConfig.get("system_id"))

    # store SSL CA certificate, if it does not exist already
    if "ca_crt" in config and not os.path.exists(
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT"
    ):
        with open(
            "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "w", encoding="utf-8"
        ) as file:
            file.write(config.get("ca_crt"))
    # however always prepare link
    os.symlink(
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT",
        "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT",
    )
    os.system("/usr/sbin/update-ca-certificates")

    # store server certificate files, if cert does not exist already
    hasCert = all([key in httpdConfig for key in ["server_crt", "server_key"]])
    if hasCert and not os.path.exists("/etc/apache2/ssl.crt/server.crt"):
        with open("/etc/apache2/ssl.crt/server.crt", "w", encoding="utf-8") as file:
            file.write(httpdConfig.get("server_crt"))
        with open("/etc/apache2/ssl.key/server.key", "w", encoding="utf-8") as file:
            file.write(httpdConfig.get("server_key"))

    with open("/etc/apache2/httpd.conf", "r+", encoding="utf-8") as file:
        file_content = file.read()
        # make sure to send logs to stdout/stderr instead to file
        file_content = re.sub(r"ErrorLog .*", "ErrorLog /proc/self/fd/2", file_content)
        # writing back the content
        file.seek(0, 0)
        file.write(file_content)
        file.truncate()

    server_version = get_server_version(config["server"])
    # Only check version for SUSE Multi-Linux Manager, not Uyuni
    matcher = re.fullmatch(r"([0-9]+\.)[0-9]+\.[0-9]+", server_version)
    if matcher:
        major_version = matcher.group(1)
        container_version = subprocess.run(
            ["rpm", "-q", "--queryformat", "%{version}", "spacewalk-proxy-common"],
            stdout=subprocess.PIPE,
            universal_newlines=True,
            check=False,
        ).stdout
        if not container_version.startswith(major_version):
            logging.critical(
                "Proxy container image version (%s) doesn't match server major version (%s)",
                container_version,
                major_version,
            )
            sys.exit(1)

    # resolve needed IP addresses
    server_ipv4, server_ipv6 = get_ips(config["server"])
    proxy_ipv4, proxy_ipv6 = get_ips(config["proxy_fqdn"])

    # Create conf file
    with open("/etc/rhn/rhn.conf", "w", encoding="utf-8") as file:
        file.write(f"""# Automatically generated Uyuni Proxy Server configuration file.
        # -------------------------------------------------------------------------
        
        # Debug log level
        debug = {config.get("log_level", 1)}
        
        # Logs redirect
        proxy.broker.log_file = stdout
        proxy.redirect.log_file = stdout

        # SSL CA certificate location
        proxy.ca_chain = /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
        
        # Corporate HTTP proxy, format: corp_gateway.example.com:8080
        proxy.http_proxy = 
        
        # Username for that corporate HTTP proxy
        proxy.http_proxy_username = 
        
        # Password for that corporate HTTP proxy
        proxy.http_proxy_password = 
        
        # Location of locally built, custom packages
        proxy.pkg_dir = /var/spool/rhn-proxy
        
        # Hostname of Uyuni, SUSE Multi-Linux Manager Server or another proxy
        proxy.rhn_parent = {config['server']}
        proxy.proxy_fqdn = {config['proxy_fqdn']}
        
        # Destination of all tracebacks, etc.
        traceback_mail = {config['email']}
        """)

        if "timeout" in config:
            file.write(f"proxy.timeout = {config['timeout']}")

    with open(
        "/etc/apache2/conf.d/smlm-proxy-forwards.conf", "r+", encoding="utf-8"
    ) as smlm_conf:
        file_content = smlm_conf.read()
        file_content = re.sub(r"{{ SERVER }}", config["server"], file_content)
        smlm_conf.seek(0, 0)
        smlm_conf.write(file_content)
        smlm_conf.truncate()

    with open("/etc/apache2/vhosts.d/ssl.conf", "w", encoding="utf-8") as file:
        file.write(f"""
<IfDefine SSL>
<IfDefine !NOSSL>
<VirtualHost _default_:443>

	DocumentRoot "/srv/www/htdocs"
	ServerName {config["proxy_fqdn"]}

	ErrorLog /proc/self/fd/2
	TransferLog /proc/self/fd/1
	CustomLog /proc/self/fd/1   ssl_combined

	SSLEngine on
	SSLUseStapling  on

    SSLCertificateFile /etc/apache2/ssl.crt/server.crt
    SSLCertificateKeyFile /etc/apache2/ssl.key/server.key

    SSLProtocol all -SSLv2 -SSLv3
    RewriteEngine on
    RewriteOptions inherit
    SSLProxyEngine on
</VirtualHost>
</IfDefine>
</IfDefine>
""")


# Make sure permissions are set as desired
os.system("/usr/bin/chown root:www /etc/rhn/rhn.conf")
os.system("/usr/bin/chmod 640 /etc/rhn/rhn.conf")

os.system("/usr/bin/chown -R wwwrun:www /var/spool/rhn-proxy")
os.system("/usr/bin/chmod -R 750 /var/spool/rhn-proxy")
if not os.path.exists("/var/cache/rhn/proxy-auth"):
    os.makedirs("/var/cache/rhn/proxy-auth")
os.system("/usr/bin/chown -R wwwrun:root /var/cache/rhn/proxy-auth")
os.system("/usr/bin/chown -R wwwrun:root /srv/tftpboot")
os.system("/usr/bin/chmod 755 /srv/tftpboot")

# Invalidate (remove) possible old proxy auth cache files, based on sha1
# after migration to sha256 proxy auth cache files.
#
# The old sha1 based cache files are like (filename length = 51):
#    p10000100040c488b45d72291a0da497f5101d47e274c6b63ac
#
# The new sha256 based cache files are like (filename length = 75):
#    p1000010004e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
#
for cache_file in Path("/var/cache/rhn/proxy-auth").iterdir():
    if cache_file.is_file() and len(cache_file.name) == 51:
        cache_file.unlink()
