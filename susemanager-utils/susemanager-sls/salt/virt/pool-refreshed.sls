mgr_pool_refreshed:
  module.run:
    - name: virt.pool_refresh
    - m_name: {{ pillar['pool_name'] }}
