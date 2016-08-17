{%- if grains['os_family'] == 'RedHat' %}
/usr/share/yum-plugins/susemanagerplugin.py:
  file.managed:
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.py
    - user: root
    - group: root
    - mode: 644

/etc/yum/pluginconf.d/susemanagerplugin.conf:
  file.managed:
    - source:
      - salt://channels/yum-susemanager-plugin/susemanagerplugin.conf
    - user: root
    - group: root
    - mode: 644
{%- endif %}

{%- if grains['os_family'] == 'Suse' %}
/etc/zypp/repos.d/susemanager:channels.repo:
{%- elif grains['os_family'] == 'RedHat' %}
/etc/yum.repos.d/susemanager:channels.repo:
{%- endif %}
  file.managed:
    - source:
      - salt://channels/channels.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644
{%- if grains['os_family'] == 'RedHat' %}
    - require:
       - file: /usr/share/yum-plugins/susemanagerplugin.py
       - file: /etc/yum/pluginconf.d/susemanagerplugin.conf
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
yum_clean_all:
  cmd.run:
    - name: /usr/bin/yum clean all
    - user: root
    - onchanges: 
       - file: "/etc/yum.repos.d/susemanager:channels.repo"
    -  unless: "/usr/bin/yum repolist | grep \"repolist: 0$\""
{%- endif %}
