{% if pillar.get('actionchain_id', None) %}
start_action_chain:
  module.run:
    - name: mgractionchains.start
	- arg:
	  - {{ pillar['actionchain_id'] }}
{% endif %}
