{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease']|int > 11 and not grains['oscodename'] == 'openSUSE Leap 15.3'%}
mgr_install_products:
  product.all_installed:
    - refresh: True
    - require:
      - file: mgrchannels_*
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{%- endif %}

include:
  - util.syncstates
  - .packages_{{ grains['machine_id'] }}
