# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

resumessh:
    module.run:
    -   name: mgractionchains.resume
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - module: sync_modules
{%- endif %}

include:
  - util.syncall
