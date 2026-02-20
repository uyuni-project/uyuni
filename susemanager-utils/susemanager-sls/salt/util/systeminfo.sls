# Update system info on each minion start

include:
  - util.syncmodules
  - util.syncstates
  - util.syncgrains
  - util.syncbeacons
status_uptime:
  module.run:
    - name: status.uptime
grains_update:
  module.run:
    - name: grains.item
    - args:
      - kernelrelease
      - master

kernel_live_version:
  module.run:
    - name: sumautil.get_kernel_live_version
