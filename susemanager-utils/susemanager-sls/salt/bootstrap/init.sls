/etc/zypp/repos.d/susemanager:bootstrap.repo:
  file.managed:
    - source:
      - salt://bootstrap/bootstrap.repo
    - template: jinja
    - mode: 644

salt-minion-package:
  pkg.installed:
    - name: salt-minion
    - require:
      - file: /etc/zypp/repos.d/susemanager:bootstrap.repo

/etc/salt/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644

/etc/salt/minion_id:
  file.managed:
    - contents_pillar: minion_id
    - require:
      - pkg: salt-minion-package

# Manage minion key files in case they are provided in the pillar
{% if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
/etc/salt/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub
    - mode: 644

/etc/salt/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem
    - mode: 400

salt-minion:
  service.running:
    - enable: True
    - require:
      - pkg: salt-minion-package
    - watch:
      - file: /etc/salt/minion_id
      - file: /etc/salt/pki/minion/minion.pem
      - file: /etc/salt/pki/minion/minion.pub
      - file: /etc/salt/minion.d/susemanager.conf
{% else %}
salt-minion:
  service.running:
    - enable: True
    - require:
      - pkg: salt-minion-package
    - watch:
      - file: /etc/salt/minion_id
      - file: /etc/salt/minion.d/susemanager.conf
{% endif %}
