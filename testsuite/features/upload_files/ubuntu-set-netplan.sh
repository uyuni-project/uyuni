#! /bin/bash
set -e

cat > /etc/netplan/01-netcfg.yaml <<END
network:
  version: 2
  renderer: networkd
  ethernets:
    ens3:
      dhcp4: no
      addresses: [ $(ip a s ens3 | grep "inet " | awk '{print $2}') ]
      nameservers:
        search: [ $(hostname -d) ]
    ens4:
      dhcp4: yes
END

netplan apply

systemctl restart systemd-networkd
