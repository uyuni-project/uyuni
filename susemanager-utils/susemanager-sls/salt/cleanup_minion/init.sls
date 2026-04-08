{%- set salt_minion_name = 'salt-minion' %}
{%- set salt_config_dir = '/etc/salt' %}
{# Use venv-salt-minion if the state applied with it #}
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
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

{%- if not grains.get('transactional', False) %}
mgr_async_identity_cleanup:
  cmd.run:
    - bg: True
    - name: |
        /usr/bin/sleep 1 && \
        /usr/bin/rm -f /etc/sysconfig/rhn/systemid \
                       {{ salt_config_dir }}/minion.d/susemanager.conf \
                       {{ salt_config_dir }}/minion.d/master.conf \
                       {{ salt_config_dir }}/pki/minion/minion.pem \
                       {{ salt_config_dir }}/pki/minion/minion.pub \
                       {{ salt_config_dir }}/pki/minion/minion_master.pub
{%- endif %}

{%- if not grains.get('transactional', False) %}
mgr_schedule_salt_minion_stop:
  schedule.present:
    - function: cmd.run
    - seconds: 3
    - once: True
    - persist: False
    - maxrunning: 1
    - job_args:
      - |
        /usr/bin/rm -rf /var/cache/{{ salt_minion_name }} && \
        /usr/bin/rm -f {{ salt_config_dir }}/minion.d/_schedule.conf && \
        /usr/bin/systemctl stop {{ salt_minion_name }}
    - order: last
{%- endif %}

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_disable_salt:
  cmd.run:
    - name: /usr/bin/systemctl disable {{ salt_minion_name }}
{%- endif %}
