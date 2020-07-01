mgr_network_{{ pillar['network_state'] }}:
  mgrcompat.module_run:
    - name: virt.network_{{ pillar['network_state'] }}
    - m_name: {{ pillar['network_name'] }}
