mgr_pool_deleted:
  virt.pool_deleted:
    - name: {{ pillar['pool_name'] }}
    - purge: {{ pillar['pool_purge'] }}
