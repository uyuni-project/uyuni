{% set machine_id = salt['environ.get']('UYUNI_MACHINE_ID') %}
{% set master = salt['environ.get']('UYUNI_MASTER') %}
{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}
{% set activation_key = salt['environ.get']('UYUNI_ACTIVATION_KEY') %}
{% set ca_certs = salt['environ.get']('UYUNI_CA_CERTS') %}
{% set srv_cert_rpm = salt['environ.get']('UYUNI_SRV_CERT') %}
{% set email = salt['environ.get']('UYUNI_EMAIL', 'root@localhost') %}
{% set system_id = '/etc/sysconfig/rhn/systemid' %}

cont_check_ca_certs:
  file.exists:
    - name: {{ ca_certs }}

cont_check_srv_cert:
  file.exists:
    - name: {{ srv_cert_rpm }}

cont_setup_machine_id:
  file.managed:
    - name: /etc/machine-id
    - contents: {{ machine_id }}

cont_setup_minion_id:
  file.managed:
    - name: /etc/salt/minion_id
    - contents: {{ minion_id }}

cont_minion_conf:
  file.managed:
    - name: /etc/salt/minion.d/master.conf
    - template: jinja
    - mode: 644
    - contents: |
        master: {{ master }}
        server_id_use_crc: adler32
        enable_legacy_startup_events: False
        enable_fqdns_grains: False
        {% if activation_key %}
        grains:
          susemanager:
            activation_key: {{ activation_key }}
        {% endif %}
        start_event_grains:
          - machine_id
          - saltboot_initrd
          - susemanager
        system-environment:
          modules:
            pkg:
              _:
                SALT_RUNNING: 1

cont_copy_salt_minion_priv_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion.pem
    - source: /config/salt/pki/minion/minion.pem
    - mode: 400
    - onlyif: test -f /config/salt/pki/minion/minion.pem

cont_copy_salt_minion_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion.pub
    - source: /config/salt/pki/minion/minion.pub
    - mode: 644
    - onlyif: test -f /config/salt/pki/minion/minion.pub

cont_copy_salt_master_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion_master.pub
    - source: /config/salt/pki/minion/minion_master.pub
    - mode: 644
    - onlyif: test -f /config/salt/pki/minion/minion_master.pub

cont_start_minion:
  cmd.run:
    - name: /usr/bin/salt-minion -d
    - require:
      - file: cont_setup_machine_id
      - file: cont_setup_minion_id
      - file: cont_minion_conf

{# store the salt keys to re-use them for the next start of the container #}
wait_for_keys:
  file.exists:
    - name: /etc/salt/pki/minion/minion.pem
    - retry:
        attempts: 15
        interval: 5

cont_store_salt_dirs:
  file.directory:
    - name: /config/salt/pki/minion/
    - makedirs: True
    - mode: 700

cont_store_salt_minion_priv_key:
  file.copy:
    - name: /config/salt/pki/minion/minion.pem
    - source: /etc/salt/pki/minion/minion.pem
    - mode: 400
    - unless: test -f /config/salt/pki/minion/minion.pem
    - require:
      - file: wait_for_keys

cont_store_salt_minion_pub_key:
  file.copy:
    - name: /config/salt/pki/minion/minion.pub
    - source: /etc/salt/pki/minion/minion.pub
    - mode: 644
    - unless: test -f /config/salt/pki/minion/minion.pub
    - require:
      - file: wait_for_keys

cont_store_salt_master_pub_key:
  file.copy:
    - name: /config/salt/pki/minion/minion_master.pub
    - source: /etc/salt/pki/minion/minion_master.pub
    - mode: 644
    - unless: test -f /config/salt/pki/minion/minion_master.pub
    - require:
      - file: wait_for_keys

{%- if salt['file.file_exists']('/config/sysconfig/rhn/systemid') %}

cont_copy_system_id:
  file.copy:
    - name: /etc/sysconfig/rhn/systemid
    - source: /config/sysconfig/rhn/systemid

{%- else %}

cont_fetch_system_id:
  cmd.run:
    - name: /usr/sbin/fetch-certificate {{ system_id }}
    - require:
      - cmd: cont_start_minion
      - file: cont_check_ca_certs
      - file: cont_check_srv_cert

cont_activate:
  cmd.run:
    - name: rhn-proxy-activate --non-interactive --server={{ master }} --ca-cert="{{ ca_certs }}"
    - require:
      - cmd: cont_fetch_system_id

{# we store the systemid after successful activation #}
cont_store_system_id:
  file.copy:
    - name: /config/sysconfig/rhn/systemid
    - source: /etc/sysconfig/rhn/systemid
    - mode: 644
    - makedirs: True
    - require:
      - cmd: cont_activate

{%- endif %}

cont_inst_srv_cert:
  cmd.run:
    - name: rpm -Uv {{ srv_cert_rpm }}

cont_deploy_ca:
  file.copy:
    - name: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - source: {{ ca_certs }}

cont_link_ca:
  file.symlink:
    - name: /usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT
    - target: /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT
    - force: True
    - makedirs: True

cont_update_ca_certs:
  cmd.run:
    - name: /usr/sbin/update-ca-certificates
    - onchanges:
      - file: cont_deploy_ca

cont_squid_conf:
  file.managed:
    - name: /etc/squid/squid.conf
    - source: salt://proxy/squid.conf.templ
    - template: jinja

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
 
