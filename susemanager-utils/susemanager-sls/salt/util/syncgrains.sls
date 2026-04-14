sync_grains:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_grains:
{%- else %}
  module.run:
    - name: saltutil.sync_grains
{%- endif %}
    - reload_grains: true
