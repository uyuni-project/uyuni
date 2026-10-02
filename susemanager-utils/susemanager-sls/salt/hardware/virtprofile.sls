# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

{% set vm_info_available = 'virt.vm_info' in salt %}
mgr_virt_profile:
  module.run:
    - name: virt.vm_info
    - onlyif:
      - test '{{ vm_info_available }}' = 'True' && virsh list