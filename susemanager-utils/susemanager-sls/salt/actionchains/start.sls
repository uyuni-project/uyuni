start_action_chain:
    mgrcompat.module_run:
    -  name: mgractionchains.start
    -  actionchain_id: {{ pillar['action_chain_id']}}
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}

include:
  - util.syncstates