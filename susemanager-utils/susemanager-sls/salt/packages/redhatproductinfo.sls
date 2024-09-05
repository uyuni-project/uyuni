{% if grains['os_family'] == 'RedHat' %}
rhelrelease:
  cmd.run:
    - name: /usr/bin/cat /etc/redhat-release
    - onlyif: /usr/bin/test -f /etc/redhat-release -a ! -L /etc/redhat-release
alibabarelease:
  cmd.run:
    - name: /usr/bin/cat /etc/alinux-release
    - onlyif: /usr/bin/test -f /etc/alinux-release
centosrelease:
  cmd.run:
    - name: /usr/bin/cat /etc/centos-release
    - onlyif: /usr/bin/test -f /etc/centos-release
oraclerelease:
  cmd.run:
    - name: /usr/bin/cat /etc/oracle-release
    - onlyif: /usr/bin/test -f /etc/oracle-release
amazonrelease:
  cmd.run:
    - name: /usr/bin/cat /etc/system-release
    - onlyif: /usr/bin/test -f /etc/system-release && /usr/bin/grep -qi Amazon /etc/system-release
almarelease:
  cmd.run:
    - name: /usr/bin/cat /etc/almalinux-release
    - onlyif: /usr/bin/test -f /etc/almalinux-release
rockyrelease:
  cmd.run:
    - name: /usr/bin/cat /etc/rocky-release
    - onlyif: /usr/bin/test -f /etc/rocky-release
respkgquery:
  cmd.run:
    - name: /usr/bin/rpm -q --whatprovides 'sles_es-release-server'
    - onlyif: /usr/bin/rpm -q --whatprovides 'sles_es-release-server'
sllpkgquery:
  cmd.run:
    - name: /usr/bin/rpm -q --whatprovides 'sll-release'
    - onlyif: /usr/bin/rpm -q --whatprovides 'sll-release'
{% endif %}
