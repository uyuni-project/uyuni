{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}
mgr_start_event_grains:
  file.append:
    - name: {{ susemanager_minion_config }}
    - text: |
        start_event_grains: [machine_id, saltboot_initrd, susemanager]
    - unless: /usr/bin/grep 'start_event_grains:' {{ susemanager_minion_config }}
