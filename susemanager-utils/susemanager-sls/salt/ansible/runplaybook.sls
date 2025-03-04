#
# SLS to trigger a playbook execution on an Ansible control node
#
# This SLS requires pillar data to render properly.
#
# Example (inventory is optional):
#
# pillar = {
#   "playbook_path": "/root/ansible-examples/lamp_simple/site.yml",
#   "rundir": "/root/ansible-examples/lamp_simple"
#   "inventory_path": "/root/ansible-examples/lamp_simple/hosts"
# }
#

run_ansible_playbook:
  mgrcompat.module_run:
    - name: ansible.playbooks
    - playbook: {{ pillar["playbook_path"] }}
    - rundir: {{ pillar["rundir"] }}
    - flush_cache: {{ pillar["flush_cache"] }}
{%- if "inventory_path" in pillar %}
    - inventory: {{ pillar["inventory_path"] }}
{% endif %}
