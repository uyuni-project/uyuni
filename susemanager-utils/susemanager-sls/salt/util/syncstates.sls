# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

sync_states:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_states
{%- elif salt['saltutil.sync_states']() or True %}
  module.run:
    - name: saltutil.sync_states
{%- endif %}

