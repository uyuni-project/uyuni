{% if pillar.get('group_ids', []) -%}
include:
{% for gid in pillar.get('group_ids', []) -%}
  - custom.group_{{ gid }}
{% endfor %}
{% endif %}
