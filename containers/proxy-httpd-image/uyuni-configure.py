#!/usr/bin/python3
# pylint: disable=invalid-name
"""Configure script for Uyuni proxy httpd container."""

import logging
import os
import subprocess
import re
import yaml
import socket
import sys

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


def insert_under_line(file_path, line_to_match, line_to_insert):
    # add 4 leading spaces and a new line in the end
    line_to_insert = line_to_insert.rjust(len(line_to_insert) + 4) + "\n"

    with open(file_path, "r", encoding="utf-8") as f:
        contents = f.readlines()

    index = -1
    for ind, line in enumerate(contents):
        if line_to_match in line:
            index = ind + 1

    contents.insert(index, line_to_insert)

    with open(file_path, "w", encoding="utf-8") as f:
        contents = "".join(contents)
        f.write(contents)


# read from files
with open(config_path + "config.yaml", encoding="utf-8") as source:
    config = yaml.safe_load(source)

    # log_level is the value for rhn.conf and should be a positive integer
    log_level = logging.WARNING if config.get("log_level") == 1 else logging.DEBUG
    logging.getLogger().setLevel(log_level)

with open(config_path + "httpd.yaml", encoding="utf-8") as httpdSource:
    httpdConfig = yaml.safe_load(httpdSource).get("httpd")

    server_version = config.get("server_version")
    # Only check version for SUSE Manager, not Uyuni
    matcher = re.fullmatch(r"([0-9]+\.[0-9]+\.)[0-9]+", server_version)
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

    # store the systemid content
    with open("/etc/sysconfig/rhn/systemid", "w", encoding="utf-8") as file:
        file.write(httpdConfig.get("system_id"))

    # store SSL CA certificate
    with open(
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "w", encoding="utf-8"
    ) as file:
        file.write(config.get("ca_crt"))
    os.symlink(
        "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT",
        "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT",
    )
    os.system("/usr/sbin/update-ca-certificates")

    # store server certificate files
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

    # resolve needed IP addresses
    server_ipv4, server_ipv6 = get_ips(config["server"])
    proxy_ipv4, proxy_ipv6 = get_ips(config["proxy_fqdn"])

    # Create conf file
    with open("/etc/rhn/rhn.conf", "w", encoding="utf-8") as file:
        file.write(
            f"""# Automatically generated Uyuni Proxy Server configuration file.
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
        
        # Hostname of Uyuni, SUSE Manager Server or another proxy
        proxy.rhn_parent = {config['server']}
        proxy.proxy_fqdn = {config['proxy_fqdn']}
        
        # Destination of all tracebacks, etc.
        traceback_mail = {config['email']}

        # Tftp sync configuration
        tftpsync.server_fqdn = {config['server']}
        tftpsync.server_ip = {server_ipv4}
        tftpsync.server_ip6 = {server_ipv6}
        tftpsync.proxy_ip = {proxy_ipv4}
        tftpsync.proxy_ip6 = {proxy_ipv6}
        tftpsync.proxy_fqdn = {config['proxy_fqdn']}
        tftpsync.tftpboot = /srv/tftpboot"""
        )

    with open(
        "/etc/apache2/conf.d/susemanager-tftpsync-recv.conf", "w", encoding="utf-8"
    ) as file:
        require_ipv4 = ""
        require_ipv6 = ""
        if len(proxy_ipv4) > 0:
            require_ipv4 = f"Require ip {proxy_ipv4}"
        if len(proxy_ipv6) > 0:
            require_ipv6 = f"Require ip {proxy_ipv6}"
        file.write(
            f"""<Directory "/srv/www/tftpsync">
    <RequireAny>
        {require_ipv4}
        {require_ipv6}
    </RequireAny>
</Directory>

WSGIScriptAlias /tftpsync/add /srv/www/tftpsync/add
WSGIScriptAlias /tftpsync/delete /srv/www/tftpsync/delete"""
        )

    with open("/etc/apache2/conf.d/cobbler-proxy.conf", "w", encoding="utf-8") as file:
        file.write(
            f"""ProxyPass /cobbler_api https://{config['server']}/download/cobbler_api
ProxyPassReverse /cobbler_api https://{config['server']}/download/cobbler_api
RewriteRule ^/cblr/svc/op/ks/(.*)$ /download/$0 [R,L]
RewriteRule ^/cblr/svc/op/autoinstall/(.*)$ /download/$0 [R,L]
ProxyPass /cblr https://{config['server']}/cblr
ProxyPassReverse /cblr https://{config['server']}/cblr
ProxyPass /cobbler https://{config['server']}/cobbler
ProxyPassReverse /cobbler https://{config['server']}/cobbler
        """
        )

    with open(
        "/etc/apache2/conf.d/susemanager-pub.conf", "w", encoding="utf-8"
    ) as file:
        file.write("WSGIScriptAlias /pub /usr/share/rhn/wsgi/xmlrpc.py")

    with open("/etc/apache2/vhosts.d/ssl.conf", "w", encoding="utf-8") as file:
        file.write(
            f"""
<IfDefine SSL>
<IfDefine !NOSSL>
<VirtualHost _default_:443>

	DocumentRoot "/srv/www/htdocs"
	ServerName {config['proxy_fqdn']}

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
"""
        )

    # Adjust logs format in apache httpd:
    # Modify the other configurations so that the var HANDLER_TYPE gets set based on a directory of a script executed
    insert_under_line(
        "/etc/apache2/conf.d/spacewalk-proxy-wsgi.conf",
        "<Directory /usr/share/rhn>",
        'SetEnv HANDLER_TYPE "proxy-broker"',
    )
    insert_under_line(
        "/etc/apache2/conf.d/spacewalk-proxy.conf",
        '<Directory "/srv/www/htdocs/pub/*">',
        'SetEnv HANDLER_TYPE "proxy-html"',
    )
    insert_under_line(
        "/etc/apache2/conf.d/spacewalk-proxy.conf",
        '<Directory "/srv/www/htdocs/docs/*">',
        'SetEnv HANDLER_TYPE "proxy-docs"',
    )

    # redirect /saltboot to the server
    insert_under_line(
        "/etc/apache2/conf.d/spacewalk-proxy-wsgi.conf",
        "WSGIScriptAlias /tftp /usr/share/rhn/wsgi/xmlrpc.py",
        "WSGIScriptAlias /saltboot /usr/share/rhn/wsgi/xmlrpc.py",
    )

    os.system("chown root:www /etc/rhn/rhn.conf")
    os.system("chmod 640 /etc/rhn/rhn.conf")

# Make sure permissions are set as desired
os.system("chown -R wwwrun:www /var/spool/rhn-proxy")
os.system("chmod -R 750 /var/spool/rhn-proxy")
if not os.path.exists("/var/cache/rhn/proxy-auth"):
    os.makedirs("/var/cache/rhn/proxy-auth")
os.system("chown -R wwwrun:root /var/cache/rhn/proxy-auth")
os.system("chown -R wwwrun:root /srv/tftpboot")
os.system("chmod 755 /srv/tftpboot")
