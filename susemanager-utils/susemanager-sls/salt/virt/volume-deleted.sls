mgr_volume_deleted:
  mgrcompat.module_run:
    - name: virt.volume_delete
    - pool: {{ pillar['pool_name'] }}
    - volume: {{ pillar['volume_name'] }}

