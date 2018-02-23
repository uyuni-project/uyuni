{% if pillar.get('actionchain_id', None) %}
start_action_chain:
  module.run:
    - name: mgractionchains.start
    - actionchain_id: {{ pillar['actionchain_id'] }}
{% endif %}
