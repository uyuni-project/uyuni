# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

{% if pillar['addon_group_types'] is defined and 'container_build_host' in pillar['addon_group_types'] %}
mgr_docker_service:
  service.running:
    - name: docker
    - enable: True
{%- if not grains.get('transactional', False) %}
    - require:
      - pkg: mgr_install_docker
{%- endif %}
{% endif %}

{%- if not grains.get('transactional', False) %}
include:
  - services.docker_prereqs
{%- endif %}
