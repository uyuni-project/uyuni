include:
  - cleanup_minion.common_cleanup
{% if grains.get('transactional', False) %}
  - cleanup_minion.cleanup_tu_minion
{% else %}
  - cleanup_minion.cleanup_non_tu_minion
{% endif %}
