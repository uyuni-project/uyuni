{%- set susemanager_conf='/etc/salt/minion.d/susemanager.conf' %}
{%- set venv_susemanager_conf='/etc/opt/venv-salt-minion/minion.d/susemanager.conf' %}
{%- set managed_minion=salt['file.file_exists'](susemanager_conf) and
                       not salt['file.replace'](susemanager_conf, '^master: .*', 'master: ' + pillar['mgr_server'],
                                                dry_run=True, show_changes=False, ignore_if_missing=True) %}
{%- set venv_managed_minion=salt['file.file_exists'](venv_susemanager_conf) and
                            not salt['file.replace'](venv_susemanager_conf, '^master: .*', 'master: ' + pillar['mgr_server'],
                                                     dry_run=True, show_changes=False, ignore_if_missing=True) %}
{%- if managed_minion or venv_managed_minion %}
{%- set pkgs_installed = salt['pkg.info_installed']() %}
{%- set venv_minion_installed = pkgs_installed.get('venv-salt-minion', False) and True %}
{%- set venv_minion_available = venv_minion_installed or salt['pkg.latest_version']('venv-salt-minion') or False %}
{%- if venv_minion_available %}
venv-salt-minion-pkg:
  pkg.installed:
    - name: venv-salt-minion
    - onlyif:
      - ([ {{ venv_minion_installed }} = "False" ])

copy-salt-minion-id:
  file.copy:
    - name: /etc/opt/venv-salt-minion/minion_id
    - source: /etc/salt/minion_id
    - require:
      - pkg: venv-salt-minion-pkg
    - onlyif:
      - test -f /etc/salt/minion_id

copy-salt-minion-configs:
  cmd.run:
    - name: cp -r /etc/salt/minion.d /etc/opt/venv-salt-minion/
    - require:
      - pkg: venv-salt-minion-pkg
    - onlyif:
      - ([ {{ venv_managed_minion }} = "False" ])

copy-salt-minion-keys:
  cmd.run:
    - name: cp -r /etc/salt/pki/minion/minion* /etc/opt/venv-salt-minion/pki/minion/
    - require:
      - cmd: copy-salt-minion-configs
    - onlyif:
      - test -f /etc/salt/pki/minion/minion_master.pub
    - unless:
      - test -f /etc/opt/venv-salt-minion/pki/minion/minion_master.pub

enable-venv-salt-minion:
  service.running:
    - name: venv-salt-minion
    - enable: True
    - require:
      - cmd: copy-salt-minion-keys

disable-salt-minion:
  service.dead:
    - name: salt-minion
    - enable: False
    - require:
      - service: enable-venv-salt-minion

{%- if salt['pillar.get']('mgr_purge_non_venv_salt') %}
purge-non-venv-salt-packages:
  pkg.purged:
    - pkgs:
      - salt
      - salt-common
      - salt-minion
      - python2-salt
      - python3-salt
    - require:
      - service: disable-salt-minion
{%- endif %}

{%- if salt['pillar.get']('mgr_purge_non_venv_salt_files') %}
purge-non-venv-salt-pki-dir:
  cmd.run:
    - name: rm -rf /etc/salt/minion* /etc/salt/pki/minion
    - onlyif:
      - test -d /etc/salt/pki/minion
    - require:
      - service: disable-salt-minion

purge-non-venv-salt-conf-dir:
  file.absent:
    - name: /etc/salt
    - unless:
      - find /etc/salt -type f -print -quit | grep -q .
    - require:
      - cmd: purge-non-venv-salt-pki-dir
{%- endif %}
{%- else %}
venv-salt-minion-unavailable:
  test.fail_without_changes:
    - comment: venv-salt-minion package is not available
{%- endif %}
{%- else %}
salt-minion-of-another-master:
  test.fail_without_changes:
    - comment: The salt-minion is managed by another master
{%- endif %}
