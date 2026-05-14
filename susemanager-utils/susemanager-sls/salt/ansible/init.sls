# Ansible Control Node prerequisities state
#
# Copyright (c) 2017 - 2021 SUSE LLC

{% if pillar['addon_group_types'] is defined and 'ansible_control_node' in pillar['addon_group_types'] %}
{%- set ansible_installed = salt['pkg.info_installed']('ansible-core', 'ansible', attr='version', failhard=False) %}
{%- set ansible_installed = 'ansible-core' in ansible_installed or 'ansible' in ansible_installed %}
mgr_ansible_installed:
  pkg.installed:
    - pkgs:
{%- if 'ansible-core' in salt['pkg.list_repo_pkgs']() %}
      - ansible-core
{%- else %}
      - ansible
{%- endif %}
    - unless: test '{{ ansible_installed }}' = True
{% endif %}
