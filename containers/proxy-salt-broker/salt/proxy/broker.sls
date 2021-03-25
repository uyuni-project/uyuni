{% set machine_id = salt['environ.get']('UYUNI_MACHINE_ID') %}
{% set master = salt['environ.get']('UYUNI_MASTER') %}
{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}
{% set email = salt['environ.get']('UYUNI_EMAIL', 'root@localhost') %}

cont_set_fqdn:
  module.run:
    - name: hosts.add_host
    - ip: {{ salt['grains.get']('ip4_interfaces:eth0').pop() }}
    - alias: {{ minion_id }}

cont_setup_machine_id:
  file.managed:
    - name: /etc/machine-id
    - contents: {{ machine_id }}

cont_setup_minion_id:
  file.managed:
    - name: /etc/salt/minion_id
    - contents: {{ minion_id }}

cont_rhn_conf:
  file.managed:
    - name: /etc/rhn/rhn.conf
    - mode: 640
    - user: root
    - group: www
    - contents: |
        # Automatically generated Spacewalk Proxy Server configuration file.
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
        proxy.rhn_parent = {{ master }}
        
        # Destination of all tracebacks, etc.
        traceback_mail = {{ email }}

