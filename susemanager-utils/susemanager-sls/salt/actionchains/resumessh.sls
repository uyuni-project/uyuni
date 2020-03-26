resumessh:
    module.run:
    -   name: mgractionchains.resume
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - module: sync_modules
{%- endif %}

include:
  - util.synccustomall
