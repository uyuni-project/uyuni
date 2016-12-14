bootstrap_repo:
  file.managed:
{%- if grains['os_family'] == 'Suse' %}
    - name: /etc/zypp/repos.d/susemanager:bootstrap.repo
{%- elif grains['os_family'] == 'RedHat' %}
    - name: /etc/yum.repos.d/susemanager:bootstrap.repo
{%- endif %}
    - source:
      - salt://bootstrap/bootstrap.repo
    - template: jinja
    - mode: 644

{%- if grains['os_family'] == 'RedHat' %}
trust_suse_manager_tools_gpg_key:
  cmd.run:
{%- if grains['osmajorrelease'] == '6' %}
    - name: rpm --import https://{{ salt['pillar.get']('master') }}/pub/{{ salt['pillar.get']('gpgkeys:res6tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res6tools:name') }}
{%- elif grains['osmajorrelease'] == '7' %}
    - name: rpm --import https://{{ salt['pillar.get']('master') }}/pub/{{ salt['pillar.get']('gpgkeys:res7tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res7tools:name') }}
{%- endif %}
    - user: root

trust_res_gpg_key:
  cmd.run:
    - name: rpm --import https://{{ salt['pillar.get']('master') }}/pub/{{ salt['pillar.get']('gpgkeys:res:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res:name') }}
    - user: root
{%- endif %}

salt-minion-package:
  pkg.installed:
    - name: salt-minion
    - require:
      - file: bootstrap_repo

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
# Make sure no master aliasing left over from ssh-push via tunnel
master_localhost_alias_absent:
  host.absent:
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('master') }}
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
      - host: master_localhost_alias_absent
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
      - host: master_localhost_alias_absent
    - watch:
      - file: /etc/salt/minion_id
      - file: /etc/salt/minion.d/susemanager.conf
{% endif %}
