{% if pillar['network_state'] != 'delete' %}
mgr_network_{{ pillar['network_state'] }}:
  mgrcompat.module_run:
    - name: virt.network_{{ pillar['network_state'] }}
    - m_name: {{ pillar['network_name'] }}

{% else %}
  {%- set net_info = salt.virt.network_info(pillar['network_name'])[pillar['network_name']] %}
  {%- if net_info["active"] == 1 %}
mgr_network_stop:
  mgrcompat.module_run:
    - name: virt.network_stop
    - m_name: {{ pillar['network_name'] }}
  {%- endif %}

mgr_network_delete:
  mgrcompat.module_run:
    - name: virt.network_undefine
    - m_name: {{ pillar['network_name'] }}
  {%- if net_info["active"] == 1 %}
    - require:
        - mgrcompat: mgr_network_stop
  {%- endif %}
{% endif %}
