include:
  - util.syncmodules
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
