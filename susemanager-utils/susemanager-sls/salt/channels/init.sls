{% if grains['os_family'] == 'RedHat' %}
/usr/share/yum-plugins/sumaplugin.py:
  file.managed:
    - source:
      - salt://channels/yum-suma-plugin/sumaplugin.py
    - user: root
    - group: root
    - mode: 644

/etc/yum/pluginconf.d/sumaplugin.conf:
  file.managed:
    - source:
      - salt://channels/yum-suma-plugin/sumaplugin.conf
    - user: root
    - group: root
    - mode: 644
{% endif %}

{% if grains['os_family'] == 'Suse' %}
/etc/zypp/repos.d/susemanager:channels.repo:
{% elif grains['os_family'] == 'RedHat' %}
/etc/yum.repos.d/susemanager:channels.repo:
{% endif %}
  file.managed:
    - source:
      - salt://channels/channels.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644
{% if grains['os_family'] == 'RedHat' %}
    - require:
       - file: /usr/share/yum-plugins/sumaplugin.py
       - file: /etc/yum/pluginconf.d/sumaplugin.conf
{% endif %}
