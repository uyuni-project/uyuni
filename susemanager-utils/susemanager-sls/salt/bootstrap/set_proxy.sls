{%- set conf_file = '/etc/salt/minion.d/susemanager.conf' %}
{%- set salt_service = 'salt-minion' %}

{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set conf_file = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- set salt_service = 'venv-salt-minion' %}
{%- endif -%}

{%- set pattern = '^master:.*' %}

{% if salt['file.search'](conf_file, pattern) -%}

{{ conf_file }}:
  file.line:
    - mode: replace
    - match: "{{ pattern }}"
    - content: "master: {{ pillar['mgr_server'] }}"

restart:
  mgrcompat.module_run:
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
