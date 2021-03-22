{% set machine_id = salt['environ.get']('UYUNI_MACHINE_ID') %}
{% set master = salt['environ.get']('UYUNI_MASTER') %}
{% set minion_id = salt['environ.get']('UYUNI_MINION_ID') %}
{% set activation_key = salt['environ.get']('UYUNI_ACTIVATION_KEY') %}
{% set system_id = '/etc/sysconfig/rhn/systemid' %}

cont_setup_machine_id:
  file.managed:
    - name: /etc/machine-id
    - contents: {{ machine_id }}

cont_setup_minion_id:
  file.managed:
    - name: /etc/salt/minion_id
    - contents: {{ minion_id }}

cont_minion_conf:
  file.managed:
    - name: /etc/salt/minion.d/susemanager.conf
    - source: salt://proxy/susemanager.conf.templ
    - template: jinja
    - mode: 644

cont_start_minion:
  cmd.run:
    - name: /usr/bin/salt-minion -d

cont_fetch_system_id:
  cmd.run:
    - name: /usr/sbin/fetch-certificate {{ system_id }}
