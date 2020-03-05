mgr_pool_{{ pillar['pool_state'] }}:
  module.run:
    - name: virt.pool_{{ pillar['pool_state'] }}
    - m_name: {{ pillar['pool_name'] }}
