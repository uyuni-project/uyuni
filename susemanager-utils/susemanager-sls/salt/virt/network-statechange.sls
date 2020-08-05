{% if pillar['network_state'] != 'delete' %}
mgr_network_{{ pillar['network_state'] }}:
  mgrcompat.module_run:
    - name: virt.network_{{ pillar['network_state'] }}
    - m_name: {{ pillar['network_name'] }}

{% else %}
mgr_network_stop:
  mgrcompat.module_run:
    - name: virt.network_stop
    - m_name: {{ pillar['network_name'] }}

mgr_network_delete:
  mgrcompat.module_run:
    - name: virt.network_undefine
    - m_name: {{ pillar['network_name'] }}
    - require:
        - mgrcompat: mgr_network_stop
{% endif %}
