{%- set avoid_venv_salt_minion = salt['pillar.get']('mgr_avoid_venv_salt_minion') %}
{%- set is_salt_ssh = salt['pillar.get']('contact_method') in ['ssh-push', 'ssh-push-tunnel'] %}
{%- set is_saltboot = salt['file.file_exists']('/etc/ImageVersion') %}

{%- if grains['saltversion'] == '3000' and not is_salt_ssh and 
       not avoid_venv_salt_minion == true and not is_saltboot %}

include:
  - util.mgr_switch_to_venv_minion

mgr_remove_susemanagerconf:
  file.absent:
    - name: /etc/salt/minion.d/susemanager.conf
    - require:
      - service: mgr_disable_salt_minion
    - order: last

{%- endif %}

