#!/usr/bin/python3

import os
import yaml

config_path = "/etc/uyuni/"

# read from file
with open(config_path + "config.yaml") as source:
    config = yaml.safe_load(source)
    
    # Save the systemid file
    with open("/etc/sysconfig/rhn/systemid", "w") as file:
        file.write(str(config['system_id']))
    
    # Decode and install SSL CA certificate
    os.system(f"echo -n '{config['binary_ca_certs']}' | base64 -d > /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT")
    os.symlink("/etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT", "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT")
    os.system("/usr/sbin/update-ca-certificates")

    # Decode and install server certificate package
    os.system(f"echo -n '{config['binary_srv_cert_rpm']}' | base64 -d > /root/rhn-org-httpd-ssl-key-pair-proxy-1.0-3.noarch.rpm")
    os.system('rpm -Uv /root/rhn-org-httpd-ssl-key-pair-proxy-1.0-3.noarch.rpm')

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

