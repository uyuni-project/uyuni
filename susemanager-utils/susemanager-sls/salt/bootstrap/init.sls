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
    - makedirs: True

# Manage minion key files in case they are provided in the pillar
{% if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
/etc/salt/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub
    - makedirs: True

/etc/salt/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem
    - makedirs: True

salt-minion:
  pkg.installed:
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo
  service.running:
    - require:
      - pkg: salt-minion
      - file: /etc/salt/pki/minion/minion.pem
      - file: /etc/salt/pki/minion/minion.pub
      - file: /etc/salt/minion.d/susemanager.conf
{% else %}
salt-minion:
  pkg.installed:
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo
  service.running:
    - require:
      - pkg: salt-minion
      - file: /etc/salt/minion.d/susemanager.conf
{% endif %}
