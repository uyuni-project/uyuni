sync_beacons:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
  saltutil.sync_beacons
{%- else %}
  mgrcompat.module_run:
    - name: saltutil.sync_beacons
{%- endif %}
