include:
{% for gid in pillar['group_id'] %}
  - custom.group_{{ gid }}
{% endfor %}
