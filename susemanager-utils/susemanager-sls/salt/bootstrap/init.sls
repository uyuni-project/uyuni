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

# disable all repos, except of repos flagged with keep:* (should be none)
{% set repos_disabled = {'match_str': 'keep:', 'matching': false} %}
{%- include 'channels/disablelocalrepos.sls' %}
{% do repos_disabled.update({'skip': true}) %}

{%- set transactional = grains['transactional'] %}

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
  {%- if transactional %}
    {% set os_base = os_base|string + 'micro' %}
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

{%- if not transactional %}
salt-minion-package:
  pkg.installed:
    - name: {{ salt_minion_name }}
    - install_recommends: False
    - require:
      - file: bootstrap_repo
{%- else %}
{# hack until transactional_update.run is fixed to use venv-salt-call #}
{# Writing  to the future - find latest etc overlay which was created for package installation and use that as etc root #}
{# this only works here in bootstrap when we are not running in transaction #}
{%- set pending_transaction_id = salt['cmd.run']('snapper --no-dbus list --columns=number | grep "+" | tr -d "+"', python_shell=True) %}
{%- if not pending_transaction_id %}
{#  if we did not get pending transaction id, write to current upperdir #}
{%- set pending_transaction_id = salt['cmd.run']('snapper --no-dbus list --columns number | grep "*" | tr -d "*"', python_shell=True) %}
{%- endif %}
{# increase transaction id by 1 since jinja is doing this before new transaction for package install is created #}
{# this is working under assumption there will be only one transaction between jinja render and actual package installation #}
{%- set pending_transaction_id = pending_transaction_id|int + 1 %}
{%- set salt_config_dir = '/var/lib/overlay/' + pending_transaction_id|string + salt_config_dir %}

salt-minion-package:
  mgrcompat.module_run:
    - name: transactional_update.pkg_install
    - pkg: {{ salt_minion_name }}
    - args: "--no-recommends"
    - require:
      - file: bootstrap_repo

{%- if not use_venv_salt %}
{# transactional_update executor module is required for classic salt-minion only #}
{# venv-salt-minion has its own venv executor module which invokes transactional_update if needed #}
{{ salt_config_dir }}/minion.d/transactional_update.conf:
  file.managed:
    - source:
      - salt://bootstrap/transactional_update.conf
    - template: jinja
    - mode: 644
    - makedirs: True
    - require:
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
{%- endif %}
{%- endif %}

{# We must install "python3-contextvars" on DEB based distros, running Salt 3004, with Python version < 3.7, like Ubuntu 18.04 #}
{# We cannot make this package a hard depedendency for Salt DEB package because this is only needed in Ubuntu 18.04 #}
{# DEB based distros with Python version >= 3.7 does not need this package - package is not existing in such cases #}
{# Since we only maintain a single DEB package for all DEB based distros, we need to explicitely install the package here #}
{%- set contextvars_needed = False %}
{%- if salt_minion_name == 'salt-minion' and grains['os_family'] == 'Debian' and grains['pythonversion'][0] >= 3 and grains['pythonversion'][1] < 7 %}
  {%- if not (grains['os'] == 'Ubuntu' and grains['osrelease_info'][0] == 16) and not (grains['os'] == 'Debian' and grains['osrelease_info'][0] == 9) %}
    {%- set contextvars_needed = True %}
  {%- endif %}
{%- endif %}

{% if contextvars_needed %}
salt-install-contextvars:
  pkg.installed:
    - name: python3-contextvars
    - install_recommends: False
    - require:
      - file: bootstrap_repo
      - pkg: salt-minion-package
{% endif %}

{{ salt_config_dir }}/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644
    - makedirs: True
    - require:
      - salt-minion-package

{{ salt_config_dir }}/minion_id:
  file.managed:
    - contents_pillar: minion_id
    - require:
      - salt-minion-package

{%- if not transactional %}
{% include 'bootstrap/remove_traditional_stack.sls' %}
{% else %}
include:
  - util.syncstates
{%- endif %}

# Manage minion key files in case they are provided in the pillar
{%- if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
{{ salt_config_dir }}/pki/minion/minion.pub:
  file.managed:
    - contents_pillar: minion_pub
    - mode: 644
    - makedirs: True
    - require:
      - salt-minion-package

{{ salt_config_dir }}/pki/minion/minion.pem:
  file.managed:
    - contents_pillar: minion_pem
    - mode: 400
    - makedirs: True
    - require:
      - salt-minion-package
{%- endif %}

{%- if not transactional %}
{{ salt_minion_name }}:
  service.running:
    - enable: True
    - require:
      - salt-minion-package
      - host: mgr_server_localhost_alias_absent
    - watch:
      - file: {{ salt_config_dir }}/minion_id
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
  {%- if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
      - file: {{ salt_config_dir }}/pki/minion/minion.pem
      - file: {{ salt_config_dir }}/pki/minion/minion.pub
  {%- endif %}
{%- else %}
{{ salt_minion_name }}:
  mgrcompat.module_run:
    - name: transactional_update.run
    - command: systemctl enable {{ salt_minion_name }}
    - snapshot: continue
    - require:
      - salt-minion-package
      - host: mgr_server_localhost_alias_absent
      - file: {{ salt_config_dir }}/minion_id
      - file: {{ salt_config_dir }}/minion.d/susemanager.conf
  {%- if pillar['minion_pub'] is defined and pillar['minion_pem'] is defined %}
      - file: {{ salt_config_dir }}/pki/minion/minion.pem
      - file: {{ salt_config_dir }}/pki/minion/minion.pub
  {%- endif %}

{# Change REBOOT_METHOD to systemd if it is default, otherwise don't change it #}

copy_transactional_conf_file_to_etc:
  file.copy:
    - name: /etc/transactional-update.conf
    - source: /usr/etc/transactional-update.conf
    - unless:
      - test -f /etc/transactional-update.conf

transactional_update_set_reboot_method_systemd:
  file.keyvalue:
    - name: /etc/transactional-update.conf
    - key_values:
        REBOOT_METHOD: 'systemd'
    - separator: '='
    - uncomment: '# '
    - append_if_not_found: True
    - require:
      - file: copy_transactional_conf_file_to_etc
    - unless:
      - grep -P '^(?=[\s]*+[^#])[^#]*(REBOOT_METHOD=(?!auto))' /etc/transactional-update.conf

{%- endif %}
