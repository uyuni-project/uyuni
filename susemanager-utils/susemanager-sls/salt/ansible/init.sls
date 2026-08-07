include:
  - ansible.prereq

{%- if pillar['addon_group_types'] is defined and 'ansible_control_node' in pillar['addon_group_types'] %}
{%- if 'ansible.targets' in salt %}
mgr_ansible_inventory_refresh:
  module.run:
    - name: event.send
    - tag: salt/beacon/{{ grains['id'] }}/inotify//etc/ansible/hosts
    - onlyif:
      - test -f /etc/ansible/hosts
{%- endif %}
{% endif %}
