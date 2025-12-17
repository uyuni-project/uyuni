{%- set venv_is_running = '/venv-salt-minion/' in grains.get('pythonexecutable', '') %}
{%- if venv_is_running %}

{%- if 'mgr_schedule_salt_minion_purge' in salt['schedule.list']() %}
mgr_schedule_salt_minion_purge:
  schedule.absent
{%- endif %}

{%- if not (salt['pillar.get']('mgr_purge_non_venv_salt') or salt['pillar.get']('mgr_purge_non_venv_salt_files') or salt['pillar.get']('mgr_purge_non_venv_salt_force')) %}
mgr_venv_salt_minion_switch_not_required:
  test.succeed_without_changes:
    - comment: Switching to venv-salt-minion is not required as it is already running
{%- endif %}

{%- if salt['pillar.get']('mgr_purge_non_venv_salt') or salt['pillar.get']('mgr_purge_non_venv_salt_force') %}
{%- set pkgs_installed = salt['pkg.list_pkgs']() %}
{%- if 'salt-master' in pkgs_installed and not salt['pillar.get']('mgr_purge_non_venv_salt_force') %}
mgr_salt_master_installed_warning:
  test.fail_without_changes:
    - comment: salt-master is installed, if you really want to enforce salt packages deletion, use `mgr_purge_non_venv_salt_force` pillar
{%- else %}
mgr_purge_non_venv_salt_packages:
  pkg.purged:
    - pkgs:
      - salt
      - salt-common
      - salt-minion
      - python2-salt
      - python3-salt
      - python311-salt
      - python312-salt
      - python313-salt
{%- endif %}
{%- endif %}

{%- if salt['pillar.get']('mgr_purge_non_venv_salt_files') %}
mgr_purge_non_venv_salt_pki_dir:
  cmd.run:
    - name: /usr/bin/rm -rf /etc/salt/minion* /etc/salt/pki/minion
    - onlyif:
      - /usr/bin/test -d /etc/salt/pki/minion

mgr_purge_non_venv_salt_conf_dir:
  file.absent:
    - name: /etc/salt
    - unless:
      - /usr/bin/find /etc/salt -type f -print -quit | /usr/bin/grep -q .
    - require:
      - cmd: mgr_purge_non_venv_salt_pki_dir
{%- endif %}
{%- else %}
{%- set pkgs_installed = salt['pkg.list_pkgs']() %}
{%- set venv_minion_installed = 'venv-salt-minion' in pkgs_installed %}
{%- set venv_minion_available = venv_minion_installed or 'venv-salt-minion' in salt['pkg.list_repo_pkgs']() %}
{%- if venv_minion_available %}
mgr_venv_salt_minion_pkg:
  pkg.installed:
    - name: venv-salt-minion
    - onlyif:
      - ([ {{ venv_minion_installed }} = "False" ])

mgr_copy_salt_minion_id:
  file.copy:
    - name: /etc/venv-salt-minion/minion_id
    - source: /etc/salt/minion_id
    - force: True
    - preserve: True
    - require:
      - pkg: mgr_venv_salt_minion_pkg
    - onlyif:
      - /usr/bin/test -f /etc/salt/minion_id

mgr_copy_salt_minion_configs:
  cmd.run:
    - name: /usr/bin/cp -r /etc/salt/minion.d /etc/venv-salt-minion/
    - require:
      - pkg: mgr_venv_salt_minion_pkg

mgr_copy_salt_minion_grains:
  file.copy:
    - name: /etc/venv-salt-minion/grains
    - source: /etc/salt/grains
    - force: True
    - preserve: True
    - require:
      - pkg: mgr_venv_salt_minion_pkg
    - onlyif:
      - /usr/bin/test -f /etc/salt/grains

mgr_copy_salt_minion_keys:
  cmd.run:
    - name: /usr/bin/rm -f /etc/venv-salt-minion/pki/minion/minion*; /usr/bin/cp -r /etc/salt/pki/minion/minion* /etc/venv-salt-minion/pki/minion/
    - require:
      - cmd: mgr_copy_salt_minion_configs
    - onlyif:
      - /usr/bin/test -f /etc/salt/pki/minion/minion.pem

mgr_enable_venv_salt_minion:
  service.running:
    - name: venv-salt-minion
    - enable: True
    - require:
      - pkg: mgr_venv_salt_minion_pkg
      - cmd: mgr_copy_salt_minion_configs
      - cmd: mgr_copy_salt_minion_keys
      - file: mgr_copy_salt_minion_grains

mgr_disable_salt_minion:
  service.disabled:
    - name: salt-minion
    - require:
      - service: mgr_enable_venv_salt_minion

mgr_schedule_salt_minion_stop:
  cmd.run:
    - name: |
        venv-salt-call --local schedule.add mgr_schedule_salt_minion_stop \
        seconds=5 persist=False maxrunning=1 function='state.high' \
        job_args='[{"mgr_schedule_salt_minion_disabled": {"service": ["dead", {"name": "salt-minion"}, {"enable": False}]}, "mgr_schedule_salt_minion_stop": {"schedule": ["absent"]}}]'

{%- if salt['pillar.get']('mgr_purge_non_venv_salt') or salt['pillar.get']('mgr_purge_non_venv_salt_files') or salt['pillar.get']('mgr_purge_non_venv_salt_force') %}
mgr_schedule_salt_minion_purge:
  cmd.run:
    - name: |
        venv-salt-call --local schedule.add mgr_schedule_salt_minion_purge \
        seconds=10 persist=False maxrunning=1 function='state.apply' \
        job_args='["util.mgr_switch_to_venv_minion"]' \
        job_kwargs='{"pillar": {"mgr_purge_non_venv_salt": {{ salt["pillar.get"]("mgr_purge_non_venv_salt", false) }}, "mgr_purge_non_venv_salt_files": {{ salt["pillar.get"]("mgr_purge_non_venv_salt_files", false) }}, "mgr_purge_non_venv_salt_force": {{ salt["pillar.get"]("mgr_purge_non_venv_salt_force", false) }} }}'
{%- endif %}

{%- else %}

mgr_venv_salt_minion_unavailable:
  test.fail_without_changes:
    - comment: venv-salt-minion package is not available
{%- endif %}
{%- endif %}
