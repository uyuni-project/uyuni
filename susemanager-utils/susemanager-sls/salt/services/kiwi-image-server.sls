# Image Server installation state - part of SUSE Manager for Retail
#
# Copyright © 2017, 2018 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'osimage_build_host' in pillar['addon_group_types'] %}
{% set kiwi_dir = '/var/lib/Kiwi' %}

mgr_install_kiwi:
  pkg.installed:
    - pkgs:
      - kiwi
    - order: first

mgr_kiwi_build_tools:
  pkg.installed:
    - pkgs:
      - kiwi-desc-saltboot
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
    - name: {{ kiwi_dir }}/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm
    - source: salt://images/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm

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
  module.run:
    - name: saltutil.sync_all

{% endif %}
