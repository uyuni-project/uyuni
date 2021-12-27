#!/usr/bin/python3

import base64
import os
import re
import yaml

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)
    
    # Save the systemid file
    with open("/etc/sysconfig/rhn/systemid", "w") as file:
        file.write(base64.b64decode(config['binary_system_id']).decode())
    
    # Decode and install SSL CA certificate
    with open("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "w") as file:
        file.write(base64.b64decode(config['binary_ca_certs']).decode())
    os.symlink("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT")
    os.system("/usr/sbin/update-ca-certificates")

    # Decode and install server certificate files
    with open("/etc/apache2/ssl.crt/server.crt", "w") as file:
        file.write(base64.b64decode(config['binary_server_crt']).decode())
    with open("/etc/apache2/ssl.csr/server.csr", "w") as file:
        file.write(base64.b64decode(config['binary_server_csr']).decode())
    with open("/etc/apache2/ssl.key/server.key", "w") as file:
        file.write(base64.b64decode(config['binary_server_key']).decode())

    with open("/etc/apache2/httpd.conf", "r+") as file:
        file_content = file.read()
        # make sure to send logs to stdout/stderr instead to file
        file_content = re.sub(r"ErrorLog .*", "ErrorLog /proc/self/fd/2", file_content)
        # writing back the content
        file.seek(0,0)
        file.write(file_content)
        file.truncate()

    # Create conf file
    with open("/etc/rhn/rhn.conf", "w") as file:
        file.write(f'''# Automatically generated Spacewalk Proxy Server configuration file.
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
        
        # Hostname of RHN Classic Server or Red Hat Satellite
        proxy.rhn_parent = {config['server']}
        
        # Destination of all tracebacks, etc.
        traceback_mail = {config['email']}''')
    os.system('chown root:www /etc/rhn/rhn.conf')
    os.system('chmod 640 /etc/rhn/rhn.conf')

# Make sure permissions are set as desired
os.system('chown -R root:root /srv/www/htdocs/pub')
os.system('chmod -R 755 /srv/www/htdocs/pub')
os.system('chown -R wwwrun:www /var/spool/rhn-proxy')
os.system('chmod -R 750 /var/spool/rhn-proxy')
os.system('chown -R wwwrun:root /var/cache/rhn/proxy-auth')
