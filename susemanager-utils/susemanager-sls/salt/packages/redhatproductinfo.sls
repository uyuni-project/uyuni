{% if grains['os_family'] == 'RedHat' %}
rhelrelease:
  cmd.run:
    - name: cat /etc/redhat-release
    - onlyif: test -f /etc/redhat-release -a ! -L /etc/redhat-release
centosrelease:
  cmd.run:
    - name: cat /etc/centos-release
    - onlyif: test -f /etc/centos-release
oraclerelease:
  cmd.run:
    - name: cat /etc/oracle-release
    - onlyif: test -f /etc/oracle-release
respkgquery:
  cmd.run:
    - name: rpm -q --whatprovides 'sles_es-release-server'
    - onlyif: rpm -q --whatprovides 'sles_es-release-server'
{% endif %}
