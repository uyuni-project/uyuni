packages:
  module.run:
    - name: pkg.info_installed
    - kwargs: {
          attr: 'arch,epoch,version,release,install_date',
          errors: report
      }
{% if grains['os_family'] == 'Suse' %}
products:
  module.run:
    - name: pkg.list_products
{% endif %}

