#!jinja|yaml
# SUSE Multi-Linux Manager image server preparation
#
# Copyright (c) 2017 - 2025 SUSE LLC

{% from "images/kiwi-detect.sls" import kiwi_method with context %}

{% if 'osimage_build_host' in pillar.get('addon_group_types', []) %}
{%- set kiwi_dir = '/var/lib/Kiwi' %}

{# Set correct package list base on SLES version but independent of kiwi_ng usage #}
{%- if kiwi_method == 'legacy' %}
{%-   set kiwi_modules = ['kiwi', 'kiwi-desc-netboot', 'kiwi-desc-saltboot', 'kiwi-desc-vmxboot', 'kiwi-desc-oemboot', 'kiwi-desc-isoboot'] %}
{%- elif kiwi_method == 'kiwi-ng' %}
{%-   if grains['osfullname'] == "SLES" and grains['osrelease'] in ['15.4', '15.5', '15.6', '15.7'] %}
{%-      set kiwi_modules = ['python311-kiwi', 'kiwi-systemdeps-disk-images', 'kiwi-systemdeps-image-validation', 'kiwi-systemdeps-iso-media', 'kiwi-systemdeps-containers', 'kiwi-boot-descriptions'] %}
{%-   else %}
{%-      set kiwi_modules = ['python3-kiwi', 'kiwi-systemdeps-disk-images', 'kiwi-systemdeps-image-validation', 'kiwi-systemdeps-iso-media', 'kiwi-systemdeps-containers', 'kiwi-boot-descriptions'] %}
{%-   endif %}
{%- elif kiwi_method == 'podman' %}
{#- TODO: add kiwi container rpm once available#}
{%-   set kiwi_modules = ['podman', 'xorriso'] %}
{%- else: %}
kiwi_unknown_method:
  test.fail_without_changes:
    - name: Unknown kiwi method {{ kiwi_method }}
{%- endif %}

mgr_install_kiwi:
  pkg.installed:
    - pkgs:
      - git-core
{%- for km in kiwi_modules %}
      - {{ km }}
{%- endfor %}

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
