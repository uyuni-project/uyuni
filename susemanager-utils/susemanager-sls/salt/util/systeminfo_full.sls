# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# Collect full system info for minion registration

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
    - name: grains.items

kernel_live_version:
  module.run:
    - name: sumautil.get_kernel_live_version
