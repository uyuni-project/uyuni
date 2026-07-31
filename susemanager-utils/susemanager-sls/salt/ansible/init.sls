# Ansible Control Node prerequisities state
#
# Copyright (c) 2017 - 2026 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'ansible_control_node' in pillar['addon_group_types'] %}

{% set available_pkgs = salt['pkg.list_repo_pkgs']() %}
mgr_ansible_installed:
  {%- if 'ansible-core' in available_pkgs or 'ansible' in available_pkgs %}
  pkg.installed:
    - pkgs:
     {%- if 'ansible-core' in available_pkgs %}
      - ansible-core
     {%- endif %}
     {%- if 'ansible' in available_pkgs %}
      - ansible
     {%- endif %}
  {%- else %}
  test.fail_without_changes:
    - name: "No candidates for Ansible packages available for installation"
  {%- endif %}

{%- if 'ansible.targets' in salt %}
mgr_ansible_inventory_refresh:
  module.run:
    - name: event.send
    - tag: salt/beacon/{{ grains['id'] }}/inotify//etc/ansible/hosts
    - onlyif:
      - test -f /etc/ansible/hosts
{%- endif %}

{% endif %}
