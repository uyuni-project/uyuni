sync_states:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_states
{%- else %}
  mgrcompat.module_run:
    - name: saltutil.sync_states
{%- endif %}

