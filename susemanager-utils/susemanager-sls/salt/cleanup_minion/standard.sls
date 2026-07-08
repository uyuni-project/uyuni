{%- set salt_minion_name = 'salt-minion' %}
{%- set salt_config_dir = '/etc/salt' %}
{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set salt_config_dir = '/etc/venv-salt-minion' %}
{%- endif -%}

mgr_schedule_salt_minion_stop:
  schedule.present:
    - function: cmd.run
    - seconds: 3
    - once: True
    - persist: False
    - maxrunning: 1
    - job_args:
      - |
        /usr/bin/rm -f /etc/sysconfig/rhn/systemid \
                       {{ salt_config_dir }}/minion.d/_schedule.conf \
                       {{ salt_config_dir }}/minion.d/susemanager.conf \
                       {{ salt_config_dir }}/minion.d/master.conf \
                       {{ salt_config_dir }}/pki/minion/minion.pem \
                       {{ salt_config_dir }}/pki/minion/minion.pub \
                       {{ salt_config_dir }}/pki/minion/minion_master.pub && \
        /usr/bin/rm -rf /var/cache/{{ salt_minion_name }} && \
        /usr/bin/systemctl stop {{ salt_minion_name }}
    - order: last
