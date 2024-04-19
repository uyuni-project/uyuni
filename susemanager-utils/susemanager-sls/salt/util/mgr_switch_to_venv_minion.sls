{%- set susemanager_conf='/etc/salt/minion.d/susemanager.conf' %}
{%- set venv_susemanager_conf='/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- set managed_minion=salt['file.file_exists'](susemanager_conf) and
                       not salt['file.replace'](susemanager_conf, '^master: .*', 'master: ' + pillar['mgr_server'],
                                                dry_run=True, show_changes=False, ignore_if_missing=True) %}
{%- set venv_managed_minion=salt['file.file_exists'](venv_susemanager_conf) and
                            not salt['file.replace'](venv_susemanager_conf, '^master: .*', 'master: ' + pillar['mgr_server'],
                                                     dry_run=True, show_changes=False, ignore_if_missing=True) %}
{%- if managed_minion or venv_managed_minion %}
{%- set pkgs_installed = salt['pkg.list_pkgs']() %}
{%- set venv_minion_installed = 'venv-salt-minion' in pkgs_installed %}
{%- set venv_minion_available = venv_minion_installed or 'venv-salt-minion' in salt['pkg.list_repo_pkgs']() %}
{%- if venv_minion_available %}
include:
  - services.salt-minion
 
mgr_venv_salt_minion_pkg:
  pkg.installed:
    - name: venv-salt-minion
    - onlyif:
      - ([ {{ venv_minion_installed }} = "False" ])

mgr_copy_salt_minion_id:
  file.copy:
    - name: /etc/venv-salt-minion/minion_id
    - source: /etc/salt/minion_id
    - require:
      - pkg: mgr_venv_salt_minion_pkg
    - onlyif:
      - test -f /etc/salt/minion_id

mgr_copy_salt_minion_configs:
  cmd.run:
    - name: cp -r /etc/salt/minion.d /etc/venv-salt-minion/
    - require:
      - pkg: mgr_venv_salt_minion_pkg
    - onlyif:
      - ([ {{ venv_managed_minion }} = "False" ])

mgr_copy_salt_minion_grains:
  file.copy:
    - name: /etc/venv-salt-minion/grains
    - source: /etc/salt/grains
    - require:
      - pkg: mgr_venv_salt_minion_pkg
    - onlyif:
      - test -f /etc/salt/grains

mgr_copy_salt_minion_keys:
  cmd.run:
    - name: cp -r /etc/salt/pki/minion/minion* /etc/venv-salt-minion/pki/minion/
    - require:
      - cmd: mgr_copy_salt_minion_configs
    - onlyif:
      - test -f /etc/salt/pki/minion/minion_master.pub
    - unless:
      - test -f /etc/venv-salt-minion/pki/minion/minion_master.pub

mgr_enable_venv_salt_minion:
  service.running:
    - name: venv-salt-minion
    - enable: True
    - require:
      - cmd: mgr_copy_salt_minion_keys

mgr_disable_salt_minion:
  service.dead:
    - name: salt-minion
    - enable: False
    - require:
      - service: mgr_enable_venv_salt_minion
      - sls: services.salt-minion

{%- if salt['pillar.get']('mgr_purge_non_venv_salt') %}
mgr_purge_non_venv_salt_packages:
  pkg.purged:
    - pkgs:
      - salt
      - salt-common
      - salt-minion
      - python2-salt
      - python3-salt
    - require:
      - service: mgr_disable_salt_minion
{%- endif %}

{%- if salt['pillar.get']('mgr_purge_non_venv_salt_files') %}
mgr_purge_non_venv_salt_pki_dir:
  cmd.run:
    - name: rm -rf /etc/salt/minion* /etc/salt/pki/minion
    - onlyif:
      - test -d /etc/salt/pki/minion
    - require:
      - service: mgr_disable_salt_minion

mgr_purge_non_venv_salt_conf_dir:
  file.absent:
    - name: /etc/salt
    - unless:
      - find /etc/salt -type f -print -quit | grep -q .
    - require:
      - cmd: mgr_purge_non_venv_salt_pki_dir
{%- endif %}
{%- else %}
mgr_venv_salt_minion_unavailable:
  test.fail_without_changes:
    - comment: venv-salt-minion package is not available
{%- endif %}
{%- else %}
mgr_salt_minion_of_another_master:
  test.fail_without_changes:
    - comment: The salt-minion is managed by another master
{%- endif %}
