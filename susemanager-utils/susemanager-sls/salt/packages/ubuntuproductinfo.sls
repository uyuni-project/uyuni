{% if grains['os_family'] == 'Debian' %}
{% if grains['osfullname'] == 'Debian' %}
fake_debian_release:
  cmd.run:
    - name: echo "Debian" "$(cat /etc/os-release | grep ^VERSION= | cut -d '"' -f2 )" | tee /etc/debian-release
    - onlyif: test -f /etc/os-release
debianrelease:
  cmd.run:
    - name: cat /etc/debian-release
    - onlyif: test -f /etc/debian-release
{% elif grains['osfullname'] == 'Ubuntu' %}
fake_ubuntu_release:
  cmd.run:
    - name: echo "Ubuntu" "$(cat /etc/os-release | grep ^VERSION= | cut -d '"' -f2 )" | tee /etc/ubuntu-release
    - onlyif: test -f /etc/os-release
ubunturelease:
  cmd.run:
    - name: cat /etc/ubuntu-release
    - onlyif: test -f /etc/ubuntu-release
{% endif %}
respkgquery:
  cmd.run:
    - name: dpkg -S 'sles_es-release-server'
    - onlyif: dpkg -S 'sles_es-release-server'
{% endif %}
