{%- set conf_file = '/etc/salt/minion.d/susemanager.conf' %}
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
    - cmd: "sleep 2; service salt-minion restart"
    - python_shell: true

{% else -%}

non_standard_conf:
  test.configurable_test_state:
    - changes: False
    - result: False
    - comment: "Can't change proxy. Salt master is not configured in {{ conf_file }}"

{% endif %}
