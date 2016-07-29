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
