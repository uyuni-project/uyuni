{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}
mgr_disable_fqdns_grains:
  file.append:
    - name: {{ susemanager_minion_config }}
    - text: "enable_fqdns_grains: False"
    - unless: grep 'enable_fqdns_grains:' {{ susemanager_minion_config }}

mgr_salt_minion:
  service.running:
   - name: {{ salt_minion_name }}
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_fqdns_grains
