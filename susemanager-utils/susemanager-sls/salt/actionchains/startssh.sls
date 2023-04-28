startssh:
    mgrcompat.module_run:
    -   name: mgractionchains.start
    -   actionchain_id: {{ pillar.get('actionchain_id')}}
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - mgrcompat: sync_modules
{%- endif %}

include:
  - util.syncall
