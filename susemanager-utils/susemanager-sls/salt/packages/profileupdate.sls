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
  cmd.run:
    - name: cat /etc/redhat-release
    - onlyif: test -f /etc/redhat-release
centosrelease:
  cmd.run:
    - name: cat /etc/centos-release
    - onlyif: test -f /etc/centos-release
respkgquery:
  cmd.run:
    - name: rpm -q --whatprovides 'sles_es-release-server'
    - onlyif: rpm -q --whatprovides 'sles_es-release-server'
{% endif %}