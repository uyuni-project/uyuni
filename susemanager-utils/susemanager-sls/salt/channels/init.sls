{%- if grains['os_family'] == 'RedHat' %}
mgrchannels_susemanagerplugin:
  file.managed:
    - name: /usr/share/yum-plugins/susemanagerplugin.py
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf:
  file.managed:
    - name: /etc/yum/pluginconf.d/susemanagerplugin.conf
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644
{%- endif %}

mgrchannels_repo:
  file.managed:
{%- if grains['os_family'] == 'Suse' %}
    - name: "/etc/zypp/repos.d/susemanager:channels.repo"
{%- elif grains['os_family'] == 'RedHat' %}
    - name: "/etc/yum.repos.d/susemanager:channels.repo"
{%- elif grains['os_family'] == 'Debian' %}
    - name: "/etc/apt/sources.list.d/susemanager:channels.list"
{%- endif %}
    - source:
      - salt://channels/channels.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644
{%- if grains['os_family'] == 'RedHat' %}
    - require:
       - file: mgrchannels_susemanagerplugin
       - file: mgrchannels_susemanagerplugin_conf
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
mgrchannels_yum_clean_all:
  cmd.run:
    - name: /usr/bin/yum clean all
    - runas: root
    - onchanges: 
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/yum repolist | grep \"repolist: 0$\""
{%- endif %}
