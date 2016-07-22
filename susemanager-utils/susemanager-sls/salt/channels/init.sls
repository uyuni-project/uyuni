{% if grains['os_family'] == "Suse" %}
/etc/zypp/repos.d/susemanager:channels.repo:
  file.managed:
    - source:
      - salt://channels/channels.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644
{% endif %}
