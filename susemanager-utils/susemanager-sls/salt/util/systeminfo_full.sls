# Collect full system info for minion registration

include:
  - util.syncmodules
  - util.syncstates
  - util.syncgrains
  - util.syncbeacons
status_uptime:
  mgrcompat.module_run:
    - name: status.uptime
grains_update:
  mgrcompat.module_run:
    - name: grains.items

kernel_live_version:
  mgrcompat.module_run:
    - name: sumautil.get_kernel_live_version
