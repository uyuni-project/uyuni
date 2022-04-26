##
##  java bootstrapping calls certs.sls before this state
##

# Make sure no SUSE Manager server aliasing left over from ssh-push via tunnel
mgr_server_localhost_alias_absent:
  host.absent:
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}

no_ssh_push_key_authorized:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: salt://salt_ssh/mgr_ssh_id.pub
    - comment: susemanager-default-contact-method

# disable all susemanager:* repos
{% set repos_disabled = {'match_str': 'susemanager:', 'matching': true} %}
{%- include 'channels/disablelocalrepos.sls' %}

# SUSE OS Family
{%- if grains['os_family'] == 'Suse' %}
  {% set os_base = 'sle' %}
  {% set osrelease_major = grains['osrelease_info'][0] %}
  #exceptions to the family rule
  {%- if "opensuse" in grains['oscodename']|lower %}
    {% set os_base = 'opensuse' %}
  {%- endif %}
  {%- if (grains['osrelease_info']| length) < 2 %}
    {% set osrelease_minor = 0 %}
  {%- else %}
    {% set osrelease_minor = grains['osrelease_info'][1] %}
  {%- endif %}
  #end of expections
  {% set osrelease = osrelease_major|string + '/' + osrelease_minor|string %}
{%- endif %}

# Debian OS Family
{%- if grains['os_family'] == 'Debian' %}
  ## This common part should cover most of distro e.g. Debian, Ubuntu
  {%- set os_base = grains['os_family']|lower %}
  {% set osrelease = grains['osrelease_info'][0] %}
  #exceptions to the family rule
  {%- if 'astraLinuxce' in grains['osfullname']|lower %}
    {%- set os_base = 'astra' %}
    {% set osrelease = grains['oscodename'] %}
  {%- elif grains['os'] == 'Ubuntu' %}
    {%- set os_base = grains['os']|lower %}
    {% set osrelease = grains['osrelease_info'][0]|string + '/' + grains['osrelease_info'][1]|string %}
  {%- endif %}
  #end of expections
{%- endif %}


# RedHat OS Family
{%- if grains['os_family'] == 'RedHat' %}
  ## This common part should cover most of distro e.g. Centos
  {%- set os_base = grains['os']|lower %} 
  {% set osrelease = grains['osrelease_info'][0] %}
  #exception to the family rule
  {%- if 'redhat' in grains['osfullname']|lower  %}
    {%- set os_base = 'res' %}
  {%- elif 'sle' in grains['osfullname']|lower %}
    {%- set os_base = 'res' %}
  {%- elif 'rocky' in grains['osfullname']|lower %}
    {%- set os_base = 'rockylinux' %}
  {%- elif 'amazon' in grains['osfullname']|lower %}
    {%- set os_base = 'amzn' %}
  {%- elif 'alibaba' in grains['osfullname']|lower %}
    {%- set os_base = 'alibaba' %}
  {%- elif 'oracle' in grains['osfullname']|lower %}
    {%- set os_base = 'oracle' %}
  {%- endif %}
  #end of expections
{%- endif %}

{% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/' ~ os_base ~ '/' ~ osrelease ~ '/bootstrap/' %}

{%- if grains['os_family'] == 'RedHat' or  grains['os_family'] == 'Suse'%}
  {% set bootstrap_repo_request = salt['http.query'](bootstrap_repo_url + 'repodata/repomd.xml', status=True, verify_ssl=False) %}
  {%- if 'status' not in bootstrap_repo_request %}
    {{ raise('Missing request status: {}'.format(bootstrap_repo_request)) }}
  # if bootstrap does not work, try with RedHat and re-test
  {%- elif grains['os_family'] == 'RedHat' and not (0 < bootstrap_repo_request['status'] < 300) %}
    {%- set os_base = 'res' %}
    {% set osrelease = grains['osrelease_info'][0] %}
    {% set bootstrap_repo_url = 'https://' ~ salt['pillar.get']('mgr_server') ~ '/pub/repositories/' ~ os_base ~ '/' ~ osrelease ~ '/bootstrap/' %}
    {% set bootstrap_repo_request = salt['http.query'](bootstrap_repo_url + 'repodata/repomd.xml', status=True, verify_ssl=False) %}
    {%- if 'status' not in bootstrap_repo_request %}
      {{ raise('Missing request status: {}'.format(bootstrap_repo_request)) }}
    {%- elif bootstrap_repo_request['status'] == 901 %}
      {{ raise(bootstrap_repo_request['error']) }}
    {%- endif %}
  {%- elif bootstrap_repo_request['status'] == 901 %}
    {{ raise(bootstrap_repo_request['error']) }}
  {%- endif %}
  {%- set bootstrap_repo_exists = (0 < bootstrap_repo_request['status'] < 300) %}
{%- elif grains['os_family'] == 'Debian' %}
  {%- set bootstrap_repo_exists = (0 < salt['http.query'](bootstrap_repo_url + 'dists/bootstrap/Release', status=True, verify_ssl=False).get('status', 0) < 300) %}
{%- endif %}


bootstrap_repo:
  file.managed:
{%- if grains['os_family'] == 'Suse' %}
    - name: /etc/zypp/repos.d/susemanager:bootstrap.repo
{%- elif grains['os_family'] == 'RedHat' %}
    - name: /etc/yum.repos.d/susemanager:bootstrap.repo
{%- elif grains['os_family'] == 'Debian' %}
    - name: /etc/apt/sources.list.d/susemanager_bootstrap.list
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
      - mgrcompat: disable_repo_*
{%- endif %}
    - onlyif:
      - ([ {{ bootstrap_repo_exists }} = "True" ])

{% include 'channels/gpg-keys.sls' %}

{%- set salt_minion_name = 'salt-minion' %}
{%- set salt_config_dir = '/etc/salt' %}
{% set venv_available_request = salt['http.query'](bootstrap_repo_url + 'venv-enabled-' + grains['osarch'] + '.txt', status=True, verify_ssl=False) %}
{# Prefer venv-salt-minion if available and not disabled #}
{%- set use_venv_salt = salt['pillar.get']('mgr_force_venv_salt_minion') or (0 < venv_available_request.get('status', 404) < 300) and not salt['pillar.get']('mgr_avoid_venv_salt_minion') %}
{%- if use_venv_salt %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set salt_config_dir = '/etc/venv-salt-minion' %}
{%- endif -%}

salt-minion-package:
  pkg.installed:
    - name: {{ salt_minion_name }}
    - install_recommends: False
    - require:
      - file: bootstrap_repo

{{ salt_config_dir }}/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644
    - require:
      - pkg: salt-minion-package

{{ salt_config_dir }}/minion_id:
  file.managed:
    - contents_pillar: minion_id
    - require:
      - pkg: salt-minion-package

{% include 'bootstrap/remove_traditional_stack.sls' %}

mgr_update_basic_pkgs:
  pkg.latest:
    - pkgs:
      - openssl
{%- if grains['os_family'] == 'Suse' and grains['osrelease'] in ['11.3', '11.4'] and grains['cpuarch'] in ['i586', 'x86_64'] %}
      - pmtools
{%- elif grains['cpuarch'] in ['aarch64', 'x86_64'] %}
      - dmidecode
{%- endif %}
{%- if grains['os_family'] == 'Suse' %}
      - zypper
{%- elif grains['os_family'] == 'RedHat' %}
      - yum
{%- endif %}

# Manage minion key files in case they are provided in the pillar
{% if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
{{ salt_config_dir }}/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub
    - mode: 644
    - makedirs: True
    - require:
      - pkg: salt-minion-package

{{ salt_config_dir }}/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem
    - mode: 400
    - makedirs: True
    - require:
      - pkg: salt-minion-package

{{ salt_minion_name }}:
  service.running:
    - name: {{ salt_minion_name }}
    - enable: True
    - require:
      - pkg: salt-minion-package
      - host: mgr_server_localhost_alias_absent
    - watch:
      - file: {{ salt_config_dir }}/minion_id
      - file: {{ salt_config_dir }}/pki/minion/minion.pem
      - file: {{ salt_config_dir }}/pki/minion/minion.pub
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
{% else %}
{{ salt_minion_name }}:
  service.running:
    - enable: True
    - require:
      - pkg: salt-minion-package
      - host: mgr_server_localhost_alias_absent
    - watch:
      - file: {{ salt_config_dir }}/minion_id
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
{% endif %}
