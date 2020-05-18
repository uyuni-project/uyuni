packages:
  module.run:
    - name: pkg.info_installed
    - kwargs: {
          attr: 'status,arch,epoch,version,release,install_date_time_t',
{%- if grains.get('__suse_reserved_pkg_all_versions_support', False) %}
          errors: report,
          all_versions: true
{%- else %}
          errors: report
{%- endif %}
      }
{% if grains['os_family'] == 'Suse' %}
products:
  module.run:
    - name: pkg.list_products
{% elif grains['os_family'] == 'RedHat' %}
{% include 'packages/redhatproductinfo.sls' %}
{% elif grains['os_family'] == 'Debian' %}
debianrelease:
  cmd.run:
    - name: cat /etc/os-release
    - onlyif: test -f /etc/os-release
{% endif %}

include:
  - util.syncgrains
  - util.syncmodules

grains_update:
  module.run:
    - name: grains.items
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_grains
{%- else %}
      - module: sync_grains
{%- endif %}

{% if not pillar.get('imagename') %}
kernel_live_version:
  module.run:
    - name: sumautil.get_kernel_live_version
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
{%- else %}
      - module: sync_modules
{%- endif %}
{% endif %}
