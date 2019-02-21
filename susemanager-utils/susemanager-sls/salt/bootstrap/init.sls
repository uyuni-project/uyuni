# Make sure no SUSE Manager server aliasing left over from ssh-push via tunnel
mgr_server_localhost_alias_absent:
  host.absent:
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}

# disable all susemanager:* repos
{% set repos_disabled = {'match_str': 'susemanager:', 'matching': true} %}
{%- include 'channels/disablelocalrepos.sls' %}

{% set os_base = 'sle' %}
# CentOS6 oscodename is bogus
{%- if "centos" in grains['os']|lower %}
{% set os_base = 'centos' %}
{%- elif "opensuse" in grains['oscodename']|lower %}
{% set os_base = 'opensuse' %}
{%- endif %}

{%- if grains['os_family'] == 'Suse' %}
{%- if "." in grains['osrelease'] %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/' ~ os_base ~ '/' ~ grains['osrelease'].replace('.', '/') ~ '/bootstrap/' %}
{%- else %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/' ~ os_base ~ '/' ~ grains['osrelease'] ~ '/0/bootstrap/' %}
{%- endif %}
{%- elif grains['os_family'] == 'RedHat' %}
{% if salt['file.file_exists' ]('/etc/centos-release') %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/' ~ os_base ~ '/' ~ grains['osmajorrelease'] ~ '/bootstrap/' %}
{%- else %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/res/' ~ grains['osmajorrelease'] ~ '/bootstrap/' %}
{% endif %}
{%- elif grains['os_family'] == 'Debian' %}
{%- if grains['os'] == 'Ubuntu' %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/ubuntu/' ~ grains['osmajorrelease'] ~ '/bootstrap/' %}
{%- else %}
{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/debian/' ~ grains['osmajorrelease'] ~ '/bootstrap/' %}
{%- endif %}
{%- endif %}

{%- set bootstrap_repo_exists = (0 < salt['http.query'](bootstrap_repo_url + 'repodata/repomd.xml', status=True, verify_ssl=False)['status'] < 300) %}

{%- if not grains['os_family'] == 'Debian' %}
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
    - context:
      bootstrap_repo_url: {{bootstrap_repo_url}}
    - mode: 644
    - require:
      - host: mgr_server_localhost_alias_absent
{%- if repos_disabled.count > 0 %}
      - module: disable_repo_*
{%- endif %}
    - onlyif:
      - ([ {{ bootstrap_repo_exists }} = "True" ])
{%- endif %}

{%- if grains['os_family'] == 'RedHat' %}
trust_suse_manager_tools_gpg_key:
  cmd.run:
{%- if grains['osmajorrelease']|int == 6 %}
    - name: rpm --import https://{{ salt['pillar.get']('mgr_server') }}/pub/{{ salt['pillar.get']('gpgkeys:res6tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res6tools:name') }}
{%- elif grains['osmajorrelease']|int == 7 %}
    - name: rpm --import https://{{ salt['pillar.get']('mgr_server') }}/pub/{{ salt['pillar.get']('gpgkeys:res7tools:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res7tools:name') }}
{%- endif %}
    - runas: root

trust_res_gpg_key:
  cmd.run:
    - name: rpm --import https://{{ salt['pillar.get']('mgr_server') }}/pub/{{ salt['pillar.get']('gpgkeys:res:file') }}
    - unless: rpm -q {{ salt['pillar.get']('gpgkeys:res:name') }}
    - runas: root

{%- elif grains['os_family'] == 'Debian' %}
{%- include 'channels/debiankeyring.sls' %}
{%- endif %}

salt-minion-package:
  pkg.installed:
    - name: salt-minion
{%- if not grains['os_family'] == 'Debian' %}
    - require:
      - file: bootstrap_repo
{%- endif %}

/etc/salt/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644
    - require:
      - pkg: salt-minion-package

/etc/salt/minion_id:
  file.managed:
    - contents_pillar: minion_id
    - require:
      - pkg: salt-minion-package

mgr_mine_config:
  file.managed:
    - name: /etc/salt/minion.d/susemanager-mine.conf
    - contents: |
        mine_return_job: True

include:
  - bootstrap.remove_traditional_stack

mgr_update_basic_pkgs:
  pkg.latest:
    - pkgs:
      - openssl
{%- if grains['os_family'] == 'Suse' %}
      - zypper
{%- elif grains['os_family'] == 'RedHat' %}
      - yum
{% endif %}

# Manage minion key files in case they are provided in the pillar
{% if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
/etc/salt/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub
    - mode: 644
    - makedirs: True
    - require:
      - pkg: salt-minion-package

/etc/salt/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem
    - mode: 400
    - makedirs: True
    - require:
      - pkg: salt-minion-package

salt-minion:
  service.running:
    - enable: True
    - require:
      - pkg: salt-minion-package
      - host: mgr_server_localhost_alias_absent
    - watch:
      - file: /etc/salt/minion_id
      - file: /etc/salt/pki/minion/minion.pem
      - file: /etc/salt/pki/minion/minion.pub
      - file: /etc/salt/minion.d/susemanager.conf
      - file: mgr_mine_config
{% else %}
salt-minion:
  service.running:
    - enable: True
    - require:
      - pkg: salt-minion-package
      - host: mgr_server_localhost_alias_absent
    - watch:
      - file: /etc/salt/minion_id
      - file: /etc/salt/minion.d/susemanager.conf
      - file: mgr_mine_config
{% endif %}
