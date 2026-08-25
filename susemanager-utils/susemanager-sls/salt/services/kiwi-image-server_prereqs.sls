#!jinja|yaml
# SUSE Multi-Linux Manager image server prerequisites
#
# Copyright (c) 2017 - 2025 SUSE LLC

{% from "images/kiwi-detect.sls" import kiwi_method with context %}

{% if 'osimage_build_host' in pillar.get('addon_group_types', []) %}

{# Set correct package list based on SLES version but independent of kiwi_ng usage #}
{%- if kiwi_method == 'legacy' %}
{%-   set kiwi_modules = ['kiwi', 'kiwi-desc-netboot', 'kiwi-desc-saltboot', 'kiwi-desc-vmxboot', 'kiwi-desc-oemboot', 'kiwi-desc-isoboot'] %}
{%- elif kiwi_method == 'kiwi-ng' %}
{%-   if grains['osfullname'] == "SLES" and grains['osrelease'] in ['15.4', '15.5', '15.6', '15.7'] %}
{%-      set kiwi_modules = ['python311-kiwi', 'kiwi-systemdeps-disk-images', 'kiwi-systemdeps-image-validation', 'kiwi-systemdeps-iso-media', 'kiwi-systemdeps-containers', 'kiwi-boot-descriptions'] %}
{%-   else %}
{%-      set kiwi_modules = ['python3-kiwi', 'kiwi-systemdeps-disk-images', 'kiwi-systemdeps-image-validation', 'kiwi-systemdeps-iso-media', 'kiwi-systemdeps-containers', 'kiwi-boot-descriptions'] %}
{%-   endif %}
{%- elif kiwi_method == 'podman' %}
{#- TODO: add kiwi container rpm once available #}
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

mgr_sshd_installed:
  pkg.installed:
    - name: openssh

{% endif %}
