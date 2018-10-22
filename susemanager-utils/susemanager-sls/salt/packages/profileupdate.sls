packages:
  module.run:
    - name: pkg.info_installed
{% if grains['os_family'] == 'Suse' %}
    - kwargs: {
          attr: 'arch,epoch,version,release,install_date_time_t',
{%- if grains.get('__suse_reserved_pkg_all_versions_support', False) %}
          errors: report,
          all_versions: true
{%- else %}
          errors: report
{%- endif %}
      }
{% endif %}
{% if grains['os_family'] == 'Suse' %}
products:
  module.run:
    - name: pkg.list_products
{% elif grains['os_family'] == 'RedHat' %}
{% include 'packages/redhatproductinfo.sls' %}
{% endif %}
{% elif grains['os_family'] == 'Debian' %}
ubunturelease:
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
      - module: sync_grains

{% if not pillar.get('imagename') %}
kernel_live_version:
  module.run:
    - name: sumautil.get_kernel_live_version
    - require:
      - module: sync_modules
{% endif %}
