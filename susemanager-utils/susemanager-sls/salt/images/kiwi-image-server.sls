# Image Server installation state - part of SUSE Manager for Retail
#
# Copyright © 2017, 2018 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'kiwi_build_host' in pillar['addon_group_types'] %}
{% set kiwi_dir = '/var/lib/Kiwi' %}

mgr_install_kiwi:
  pkg.installed:
    - pkgs:
      - kiwi
{%- if pillar.get('use_build') %}
      - build
{%- endif %}
    - order: first

mgr_kiwi_build_tools:
  pkg.installed:
    - pkgs:
      - kiwi-desc-saltboot
      - image-server-tools
    - order: last

{{ kiwi_dir }}:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755

{{ kiwi_dir }}/images:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755

{{ kiwi_dir }}/chroot:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755

sync:
  module.run:
    - name: saltutil.sync_all

{% endif %}
