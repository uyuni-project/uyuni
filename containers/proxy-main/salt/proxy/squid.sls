{% set machine_id = salt['environ.get']('UYUNI_MACHINE_ID') %}
{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}

cont_set_fqdn:
  module.run:
    - name: hosts.add_host
    - ip: {{ salt['grains.get']('ip4_interfaces:eth0').pop() }}
    - alias: {{ minion_id }}

cont_setup_machine_id:
  file.managed:
    - name: /etc/machine-id
    - contents: {{ machine_id }}

cont_squid_conf:
  file.managed:
    - name: /etc/squid/squid.conf
    - source: salt://proxy/squid.conf.templ
    - template: jinja

cont_perm_squid:
  file.directory:
    - name: /var/cache/squid
    - user: squid
    - group: root
    - dir_mode: 750

cont_pre_start_squid:
  cmd.run:
    - name: /usr/lib64/squid/initialize_cache_if_needed.sh
