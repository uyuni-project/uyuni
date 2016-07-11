/etc/zypp/repos.d/susemanager:bootstrap.repo:
  file.managed:
    - source:
      - salt://bootstrap/bootstrap.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644

salt-minion:
  pkg.installed:
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo
  service.running:
    - watch:
      - file: /etc/salt/minion.d/susemanager.conf
    - require:
      - pkg: salt-minion

/etc/salt/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - user: root
    - group: root
    - mode: 644
