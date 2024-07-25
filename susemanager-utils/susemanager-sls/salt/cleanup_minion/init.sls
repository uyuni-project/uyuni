{%- set salt_minion_name = 'salt-minion' %}
{%- set salt_config_dir = '/etc/salt' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set salt_config_dir = '/etc/venv-salt-minion' %}
{%- endif -%}

{%- if grains['os_family'] == 'RedHat' %}
mgrchannels_repo_clean_all:
  file.absent:
    - name: /etc/yum.repos.d/susemanager:channels.repo
{%- endif %}
{%- if grains['os_family'] == 'Suse' %}
mgrchannels_repo_clean_all:
  file.absent:
    - name: /etc/zypp/repos.d/susemanager:channels.repo
{%- endif %}
{%- if grains['os_family'] == 'Debian' %}
mgrchannels_repo_clean_channels:
  file.absent:
    - name: /etc/apt/sources.list.d/susemanager:channels.list
mgrchannels_repo_clean_auth:
  file.absent:
    - name: /etc/apt/auth.conf.d/susemanager.conf

mgrchannels_repo_clean_keyring:
  file.absent:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
{%- endif %}

mgr_mark_no_longer_managed:
  file.absent:
    - name: /etc/sysconfig/rhn/systemid

mgr_remove_salt_config:
  file.absent:
    - name: {{ salt_config_dir }}/minion.d/susemanager.conf

mgr_remove_salt_config_altname:
  file.absent:
     - name: {{ salt_config_dir }}/minion.d/master.conf

mgr_remove_salt_priv_key:
  file.absent:
     - name: {{ salt_config_dir }}/pki/minion/minion.pem

mgr_remove_salt_pub_key:
  file.absent:
     - name: {{ salt_config_dir }}/pki/minion/minion.pub

mgr_remove_salt_master_key:

  file.absent:
     - name: {{ salt_config_dir }}/pki/minion/minion_master.pub

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_disable_salt:
  cmd.run:
    - name: systemctl disable {{ salt_minion_name }}
    - require:
      - file: mgr_remove_salt_config

{%- if not grains['transactional'] %}
mgr_stop_salt:
  cmd.run:
    - bg: True
    - name: sleep 9 && systemctl stop {{ salt_minion_name }}
    - order: last
    - require:
      - file: mgr_remove_salt_config
{% endif %}
{% endif %}
