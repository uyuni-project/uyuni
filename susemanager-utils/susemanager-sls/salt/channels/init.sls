{%- set is_yum = salt['cmd.which']("yum") %}
{%- set is_dnf = salt['cmd.which']("dnf") %}
{%- if grains['os_family'] == 'RedHat' %}
{%- if is_dnf %}
mgrchannels_susemanagerplugin_dnf:
  file.managed:
    - name: /usr/lib/python{{ grains['pythonversion'][0] }}.{{ grains['pythonversion'][1] }}/site-packages/dnf-plugins/susemanagerplugin.py
    - source:
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf_dnf:
  file.managed:
    - name: /etc/dnf/plugins/susemanagerplugin.conf
    - source:
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644
{%- endif %}

{%- if is_yum %}
mgrchannels_susemanagerplugin_yum:
  file.managed:
    - name: /usr/share/yum-plugins/susemanagerplugin.py
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf_yum:
  file.managed:
    - name: /etc/yum/pluginconf.d/susemanagerplugin.conf
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644
{%- endif %}
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
{%- if is_dnf %}
       - file: mgrchannels_susemanagerplugin_dnf
       - file: mgrchannels_susemanagerplugin_conf_dnf
{%- endif %}
{%- if is_yum %}
       - file: mgrchannels_susemanagerplugin_yum
       - file: mgrchannels_susemanagerplugin_conf_yum
{%- endif %}
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
{%- if is_dnf %}
mgrchannels_dnf_clean_all:
  cmd.run:
    - name: /usr/bin/dnf clean all
    - runas: root
    - onchanges:
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/dnf repolist | grep \"repolist: 0$\""
{%- endif %}
{%- if is_yum %}
mgrchannels_yum_clean_all:
  cmd.run:
    - name: /usr/bin/yum clean all
    - runas: root
    - onchanges: 
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/yum repolist | grep \"repolist: 0$\""
{%- endif %}
{%- elif grains['os_family'] == 'Debian' %}
{%- include 'channels/debiankeyring.sls' %}
{%- endif %}
