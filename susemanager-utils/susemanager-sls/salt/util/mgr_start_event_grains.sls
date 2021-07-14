{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}
mgr_start_event_grains:
  file.append:
    - name: {{ susemanager_minion_config }}
    - text: |
        start_event_grains: [machine_id, saltboot_initrd, susemanager]
    - unless: grep 'start_event_grains:' /etc/salt/minion.d/susemanager.conf
