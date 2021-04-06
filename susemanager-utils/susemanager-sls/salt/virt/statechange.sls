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
