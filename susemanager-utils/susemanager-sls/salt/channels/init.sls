{%- if grains['os_family'] == 'RedHat' %}
mgrchannels_susemanagerplugin:
  file.managed:
  {%- if grains['osmajorrelease']|int == 8 %}
    - name: /usr/lib/python3.6/site-packages/dnf-plugins/susemanagerplugin.py
  {%- else %}
    - name: /usr/share/yum-plugins/susemanagerplugin.py
  {%- endif %}
    - source:
  {%- if grains['osmajorrelease']|int == 8 %}
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.py
  {%- else %}
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.py
  {%- endif %}
    - user: root
    - group: root
    - mode: 644

mgrchannels_susemanagerplugin_conf:
  file.managed:
  {%- if grains['osmajorrelease']|int == 8 %}
    - name: /etc/dnf/plugins/susemanagerplugin.conf
  {%- else %}
    - name: /etc/yum/pluginconf.d/susemanagerplugin.conf
  {%- endif %}
    - source:
  {%- if grains['osmajorrelease']|int == 8 %}
      - salt://channels/dnf-susemanager-plugin/susemanagerplugin.conf
  {%- else %}
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.conf
  {%- endif %}
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
mgrchannels_yum_dnf_clean_all:
  cmd.run:
  {%- if grains['osmajorrelease']|int == 8 %}
    - name: /usr/bin/dnf clean all
  {%- else %}
    - name: /usr/bin/yum clean all
  {%- endif %}
    - runas: root
    - onchanges: 
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
  {%- if grains['osmajorrelease']|int == 8 %}
    -  unless: "/usr/bin/dnf repolist | grep \"repolist: 0$\""
  {%- else %}
    -  unless: "/usr/bin/yum repolist | grep \"repolist: 0$\""
  {%- endif %}
{%- elif grains['os_family'] == 'Debian' %}
{%- include 'channels/debiankeyring.sls' %}
{%- endif %}
