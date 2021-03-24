{% set machine_id = salt['environ.get']('UYUNI_MACHINE_ID') %}
{% set master = salt['environ.get']('UYUNI_MASTER') %}
{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}
{% set activation_key = salt['environ.get']('UYUNI_ACTIVATION_KEY') %}
{% set ca_certs = salt['environ.get']('UYUNI_CA_CERTS') %}
{% set srv_cert_rpm = salt['environ.get']('UYUNI_SRV_CERT') %}
{% set email = salt['environ.get']('UYUNI_EMAIL', 'root@localhost') %}
{% set system_id = '/etc/sysconfig/rhn/systemid' %}

cont_set_fqdn:
  module.run:
    - name: hosts.add_host
    - ip: {{ salt['grains.get']('ip4_interfaces:eth0').pop() }}
    - alias: {{ minion_id }}

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

cont_copy_salt_minion_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion.pub
    - source: /config/salt/pki/minion/minion.pub
    - mode: 644

cont_copy_salt_master_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion_master.pub
    - source: /config/salt/pki/minion/minion_master.pub
    - mode: 644

cont_copy_system_id:
  file.copy:
    - name: /etc/sysconfig/rhn/systemid
    - source: /config/sysconfig/rhn/systemid

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

cont_cobbler_proxy_conf:
  file.managed:
    - name: /etc/apache2/conf.d/cobbler-proxy.conf
    - source: salt://proxy/cobbler-proxy.conf.templ
    - template: jinja

cont_ssl_conf:
  file.managed:
    - name: /etc/apache2/vhosts.d/ssl.conf
    - source: salt://proxy/ssl.conf


{%- if salt['file.file_exists']('/config/sshpush/id_susemanager_ssh_push') and salt['file.file_exists']('/config/sshpush/id_susemanager_ssh_push.pub') %}
cont_ssh_push_key_existing:
  cmd.run:
    - name: /usr/sbin/mgr-proxy-ssh-push-init -k /config/sshpush/id_susemanager_ssh_push
{% else %}
cont_ssh_push_key:
  cmd.run:
    - name: /usr/sbin/mgr-proxy-ssh-push-init

cont_store_dir:
  file.directory:
    - name: /config/sshpush/
    - mode: 700

cont_store_ssh_push_key:
  file.copy:
    - name: /config/sshpush/id_susemanager_ssh_push
    - source: /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push
    - mode: 600
    - require:
      - cmd: cont_ssh_push_key

cont_store_ssh_push_key_pub:
  file.copy:
    - name: /config/sshpush/id_susemanager_ssh_push.pub
    - source: /var/lib/spacewalk/mgrsshtunnel/.ssh/id_susemanager_ssh_push.pub
    - require:
      - cmd: cont_ssh_push_key

{% endif %}

cont_pre_start_squid:
  cmd.run:
    - name: /usr/lib64/squid/initialize_cache_if_needed.sh
