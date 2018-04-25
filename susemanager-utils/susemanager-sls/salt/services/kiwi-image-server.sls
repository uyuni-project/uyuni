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
