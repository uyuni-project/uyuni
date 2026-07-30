include:
  - cleanup_minion.common_cleanup
{% if grains.get('transactional', False) %}
  - cleanup_minion.transactional
{% else %}
  - cleanup_minion.standard
{% endif %}
