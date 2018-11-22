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
mgrchannels_repo_clean_keyring:
  file.absent:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
{%- endif %}
