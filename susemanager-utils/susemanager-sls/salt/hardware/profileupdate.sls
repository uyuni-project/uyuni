
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
  mgrcompat.module_run:
    - name: grains.items
cpuinfo:
  mgrcompat.module_run:
    - name: status.cpuinfo
udev:
  mgrcompat.module_run:
    - name: udev.exportdb
network-interfaces:
  mgrcompat.module_run:
    - name: network.interfaces
network-ips:
  mgrcompat.module_run:
    - name: sumautil.primary_ips
network-modules:
  mgrcompat.module_run:
    - name: sumautil.get_net_modules

{% if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64'] %}
smbios-records-bios:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 0
    - clean: False
smbios-records-system:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 1
    - clean: False
smbios-records-baseboard:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 2
    - clean: False
smbios-records-chassis:
  mgrcompat.module_run:
    - name: smbios.records
    - rec_type: 3
    - clean: False
{% elif grains['cpuarch'] in ['s390', 's390x'] %}
mainframe-sysinfo:
  mgrcompat.module_run:
    - name: mainframesysinfo.read_values
{% endif %}

{%- if grains['saltversioninfo'][0] >= 2018 %}
{% if 'network.fqdns' in salt %}
fqdns:
  module.run:
    - name: network.fqdns
{% endif%}
{%- endif%}
