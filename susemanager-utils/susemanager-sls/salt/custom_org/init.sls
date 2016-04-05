{% if pillar['org_id'] is defined %}
include:
  - custom.org_{{ pillar['org_id'] }}
{% endif %}
