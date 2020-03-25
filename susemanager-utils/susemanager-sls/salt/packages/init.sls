{%- if grains['os_family'] == 'Suse' and grains['osmajorrelease']|int > 11 %}
mgr_install_products:
  product.all_installed:
    - refresh: True
    - require:
      - file: mgrchannels_*
      - module: sync_states
{%- endif %}

include:
  - util.syncstates
  - .packages_{{ grains['machine_id'] }}
