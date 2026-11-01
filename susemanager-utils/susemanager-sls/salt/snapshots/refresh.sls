# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

{%- if grains.get('transactional', False) %}
snapper-list-snapshots:
  cmd.run:
    - name: snapper --json --no-dbus list
{%- endif %}
