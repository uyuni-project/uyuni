mgrchannels_repo_clean_all:
{%- if grains['os_family'] == 'RedHat' %}
  cmd.run:
    - name: rm "/etc/yum.repos.d/susemanager:channels.repo"
    - runas: root
{%- endif %}
{%- if grains['os_family'] == 'Suse' %}
  cmd.run:
    - name: rm "/etc/zypp/repos.d/susemanager:channels.repo"
    - runas: root
{%- endif %}
