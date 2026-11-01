# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

{% if pillar['org_id'] is defined %}
include:
  - custom.org_{{ pillar['org_id'] }}
{% endif %}
