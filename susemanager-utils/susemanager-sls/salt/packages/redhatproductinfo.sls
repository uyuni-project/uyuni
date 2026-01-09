{% if grains['os_family'] == 'RedHat' %}
rhelrelease:
  cmd.run:
    - name: command -p cat /etc/redhat-release
    - onlyif: command -p test -f /etc/redhat-release -a ! -L /etc/redhat-release
alibabarelease:
  cmd.run:
    - name: command -p cat /etc/alinux-release
    - onlyif: command -p test -f /etc/alinux-release
centosrelease:
  cmd.run:
    - name: command -p cat /etc/centos-release
    - onlyif: command -p test -f /etc/centos-release
oraclerelease:
  cmd.run:
    - name: command -p cat /etc/oracle-release
    - onlyif: command -p test -f /etc/oracle-release
amazonrelease:
  cmd.run:
    - name: command -p cat /etc/system-release
    - onlyif: command -p test -f /etc/system-release && command -p grep -qi Amazon /etc/system-release
almarelease:
  cmd.run:
    - name: command -p cat /etc/almalinux-release
    - onlyif: command -p test -f /etc/almalinux-release
rockyrelease:
  cmd.run:
    - name: command -p cat /etc/rocky-release
    - onlyif: command -p test -f /etc/rocky-release
respkgquery:
  cmd.run:
    - name: command -p rpm -q --whatprovides 'sles_es-release-server'
    - onlyif: command -p rpm -q --whatprovides 'sles_es-release-server'
sllpkgquery:
  cmd.run:
    - name: command -p rpm -q --whatprovides 'sll-release'
    - onlyif: command -p rpm -q --whatprovides 'sll-release'
{% endif %}
