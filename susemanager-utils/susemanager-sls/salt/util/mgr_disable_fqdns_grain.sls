{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}
mgr_disable_fqdns_grains:
  file.append:
    - name: {{ susemanager_minion_config }}
    - text: "enable_fqdns_grains: False"
    - unless: /usr/bin/grep 'enable_fqdns_grains:' {{ susemanager_minion_config }}

mgr_salt_minion:
  service.running:
   - name: {{ salt_minion_name }}
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_fqdns_grains
