{%- set avoid_salt_bundle = salt['pillar.get']('mgr_avoid_venv_salt_minion', false) == true %}
{%- set is_not_salt_ssh = salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
{%- set is_not_saltboot = salt['file.file_exists']('/etc/ImageVersion') == false %}
{%- set is_not_salt_bundle = "venv-salt-minion" not in grains["pythonexecutable"] %}

{%- if not avoid_salt_bundle and is_not_salt_bundle and is_not_salt_ssh and is_not_saltboot %}

include:
  - util.mgr_switch_to_venv_minion

mgr_remove_susemanagerconf:
  file.absent:
    - name: /etc/salt/minion.d/susemanager.conf
    - require:
      - service: mgr_disable_salt_minion
    - order: last

{%- endif %}

