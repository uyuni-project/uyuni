# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

pkg_locked:
  pkg.held:
    - replace: True
{% if pillar.get('param_pkgs') %}
    - pkgs:
{%- for pkg, arch, version in pillar.get('param_pkgs', []) %}
        - {{ pkg }}
{%- endfor %}
{%- else %}
    - pkgs: []
{% endif %}
