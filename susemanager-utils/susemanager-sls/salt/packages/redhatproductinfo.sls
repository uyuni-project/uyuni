{% if grains['os_family'] == 'RedHat' %}
rhelrelease:
  cmd.run:
    - name: cat /etc/redhat-release
    - onlyif: test -f /etc/redhat-release -a ! -L /etc/redhat-release
alibabarelease:
  cmd.run:
    - name: cat /etc/alinux-release
    - onlyif: test -f /etc/alinux-release
centosrelease:
  cmd.run:
    - name: cat /etc/centos-release
    - onlyif: test -f /etc/centos-release
oraclerelease:
  cmd.run:
    - name: cat /etc/oracle-release
    - onlyif: test -f /etc/oracle-release
amazonrelease:
  cmd.run:
    - name: cat /etc/system-release
    - onlyif: test -f /etc/system-release && grep -qi Amazon /etc/system-release
almarelease:
  cmd.run:
    - name: cat /etc/almalinux-release
    - onlyif: test -f /etc/almalinux-release
rockylinux:
  cmd.run:
    - name: cat /etc/rocky-release
    - onlyif: test -f /etc/rocky-release    
respkgquery:
  cmd.run:
    - name: rpm -q --whatprovides 'sles_es-release-server'
    - onlyif: rpm -q --whatprovides 'sles_es-release-server'
{% endif %}
