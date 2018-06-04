packages:
  module.run:
    - name: pkg.info_installed
    - kwargs: {
          attr: 'arch,epoch,version,release,install_date_time_t',
          errors: report
      }
{% if grains['os_family'] == 'Suse' %}
products:
  module.run:
    - name: pkg.list_products
{% elif grains['os_family'] == 'RedHat' %}
{% include 'packages/redhatproductinfo.sls' %}
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
