
{%- if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64', 'aarch64'] %}
mgr_install_dmidecode:
  pkg.installed:
{%- if grains['os_family'] == 'Suse' and grains['osrelease'] in ['11.3', '11.4'] %}
    - name: pmtools
{%- else %}
    - name: dmidecode
{%- endif %}
{%- endif %}

grains:
  module.run:
    - name: grains.items
cpuinfo:
  module.run:
    - name: status.cpuinfo
udev:
  module.run:
    - name: udev.exportdb
network-interfaces:
  module.run:
    - name: network.interfaces
network-ips:
  module.run:
    - name: sumautil.primary_ips
network-modules:
  module.run:
    - name: sumautil.get_net_modules

{% if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64'] %}
smbios-records-bios:
  module.run:
    - name: smbios.records
    - rec_type: 0
    - clean: False
smbios-records-system:
  module.run:
    - name: smbios.records
    - rec_type: 1
    - clean: False
smbios-records-baseboard:
  module.run:
    - name: smbios.records
    - rec_type: 2
    - clean: False
smbios-records-chassis:
  module.run:
    - name: smbios.records
    - rec_type: 3
    - clean: False
{% elif grains['cpuarch'] in ['s390', 's390x'] %}
mainframe-sysinfo:
  module.run:
    - name: mainframesysinfo.read_values
{% endif %}

{%- if grains['saltversioninfo'][0] >= 2018 %}
{% if 'network.fqdns' in salt %}
fqdns:
  module.run:
    - name: network.fqdns
{% endif%}
{%- endif%}
