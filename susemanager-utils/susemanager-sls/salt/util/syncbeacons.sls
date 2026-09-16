# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

sync_beacons:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_beacons
{%- else %}
  module.run:
    - name: saltutil.sync_beacons
{%- endif %}
