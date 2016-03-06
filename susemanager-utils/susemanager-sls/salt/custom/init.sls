include:
  - .custom_{{ grains['machine_id'] }}
{% for gid in pillar['group_ids'] %}
  - .group_{{ gid }}
{% endfor %}
  - .org_{{ pillar['org_id'] }}
