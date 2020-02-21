{%- if grains['os_family'] == 'Suse' %}
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
