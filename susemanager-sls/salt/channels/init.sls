/etc/zypp/repos.d/susemanager:channels.repo:
  file.managed:
    - source:
      - salt://channels/channels.repo.{{ grains['machine_id'] }}
      - salt://channels/empty.repo
    - user: root
    - group: root
    - mode: 644
