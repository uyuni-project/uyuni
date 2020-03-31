sync_modules:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_modules
{%- else %}
  module.run:
    - name: saltutil.sync_modules
{%- endif %}
