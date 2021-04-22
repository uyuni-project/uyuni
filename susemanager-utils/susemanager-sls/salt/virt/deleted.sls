{%- set vm_info = salt.virt_utils.vm_info(pillar['domain_name']) %}
{%- set cluster_id = vm_info[pillar['domain_name']].get('cluster_primitive') %}

{%- if cluster_id %}
{{ pillar['domain_name'] }}:
  virt_utils.cluster_vm_removed:
    - primitive: {{ cluster_id }}
    - definition_path: {{ vm_info[pillar['domain_name']]['definition_path'] }}

{%- else %}
vm_stopped:
  virt.powered_off:
    - name: {{ pillar['domain_name'] }}

mgr_virt_destroy:
  mgrcompat.module_run:
    - name: virt.purge
    - vm_: {{ pillar['domain_name'] }}
    - require:
      - virt: vm_stopped
{%- endif %}
