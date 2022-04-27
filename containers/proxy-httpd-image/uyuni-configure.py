#!/usr/bin/python3

import os
import subprocess
import re
import shutil
import yaml
import socket
import sys

from typing import Tuple

config_path = "/etc/uyuni/"

def getIPs(fqdn: str) -> Tuple[str, str]:
    addrinfo = socket.getaddrinfo(fqdn, None)
    ipv4s = set(map(lambda r: r[4][0], filter(lambda f: f[0] == socket.AF_INET, addrinfo)))
    ipv6s = set(map(lambda r: r[4][0], filter(lambda f: f[0] == socket.AF_INET6, addrinfo)))
    ipv4, ipv6 = "", ""

    if len(ipv4s) == 0 and len(ipv6s) == 0:
       print("FATAL: Cannot determine proxy IPv4 nor IPv6 from FQDN {}".format(fqdn))
       sys.exit(1)

    try:
       ipv4 = ipv4s.pop()
       if len(ipv4s) > 0:
          print("WARNING: Cannot determine unique IPv4 address for the proxy. TFTP sync may not work. Using IPv4 {}".format(ipv4))
    except KeyError:
       print("WARNING: No IPv4 address detected for proxy. If this is single stack IPv6 setup this warning can be ignored")

    try:
       ipv6 = ipv6s.pop()
       if len(ipv6s) > 0:
          print("Multiple IPv6 addresses resolved, using IPv6 {}".format(ipv6))
    except KeyError:
       print("WARNING: No IPv6 address detected for proxy. If this is single stack IPv4 setup this warning can be ignored")

    print(f"DEBUG: detected ips '{ipv4}', '{ipv6}' for fqdn {fqdn}")
    return (ipv4, ipv6)

# read from files
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)

with open(config_path + "httpd.yaml") as httpdSource:
    httpdConfig = yaml.safe_load(httpdSource)
   
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
    
    # store the systemid content
    with open("/etc/sysconfig/rhn/systemid", "w") as file:
        file.write(httpdConfig.get("system_id"))
    
    # store SSL CA certificate
    with open("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "w") as file:
        file.write(httpdConfig.get("ca_crt"))
    os.symlink("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT")
    os.system("/usr/sbin/update-ca-certificates")

    # store server certificate files
    with open("/etc/apache2/ssl.crt/server.crt", "w") as file:
        file.write(httpdConfig.get("server_crt"))
    with open("/etc/apache2/ssl.key/server.key", "w") as file:
        file.write(httpdConfig.get("server_key"))

    with open("/etc/apache2/httpd.conf", "r+") as file:
        file_content = file.read()
        # make sure to send logs to stdout/stderr instead to file
        file_content = re.sub(r"ErrorLog .*", "ErrorLog /proc/self/fd/2", file_content)
        # writing back the content
        file.seek(0,0)
        file.write(file_content)
        file.truncate()

    # resolve needed IP addresses
    serverIPv4, serverIPv6 = getIPs(config['server'])
    proxyIPv4, proxyIPv6 = getIPs(config['proxy_fqdn'])

    # Create conf file
    with open("/etc/rhn/rhn.conf", "w") as file:
        file.write(f'''# Automatically generated Uyuni Proxy Server configuration file.
        # -------------------------------------------------------------------------

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
        
        # Destination of all tracebacks, etc.
        traceback_mail = {config['email']}

        # Tftp sync configuration
        tftpsync.server_fqdn = {config['server']}
        tftpsync.server_ip = {serverIPv4}
        tftpsync.server_ip6 = {serverIPv6}
        tftpsync.proxy_ip = {proxyIPv4}
        tftpsync.proxy_ip6 = {proxyIPv6}
        tftpsync.proxy_fqdn = {config['proxy_fqdn']}
        tftpsync.tftpboot = /srv/tftpboot''')

    with open("/etc/apache2/conf.d/susemanager-tftpsync-recv.conf", "w") as file:
        requireIPv4 = ""
        requireIPv6 = ""
        if len(proxyIPv4) > 0:
           requireIPv4 = "Require ip {}".format(proxyIPv4)
        if len(proxyIPv6) > 0:
           requireIPv6 = "Require ip {}".format(proxyIPv6)
        file.write(f'''<Directory "/srv/www/tftpsync">
    <RequireAny>
        {requireIPv4}
        {requireIPv6}
    </RequireAny>
</Directory>

WSGIScriptAlias /tftpsync/add /srv/www/tftpsync/add
WSGIScriptAlias /tftpsync/delete /srv/www/tftpsync/delete''')

    with open("/etc/apache2/conf.d/saltboot.conf", "w") as file:
        # Saltboot uses the same URL regardles containerized or normal proxy
        # here we rewrite URL so upstream server understands it

        # First condition/rule removes double arch rule for backward compatility
        # second changes /saltboot/image/..?orgid=<org> to /os-images/<org>/
        file.write('''
RewriteEngine on
RewriteCond %{REQUEST_URI} ^/saltboot/(image|boot).*x86_64.*x86_64
RewriteRule ^(.*)\.x86_64(.*\.x86_64-.*) $1$2 [N]

RewriteCond %{QUERY_STRING} orgid=(\d+)
RewriteRule "^/saltboot/(image|boot)(.+)$" "/os-images/%1$2"  [R,L,QSD]
''')

    with open("/etc/apache2/vhosts.d/ssl.conf", "w") as file:
        file.write(f'''
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
''')

    os.system('chown root:www /etc/rhn/rhn.conf')
    os.system('chmod 640 /etc/rhn/rhn.conf')

# Make sure permissions are set as desired
os.system('chown -R root:root /srv/www/htdocs/pub')
os.system('chmod -R 755 /srv/www/htdocs/pub')
os.system('chown -R wwwrun:www /var/spool/rhn-proxy')
os.system('chmod -R 750 /var/spool/rhn-proxy')
if not os.path.exists('/var/cache/rhn/proxy-auth'):
    os.makedirs('/var/cache/rhn/proxy-auth')
os.system('chown -R wwwrun:root /var/cache/rhn/proxy-auth')
os.system('chown -R wwwrun:root /srv/tftpboot')
os.system('chmod 755 /srv/tftpboot')
