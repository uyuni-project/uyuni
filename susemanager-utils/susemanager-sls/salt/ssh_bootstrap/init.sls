##
##  java bootstrapping calls certs.sls before this state
##
{%- set mgr_sudo_user = salt['pillar.get']('mgr_sudo_user') or 'root' %}

mgr_ssh_identity:
  ssh_auth.present:
    - user: {{ mgr_sudo_user }}
    - source: salt://salt_ssh/mgr_ssh_id.pub
{% if salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
mgr_server_localhost_alias_present:
  host.present:
{% else %}
mgr_server_localhost_alias_absent:
  host.absent:
{% endif %}
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}

{%- if salt['pillar.get']('proxy_pub_key') and salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
no_push_key_authorized:
  ssh_auth.absent:
    - user: {{ mgr_sudo_user }}
    - comment: susemanager-ssh-push

proxy_ssh_identity:
  ssh_auth.present:
    - user: {{ mgr_sudo_user }}
    - source: salt://salt_ssh/{{ salt['pillar.get']('proxy_pub_key') }}
    - require:
      - ssh_auth: no_push_key_authorized
{%- endif %}

{%- set home = salt['user.info'](mgr_sudo_user)['home'] %}

generate_own_ssh_key:
  cmd.run:
    - name: ssh-keygen -N '' -C 'susemanager-own-ssh-push' -f {{ home }}/.ssh/mgr_own_id -t rsa -q
    - creates: {{ home }}/.ssh/mgr_own_id.pub

ownership_own_ssh_key:
  file.managed:
    - name: {{ home }}/.ssh/mgr_own_id
    - user: {{ mgr_sudo_user }}
    - replace: False
    - require:
      - cmd: generate_own_ssh_key

ownership_own_ssh_pub_key:
  file.managed:
    - name: {{ home }}/.ssh/mgr_own_id.pub
    - user: {{ mgr_sudo_user }}
    - replace: False
    - require:
      - cmd: generate_own_ssh_key

no_own_key_authorized:
  ssh_auth.absent:
    - user: {{ mgr_sudo_user }}
    - comment: susemanager-own-ssh-push
    - require:
      - file: ownership_own_ssh_key

authorize_own_key:
  ssh_auth.present:
    - user: {{ mgr_sudo_user }}
    - source: {{ home }}/.ssh/mgr_own_id.pub
    - require:
      - file: ownership_own_ssh_key
      - ssh_auth: no_own_key_authorized

# disable all repos, except of repos flagged with keep:* (should be none)
{% set repos_disabled = {'match_str': 'keep:', 'matching': false} %}
{% include 'channels/disablelocalrepos.sls' %}
{% do repos_disabled.update({'skip': true}) %}

{% include 'channels/gpg-keys.sls' %}
{% include 'bootstrap/remove_traditional_stack.sls' %}

{%- set transactional = grains['transactional'] %}
{%- if transactional %}

{{ salt_minion_name }}:
  mgrcompat.module_run:
    - name: transactional_update.run
    - command: systemctl enable {{ salt_minion_name }}
    - snapshot: continue
    - require:
      - salt-minion-package
      - host: mgr_server_localhost_alias_absent
      - file: {{ salt_config_dir }}/minion_id
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
  {%- if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
      - file: {{ salt_config_dir }}/pki/minion/minion.pem
      - file: {{ salt_config_dir }}/pki/minion/minion.pub
  {%- endif %}

{# Change REBOOT_METHOD to systemd if it is default, otherwise don't change it #}

{%- if not salt['file.file_exists']('/etc/transactional-update.conf') %}
copy_conf_file_to_etc:
  file.copy:
    - name: /etc/transactional-update.conf
    - source: /usr/etc/transactional-update.conf
{%- endif %}

transactional_update_set_reboot_method_systemd:
  file.keyvalue:
    - name: /etc/transactional-update.conf
    - key_values:
        REBOOT_METHOD: 'systemd'
    - separator: '='
    - uncomment: '# '
    - append_if_not_found: True
    - require:
      - file: copy_conf_file_to_etc
    - unless:
      - grep -P '^(?=[\s]*+[^#])[^#]*(REBOOT_METHOD=(?!auto))' /etc/transactional-update.conf

{# Use transactional reboot support -> server will be rebooted according to REBOOT_METHOD #}
reboot_transactional_server:
  mgrcompat.module_run:
    - name: transactional_update.reboot
    - require:
      - {{ salt_minion_name }}

{%- endif %}
