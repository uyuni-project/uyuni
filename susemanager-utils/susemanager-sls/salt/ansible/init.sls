# Ansible Control Node prerequisities state
#
# Copyright (c) 2017 - 2021 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'ansible_control_node' in pillar['addon_group_types'] %}
mgr_ansible_installed:
  pkg.installed:
    - pkgs:
      - ansible

{% endif %}
