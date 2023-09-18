include:
  - util.syncstates
  - .packages_{{ grains['machine_id'] }}

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

{%- if grains['os_family'] == 'Suse' and grains['instance_id'] is defined and "opensuse" not in grains['oscodename']|lower%}
{# install flavor check tool in cloud instances to be able to detect payg instances #}
mgr_install_flavor_check:
  pkg.installed:
    - name: python-instance-billing-flavor-check
    - require:
      - file: mgrchannels_*
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{%- endif %}
