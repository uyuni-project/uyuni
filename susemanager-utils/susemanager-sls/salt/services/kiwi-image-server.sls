#!jinja|yaml
# SUSE Multi-Linux Manager image server preparation
#
# Copyright (c) 2017 - 2025 SUSE LLC

{% if 'osimage_build_host' in pillar.get('addon_group_types', []) %}
{%- set kiwi_dir = '/var/lib/Kiwi' %}

mgr_kiwi_dir_created:
  file.directory:
    - name: {{ kiwi_dir }}
    - user: root
    - group: root
    - dir_mode: 755

# repo for common kiwi build needs - mainly RPM with SUSE Multi-Linux Manager certificate
mgr_kiwi_dir_repo_created:
  file.directory:
    - name: {{ kiwi_dir }}/repo
    - user: root
    - group: root
    - dir_mode: 755

mgr_osimage_cert_deployed:
  file.managed:
{%- if grains.get('osfullname') == 'SLES' and grains.get('osmajorrelease') == '11' %}
    - name: {{ kiwi_dir }}/repo/rhn-org-trusted-ssl-cert-osimage-sle11-1.0-1.noarch.rpm
    - source: salt://images/rhn-org-trusted-ssl-cert-osimage-sle11-1.0-1.noarch.rpm
{%- else %}
    - name: {{ kiwi_dir }}/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm
    - source: salt://images/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm
{%- endif %}

mgr_sshd_enabled:
  service.running:
    - name: sshd
    - enable: True

mgr_sshd_public_key_copied:
  file.append:
    - name: /root/.ssh/authorized_keys
    - source: salt://salt_ssh/mgr_ssh_id.pub
    - makedirs: True
{%- if not grains.get('transactional', False) %}
    - require:
      - pkg: mgr_sshd_installed
{%- endif %}

mgr_saltutil_synced:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_all
{%- else %}
  module.run:
    - name: saltutil.sync_all
{%- endif %}

{% endif %}

{%- if not grains.get('transactional', False) %}
include:
  - services.kiwi-image-server_prereqs
{%- endif %}
