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
    - name: /etc/salt/minion.d/master.conf
    - template: jinja
    - mode: 644
    - contents: |
        master: {{ master }}
        server_id_use_crc: adler32
        enable_legacy_startup_events: False
        enable_fqdns_grains: False
        {% if activation_key %}
        grains:
          susemanager:
            activation_key: {{ activation_key }}
        {% endif %}
        start_event_grains:
          - machine_id
          - saltboot_initrd
          - susemanager
        system-environment:
          modules:
            pkg:
              _:
                SALT_RUNNING: 1

cont_start_minion:
  cmd.run:
    - name: /usr/bin/salt-minion -d

cont_fetch_system_id:
  cmd.run:
    - name: /usr/sbin/fetch-certificate {{ system_id }}
