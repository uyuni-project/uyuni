{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease']|int > 11 %}
mgr_install_products:
  product.all_installed:
    - refresh: True
    - require:
      - file: mgrchannels_*
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - module: sync_states
{%- endif %}
{%- endif %}

include:
  - util.syncstates
  - .packages_{{ grains['machine_id'] }}
