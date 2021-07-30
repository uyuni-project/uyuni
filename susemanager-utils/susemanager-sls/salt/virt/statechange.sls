{%- set vm_info = salt.virt_utils.vm_info(pillar['domain_name']) %}
{%- set cluster_id = vm_info[pillar['domain_name']].get('cluster_primitive') %}
{%- set crm_action = {
  'running': 'start',
  'stopped': 'stop',
}.get(pillar['domain_state']) %}
{%- if cluster_id and crm_action %}
crm resource {{ crm_action }} {{ cluster_id }}:
  cmd.run
{%- else %}
{{ pillar['domain_name'] }}:
  {%- if pillar['domain_state'] == 'running' %}
  virt_utils.vm_resources_running:
    - name: {{ pillar['domain_name'] }}
  virt.running:
    - require:
      - virt_utils: {{ pillar['domain_name'] }}
  {%- else %}
  virt.{{ pillar['domain_state'] }}
  {%- endif %}
{%- endif %}
