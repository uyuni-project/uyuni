{%- set conf_file = '/etc/salt/minion.d/susemanager.conf' %}
{%- set salt_service = 'salt-minion' %}

{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set conf_file = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- set salt_service = 'venv-salt-minion' %}
{%- endif -%}

{%- set pattern = '^master:.*' %}

{#- Check the minion can reach the new Salt master ports before switching to it #}
{%- set mgr_server = pillar['mgr_server'] %}
{%- set publish_port = salt['pillar.get']('mgr_salt_publish_port') or 4505 %}
{%- set request_port = salt['pillar.get']('mgr_salt_request_port') or 4506 %}
{%- set unreachable = [] %}
{#- network.connect only tries the first address of the master: accept a port reachable on IPv4 or IPv6 #}
{%- for port in [publish_port, request_port] %}
{%-   set reachable = salt['network.connect'](mgr_server, port, timeout=5, family='ipv4').get('result')
                     or salt['network.connect'](mgr_server, port, timeout=5, family='ipv6').get('result') %}
{%-   if not reachable %}
{%-     do unreachable.append(port) %}
{%-   endif %}
{%- endfor %}

{% if unreachable -%}

unreachable_salt_master:
  test.configurable_test_state:
    - changes: False
    - result: False
    - comment: "Can't change proxy. {{ mgr_server }} is not reachable on Salt port(s) {{ unreachable | join(', ') }}"

{% elif salt['file.search'](conf_file, pattern) -%}

{{ conf_file }}:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644

restart:
  module.run:
    - name: cmd.run_bg
    - cmd: "/usr/bin/sleep 2; /usr/sbin/service {{ salt_service }} restart"
    - python_shell: true

{% else -%}

non_standard_conf:
  test.configurable_test_state:
    - changes: False
    - result: False
    - comment: "Can't change proxy. Salt master is not configured in {{ conf_file }}"

{% endif %}
