#!/usr/bin/python3

import os
import re
import shutil
import yaml

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)
    
    # copy the systemid file
    shutil.copyfile(config_path + "system_id.xml", "/etc/sysconfig/rhn/systemid")
    
    # copy SSL CA certificate
    shutil.copyfile(config_path + "ca.crt", "/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT")
    os.symlink("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT")
    os.system("/usr/sbin/update-ca-certificates")

    # copy server certificate files
    shutil.copyfile(config_path + "server.crt", "/etc/apache2/ssl.crt/server.crt")
    shutil.copyfile(config_path + "server.key", "/etc/apache2/ssl.key/server.key")

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
