{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager-mine.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager-mine.conf' %}
{%- endif -%}
mgr_disable_mine:
  file.managed:
    - name: {{ susemanager_minion_config }}
    - contents: "mine_enabled: False"
    - unless: grep 'mine_enabled:' /etc/salt/minion.d/susemanager-mine.conf

mgr_salt_minion:
  service.running:
   - name: {{ salt_minion_name }}
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_mine
{% endif %}
