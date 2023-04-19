{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
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
    - name: {{ susemanager_minion_config }}

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
