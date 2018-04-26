# Image Server installation state - part of SUSE Manager for Retail
#
# Copyright © 2017, 2018 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'osimage_build_host' in pillar['addon_group_types'] %}
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
      - git-core
    - order: last

{{ kiwi_dir }}:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755

# repo for common kiwi build needs - mainly RPM with SUSE Manager certificate
{{ kiwi_dir }}/repo:
  file.directory:
    - user: root
    - group: root
    - dir_mode: 755

# TODO - fix proper source
#{{ kiwi_dir }}/repo/kiwi_suma_mgr.rpm:
#  file.managed:
#    - source: salt://kiwi_suma_mgr.rpm

sshd:
  pkg.installed:
    - name: openssh
  service.running:
    - enable: True

ssh_public_key:
  file.append:
    - name: /root/.ssh/authorized_keys
    - source: salt://salt_ssh/mgr_ssh_id.pub
    - makedirs: True
    - require:
      - pkg: sshd
sync:
  module.run:
    - name: saltutil.sync_all

{% endif %}
