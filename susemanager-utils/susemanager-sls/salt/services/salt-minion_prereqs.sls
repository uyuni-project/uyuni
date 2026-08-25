{%- set salt_minion_name = 'salt-minion' %}
{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- endif -%}

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}

mgr_salt_minion_inst:
  pkg.installed:
    - name: {{ salt_minion_name }}
    - order: last

{% endif %}
