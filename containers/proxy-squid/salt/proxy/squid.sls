{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}

cont_set_fqdn:
  module.run:
    - name: hosts.add_host
    - ip: {{ salt['grains.get']('ip4_interfaces:eth0').pop() }}
    - alias: {{ minion_id }}

cont_squid_conf:
  file.managed:
    - name: /etc/squid/squid.conf
    - source: salt://proxy/squid.conf.templ
    - template: jinja

cont_perm_squid_cache:
  file.directory:
    - name: /var/cache/squid
    - user: squid
    - group: squid
    - dir_mode: 750
    - recurse:
      - user
      - group
      - mode

cont_perm_squid_logs:
  file.directory:
    - name: /var/log/squid
    - user: squid
    - group: squid
    - dir_mode: 750
    - recurse:
      - user
      - group
      - mode

cont_pre_start_squid:
  cmd.run:
    - name: /usr/lib/squid/initialize_cache_if_needed.sh
