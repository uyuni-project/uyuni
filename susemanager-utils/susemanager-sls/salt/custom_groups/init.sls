# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

{% if pillar.get('group_ids', []) -%}
include:
{% for gid in pillar.get('group_ids', []) -%}
  - custom.group_{{ gid }}
{% endfor %}
{% endif %}
