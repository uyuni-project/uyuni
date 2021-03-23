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

cont_copy_salt_minion_priv_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion.pem
    - source: /config/salt/pki/minion/minion.pem
    - mode: 400
    - onlyif: test -f /config/salt/pki/minion/minion.pem

cont_copy_salt_minion_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion.pub
    - source: /config/salt/pki/minion/minion.pub
    - mode: 644
    - onlyif: test -f /config/salt/pki/minion/minion.pub

cont_copy_salt_master_pub_key:
  file.copy:
    - name: /etc/salt/pki/minion/minion_master.pub
    - source: /config/salt/pki/minion/minion_master.pub
    - mode: 644
    - onlyif: test -f /config/salt/pki/minion/minion_master.pub

cont_start_minion:
  cmd.run:
    - name: /usr/bin/salt-minion -d

{# store the salt keys to re-use them for the next start of the container #}
wait_for_keys:
  file.exists:
    - name: /etc/salt/pki/minion/minion.pem
    - retry:
        attempts: 15
        interval: 5

cont_store_salt_dirs:
  file.directory:
    - name: /config/salt/pki/minion/
    - makedirs: True
    - mode: 700

cont_store_salt_minion_priv_key:
  file.copy:
    - name: /config/salt/pki/minion/minion.pem
    - source: /etc/salt/pki/minion/minion.pem
    - mode: 400
    - unless: test -f /config/salt/pki/minion/minion.pem
    - require:
      - file: wait_for_keys

cont_store_salt_minion_pub_key:
  file.copy:
    - name: /config/salt/pki/minion/minion.pub
    - source: /etc/salt/pki/minion/minion.pub
    - mode: 644
    - unless: test -f /config/salt/pki/minion/minion.pub
    - require:
      - file: wait_for_keys

cont_store_salt_master_pub_key:
  file.copy:
    - name: /config/salt/pki/minion/minion_master.pub
    - source: /etc/salt/pki/minion/minion_master.pub
    - mode: 644
    - unless: test -f /config/salt/pki/minion/minion_master.pub
    - require:
      - file: wait_for_keys

cont_fetch_system_id:
  cmd.run:
    - name: /usr/sbin/fetch-certificate {{ system_id }}
