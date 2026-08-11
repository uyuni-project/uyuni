include:
  - cleanup_minion.common_cleanup
{% if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
{% if grains.get('transactional', False) %}
  - cleanup_minion.transactional
{% else %}
  - cleanup_minion.standard
{% endif %}
{% endif %}
