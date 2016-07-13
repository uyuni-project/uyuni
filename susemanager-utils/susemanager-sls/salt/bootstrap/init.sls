/etc/zypp/repos.d/susemanager:bootstrap.repo:
  file.managed:
    - source:
      - salt://bootstrap/bootstrap.repo
    - template: jinja
    - user: root
    - group: root
    - mode: 644

/etc/salt/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - user: root
    - group: root
    - mode: 644

# Manage minion key files in case they are provided in the pillar
{% if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
/etc/salt/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub

/etc/salt/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem

salt-minion:
  pkg.installed:
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo
  service.running:
    - watch:
      - file: /etc/salt/minion.d/susemanager.conf
      - file: /etc/salt/pki/minion/minion.pem
      - file: /etc/salt/pki/minion/minion.pub
    - require:
      - pkg: salt-minion
{% else %}
salt-minion:
  pkg.installed:
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo
  service.running:
    - watch:
      - file: /etc/salt/minion.d/susemanager.conf
    - require:
      - pkg: salt-minion
{% endif %}
