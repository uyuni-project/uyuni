# Image Server installation state - part of SUSE Manager for Retail
#
# Copyright (c) 2017 - 2021 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'osimage_build_host' in pillar['addon_group_types'] %}
{% set kiwi_dir = '/var/lib/Kiwi' %}

# on SLES11 and SLES12 use legacy Kiwi, use Kiwi NG elsewhere
{%- set use_kiwi_ng = not (salt['grains.get']('osfullname') == 'SLES' and salt['grains.get']('osmajorrelease')|int() < 15) %}
{%- set available_packages = salt['pkg.search']('kiwi').keys() %}

{%- if use_kiwi_ng %}
mgr_install_kiwi:
  pkg.installed:
    - pkgs:
      - python3-kiwi
{%- if 'kiwi-systemdeps-disk-images' in available_packages %}
      - kiwi-systemdeps-disk-images
      - kiwi-systemdeps-image-validation
      - kiwi-systemdeps-iso-media
{%- endif %}
{%- if 'kiwi-systemdeps-containers' in available_packages %}
      - kiwi-systemdeps-containers
{%- endif %}
      - kiwi-boot-descriptions
{%- else %}
{% set kiwi_boot_modules = ['kiwi-desc-netboot', 'kiwi-desc-saltboot', 'kiwi-desc-vmxboot', 'kiwi-desc-oemboot', 'kiwi-desc-isoboot'] %}

mgr_install_kiwi:
  pkg.installed:
    - pkgs:
      - kiwi
{% for km in kiwi_boot_modules %}
    {% if km in available_packages %}
      - {{ km }}
    {% endif %}
{% endfor %}
{% endif %}

mgr_kiwi_build_tools:
  pkg.installed:
    - pkgs:
      - git-core

mgr_kiwi_dir_created:
  file.directory:
    - name: {{ kiwi_dir }}
    - user: root
    - group: root
    - dir_mode: 755

# repo for common kiwi build needs - mainly RPM with SUSE Manager certificate
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

mgr_sshd_installed_enabled:
  pkg.installed:
    - name: openssh
  service.running:
    - name: sshd
    - enable: True

mgr_sshd_public_key_copied:
  file.append:
    - name: /root/.ssh/authorized_keys
    - source: salt://salt_ssh/mgr_ssh_id.pub
    - makedirs: True
    - require:
      - pkg: mgr_sshd_installed_enabled

mgr_saltutil_synced:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_all
{%- else %}
  mgrcompat.module_run:
    - name: saltutil.sync_all
{%- endif %}

{% endif %}
