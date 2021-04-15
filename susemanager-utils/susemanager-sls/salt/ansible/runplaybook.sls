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
#   "inventory": "/root/ansible-examples/lamp_simple/hosts"
# }
#

run_ansible_playbook:
  ansible.playbooks:
    - name: {{ pillar["playbook_path"] }}
    - rundir: {{ pillar["rundir"] }}
{%- if "inventory" in pillar %}
    - ansible_kwargs:
        inventory: {{ pillar["inventory"] }}
{% endif %}
