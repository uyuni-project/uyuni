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
{% elif grains['os_family'] == 'RedHat' %}
rhelrelease:
  module.run:
    - name: sumautil.cat
    - path: /etc/redhat-release
{% endif %}
