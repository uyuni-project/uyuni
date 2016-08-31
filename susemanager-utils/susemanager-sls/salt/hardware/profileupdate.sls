grains:
  module.run:
    - name: grains.items
cpuinfo:
  module.run:
    - name: status.cpuinfo
udevdb:
  module.run:
    - name: udevdb.exportdb
network-interfaces:
  module.run:
    - name: network.interfaces
network-ips:
  module.run:
    - name: sumautil.primary_ips
network-modules:
  module.run:
    - name: sumautil.get_net_modules

{% if grains['cpuarch'] == 'x86_64' %}
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
{% endif %}

