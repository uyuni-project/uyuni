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

