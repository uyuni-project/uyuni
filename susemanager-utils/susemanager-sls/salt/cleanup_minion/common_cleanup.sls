{%- set salt_minion_name = 'salt-minion' %}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
{%- set salt_minion_name = 'venv-salt-minion' %}
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
mgrchannels_repo_clean_channels_deb822:
  file.absent:
    - name: /etc/apt/sources.list.d/susemanager:channels.sources
mgrchannels_repo_clean_auth:
  file.absent:
    - name: /etc/apt/auth.conf.d/susemanager.conf

mgrchannels_repo_clean_keyring:
  file.absent:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
{%- endif %}


{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_disable_salt:
  cmd.run:
    - name: /usr/bin/systemctl disable {{ salt_minion_name }}
{%- endif %}
