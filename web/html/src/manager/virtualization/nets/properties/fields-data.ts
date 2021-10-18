import * as data from "utils/data";

export const mapping = {
  bridge: {
    description: t(
      "Use an existing and configured bridge device. No iptables rule, IP address or DHCP/DNS service are added"
    ),
    bridge_name: true,
    interfaces: true,
    virtualport_types: ["open vSwitch", "MidoNet"],
    vlan_trunk: (model) => model.virtualport_type === "openvswitch",
  },
  nat: {
    description: t("Virtual network using the host IP routing stack and NAT"),
    addressing: true,
    mtu: true,
  },
  open: {
    description: t("Virtual network using the host IP routing stack, without NAT"),
    addressing: true,
    mtu: true,
  },
  route: {
    description: t("Virtual network using the host IP routing stack, without firewall rules and NAT"),
    addressing: true,
    mtu: true,
  },
  isolated: {
    description: t("Virtual network with no connection to other networks"),
    addressing: true,
    mtu: true,
  },
  macvtap: {
    description: t(
      "Network based on macvtap driver to use a pool of physical interfaces. " +
        "No possible communication between guest and host"
    ),
    bridge_name: false,
    type: ["vepa", "private", "passthrough"],
    pf: true,
    interfaces: true,
    virtualport_types: ["802.1Qbh"],
    vlan: (model) => model.macvtap_mode === "passthrough",
  },
  hostdev: {
    label: t("SR-IOV pool"),
    description: t("Pass through one device of a pool of SR-IOV Virtual Functions"),
    bridge_name: false,
    pf: true,
    vf: true,
    virtualport_types: ["802.1Qbh"],
    vlan: () => true,
  },
};

export function getValue(type, path, defaultValue) {
  return data.getValue(mapping[type] || {}, path, defaultValue);
}
