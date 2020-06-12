mgr_pool_refreshed:
  mgrcompat.module_run:
    - name: virt.pool_refresh
    - m_name: {{ pillar['pool_name'] }}
