import * as React from "react";
import _cloneDeep from "lodash/cloneDeep";
import {
  render,
  waitForElementToBeRemoved,
  screen,
  server,
  type,
  click,
  select,
  getFieldValuesByName,
  fireEvent,
} from "utils/test-utils";

import { NetworkProperties } from "./network-properties";

function fieldValuesByName(name: string) {
  return getFieldValuesByName("network properties", name);
}

let onSubmit;

beforeEach(() => {
  onSubmit = () => {};
});

async function renderWithNetwork(net = undefined) {
  server.mockGetJson("/rhn/manager/api/systems/details/virtualization/nets/fakeserverid/devices", [
    { name: "eth4", address: "42:8a:c6:98:8d:00", state: "down", VF: true, "PCI address": "0000:3d:02.6", PF: false },
    { name: "eth6", address: "76:fe:ce:4f:73:22", state: "down", VF: true, "PCI address": "0000:3d:02.4", PF: false },
    { name: "eth5", address: "e2:58:67:95:a3:a3", state: "down", VF: true, "PCI address": "0000:3d:02.5", PF: false },
    { name: "eth0", address: "a4:bf:01:1d:27:88", state: "up", VF: false, PF: true },
    { name: "virbr0-nic", address: "52:54:00:ae:52:36", state: "down" },
    { name: "eth8", address: "e6:86:48:46:c5:29", state: "down", VF: true, "PCI address": "0000:3d:02.2", PF: false },
    { name: "eth1", address: "a4:bf:01:1d:27:89", state: "down", VF: false, PF: false },
    { name: "eth7", address: "2e:ee:7b:ff:92:6a", state: "down", VF: true, "PCI address": "0000:3d:02.3", PF: false },
    { name: "eth2", address: "72:98:05:69:58:98", state: "down", VF: true, "PCI address": "0000:3d:02.1", PF: false },
  ]);

  const result = render(
    <NetworkProperties
      serverId={"fakeserverid"}
      submitText={t("Submit")}
      submit={onSubmit}
      initialModel={net}
      messages={[]}
      timezone="CET"
      localTime="2021-02-23T14:42+01:00"
      actionChains={[]}
    />
  );
  await waitForElementToBeRemoved(() => screen.queryByText("Loading..."));
  return result;
}

describe("Rendering", () => {
  test("Render with minimal properties", async () => {
    await renderWithNetwork();
    expect(fieldValuesByName("type")).toStrictEqual(["bridge"]);
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeTruthy();
  });

  test("Create bridge network", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        name: "bridge0",
        type: "bridge",
        bridge: "br0",
        autostart: true,
      });
      done();
    };

    await renderWithNetwork();
    expect(fieldValuesByName("type")).toStrictEqual(["bridge"]);
    await type(screen.getByLabelText("Name"), "bridge0");
    await type(screen.getByLabelText("Bridge"), "br0");
    click(screen.getByLabelText("Start during virtual host boot"));
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create NAT network", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        name: "nat0",
        type: "nat",
        mtu: 7000,
        nat: {
          address: { start: "192.168.10.3", end: "192.168.10.4" },
          port: { start: 1234, end: 1236 },
        },
        ipv4: {
          address: "192.168.10.0",
          prefix: 24,
        },
      });
      done();
    };

    await renderWithNetwork();
    await select(screen.getByLabelText("Network type"), "nat");
    await type(screen.getByLabelText("Name"), "nat0");
    await type(screen.getByLabelText("Maximum Transmission Unit (MTU)"), "7000");
    const ipv4_address = await screen.findByTitle("IPv4 Network address");
    await type(ipv4_address, "192.168.10.0");
    await type(screen.getByTitle("IPv4 Network address prefix"), "24");
    await type(screen.getByTitle("NAT IPv4 range start"), "192.168.10.3");
    await type(screen.getByTitle("NAT IPv4 range end"), "192.168.10.4");
    await type(screen.getByTitle("NAT port range start"), "1234");
    await type(screen.getByTitle("NAT port range end"), "1236");
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create network with all addressing fields", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        type: "open",
        bridge: "virbr2",
        name: "open0",
        domain: "tf.local",
        ipv4: {
          address: "192.168.10.0",
          prefix: 24,
          dhcpranges: [
            { start: "192.168.10.10", end: "192.168.10.20" },
            { start: "192.168.10.110", end: "192.168.10.120" },
          ],
          hosts: [
            { mac: "2A:C3:A7:A6:01:00", name: "dev-srv", ip: "192.168.10.2" },
            { mac: "2A:C3:A7:A6:01:01", ip: "192.168.10.3" },
          ],
          bootpfile: "pxelinux.0",
          bootpserver: "192.168.10.2",
          tftp: "/path/to/tftproot",
        },
        ipv6: {
          address: "2001:db8:ac10:fd01::",
          prefix: 64,
          dhcpranges: [{ start: "2001:db8:ac10:fd01::10", end: "2001:db8:ac10:fd01::20" }],
          hosts: [{ id: "0:3:0:1:0:16:3e:11:22:33", name: "peter.xyz", ip: "2001:db8:ac10:fd01::2" }],
        },
        dns: {
          hosts: [{ address: "192.168.10.1", names: ["host", "gateway"] }],
          srvs: [
            {
              name: "srv1",
              protocol: "tcp",
              domain: "test-domain-name.com",
              target: "test.example.com",
              port: 1111,
              priority: 11,
              weight: 111,
            },
            {
              name: "srv2",
              protocol: "udp",
            },
          ],
          txts: [
            { name: "example", value: "foo" },
            { name: "bar", value: "other value" },
          ],
          forwarders: [
            { address: "8.8.4.4" },
            { address: "192.168.1.1", domain: "example.com" },
            { domain: "acme.com" },
          ],
        },
      });
      done();
    };

    await renderWithNetwork();
    await select(screen.getByLabelText("Network type"), "open");
    await type(screen.getByLabelText("Name"), "open0");
    await type(screen.getByLabelText("Bridge"), "virbr2");
    await type(screen.getByLabelText("Domain name"), "tf.local");

    // IPv4 fields setting
    const ipv4_address = await screen.findByTitle("IPv4 Network address");
    await type(ipv4_address, "192.168.10.0");
    await type(screen.getByTitle("IPv4 Network address prefix"), "24");

    click(screen.getByTitle("Add DHCP Ranges"));
    const dhcp0_start = await screen.findByTitle("DHCP address range 0 start");
    await type(dhcp0_start, "192.168.10.10");
    await type(screen.getByTitle("DHCP address range 0 end"), "192.168.10.20");
    click(screen.getByTitle("Add DHCP Ranges"));
    const dhcp1_start = await screen.findByTitle("DHCP address range 1 start");
    await type(dhcp1_start, "192.168.10.110");
    await type(screen.getByTitle("DHCP address range 1 end"), "192.168.10.120");
    click(screen.getByTitle("Add DHCP Hosts"));
    const dhcp0_host = await screen.findByTitle("DHCP host 0 address");
    await type(dhcp0_host, "192.168.10.2");
    await type(screen.getByTitle("DHCP host 0 MAC address"), "2A:C3:A7:A6:01:00");
    await type(screen.getByTitle("DHCP host 0 name"), "dev-srv");
    click(screen.getByTitle("Add DHCP Hosts"));
    const dhcp1_host = await screen.findByTitle("DHCP host 1 address");
    await type(dhcp1_host, "192.168.10.3");
    await type(screen.getByTitle("DHCP host 1 MAC address"), "2A:C3:A7:A6:01:01");
    await type(screen.getByLabelText("BOOTP image file"), "pxelinux.0");
    await type(screen.getByLabelText("BOOTP server"), "192.168.10.2");
    await type(screen.getByLabelText("TFTP root path"), "/path/to/tftproot");

    // IPv6 fields checks
    click(screen.getByText("Enable IPv6"));
    const ipv6_address = await screen.findByTitle("IPv6 Network address");
    await type(ipv6_address, "2001:db8:ac10:fd01::");
    await type(screen.getByRole("textbox", { name: "IPv6 Network address prefix" }), "64");
    click(screen.getByTitle("Add DHCPv6 Ranges"));
    const dhcpv6_range0 = await screen.findByTitle("DHCPv6 address range 0 start");
    await type(dhcpv6_range0, "2001:db8:ac10:fd01::10");
    await type(screen.getByTitle("DHCPv6 address range 0 end"), "2001:db8:ac10:fd01::20");
    click(screen.getByTitle("Add DHCPv6 Hosts"));
    const dhcpv6_host0 = await screen.findByTitle("DHCPv6 host 0 address");
    await type(dhcpv6_host0, "2001:db8:ac10:fd01::2");
    await type(screen.getByTitle("DHCPv6 host 0 DUID"), "0:3:0:1:0:16:3e:11:22:33");
    await type(screen.getByTitle("DHCPv6 host 0 name"), "peter.xyz");

    // DNS fields checks
    click(screen.getByTitle("Add Forwarders"));
    const fwd0 = await screen.findByTitle("DNS forwarder 0 address");
    await type(fwd0, "8.8.4.4");
    click(screen.getByTitle("Add Forwarders"));
    const fwd1 = await screen.findByTitle("DNS forwarder 1 domain name");
    await type(fwd1, "example.com");
    await type(screen.getByTitle("DNS forwarder 1 address"), "192.168.1.1");
    click(screen.getByTitle("Add Forwarders"));
    const fwd2 = await screen.findByTitle("DNS forwarder 2 domain name");
    await type(fwd2, "acme.com");
    click(screen.getByTitle("Add Hosts"));
    const dns_host0 = await screen.findByTitle("DNS host 0 names");
    await type(dns_host0, "host,gateway");
    await type(screen.getByTitle("DNS host 0 address"), "192.168.10.1");
    click(screen.getByTitle("Add SRV records"));
    const srv0 = await screen.findByTitle("DNS SRV record 0 service");
    await type(srv0, "srv1");
    await select(screen.getByLabelText("DNS SRV record 0 protocol"), "TCP");
    await type(screen.getByTitle("DNS SRV record 0 domain name"), "test-domain-name.com");
    await type(screen.getByTitle("DNS SRV record 0 target hostname"), "test.example.com");
    await type(screen.getByTitle("DNS SRV record 0 port"), "1111");
    await type(screen.getByTitle("DNS SRV record 0 priority"), "11");
    await type(screen.getByTitle("DNS SRV record 0 weight"), "111");
    click(screen.getByTitle("Add SRV records"));
    const srv1 = await screen.findByTitle("DNS SRV record 1 service");
    await type(srv1, "srv2");
    await select(screen.getByLabelText("DNS SRV record 1 protocol"), "UDP");
    click(screen.getByTitle("Add TXT records"));
    const txt0 = await screen.findByTitle("DNS TXT record 0 name");
    await type(txt0, "example");
    await type(screen.getByTitle("DNS TXT record 0 value"), "foo");
    click(screen.getByTitle("Add TXT records"));
    const txt1 = await screen.findByTitle("DNS TXT record 1 name");
    await type(txt1, "bar");
    await type(screen.getByTitle("DNS TXT record 1 value"), "other value");

    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create openVSwitch bridge network", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        type: "bridge",
        autostart: true,
        bridge: "ovsbr0",
        name: "ovs0",
        virtualport: { type: "openvswitch", interfaceid: "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f" },
        vlantrunk: true,
        vlans: [{ tag: 42 }, { tag: 47 }],
      });
      done();
    };

    await renderWithNetwork();
    expect(fieldValuesByName("type")).toStrictEqual(["bridge"]);
    fireEvent.change(screen.getByLabelText("Name"), { target: { value: "ovs0" } });
    fireEvent.change(screen.getByLabelText("Bridge"), { target: { value: "ovsbr0" } });
    click(screen.getByLabelText("Start during virtual host boot"));

    await select(screen.getByLabelText("Virtual Port Type"), "open vSwitch");
    const iface_id = await screen.findByLabelText(/^Interface id/);
    await type(iface_id, "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f");
    click(screen.getByTitle("Add VLANs"));
    const vlan0_tag = await screen.findByTitle("VLAN 0 tag");
    await type(vlan0_tag, "42");
    click(screen.getByTitle("Add VLANs"));
    const vlan1_tag = await screen.findByTitle("VLAN 1 tag");
    await type(vlan1_tag, "47");
    expect(screen.getByLabelText<HTMLInputElement>("VLAN tags trunking").checked).toBeTruthy();

    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create macvtap passthrough network with interfaces", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        type: "macvtap",
        macvtapmode: "passthrough",
        name: "passthrough0",
        virtualport: { type: "802.1qbh", profileid: "testprofile" },
        interfaces: ["eth7", "eth8"],
      });
      done();
    };

    await renderWithNetwork();
    await select(screen.getByLabelText("Network type"), "macvtap");
    const macvtap_mode = await screen.findByLabelText(/^Macvtap mode/);
    await select(macvtap_mode, "passthrough");
    await type(screen.getByLabelText("Name"), "passthrough0");

    await select(screen.getByLabelText("Virtual Port Type"), "802.1Qbh");
    const profile_id = await screen.findByLabelText(/^Profile id/);
    expect(screen.getByLabelText<HTMLInputElement>("By profile id").checked).toBeTruthy();
    await type(profile_id, "testprofile");
    expect(screen.getByLabelText<HTMLInputElement>("By interfaces").checked).toBeTruthy();
    await select(screen.getByLabelText("Interfaces"), "eth7");
    await select(screen.getByLabelText("Interfaces"), "eth8");

    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create macvtap private network with physical function", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        type: "macvtap",
        macvtapmode: "private",
        name: "private0",
        virtualport: {
          type: "802.1qbh",
          managerid: "mgrid",
          typeid: "testtype",
          typeidversion: "testversion",
          instanceid: "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f",
        },
        pf: "eth0",
      });
      done();
    };

    await renderWithNetwork();
    await type(screen.getByLabelText("Name"), "private0");
    await select(screen.getByLabelText("Network type"), "macvtap");
    const macvtap_mode = await screen.findByLabelText(/^Macvtap mode/);
    await select(macvtap_mode, "private");

    await select(screen.getByLabelText("Virtual Port Type"), "802.1Qbh");
    const by_vsi = await screen.findByLabelText("Virtual Station Interface (VSI) parameters");
    click(by_vsi);
    const mgr_id = await screen.findByLabelText(/^VSI manager id/);
    await type(mgr_id, "mgrid");
    await type(screen.getByLabelText("VSI type id"), "testtype");
    await type(screen.getByLabelText("VSI type id version"), "testversion");
    await type(screen.getByLabelText("VSI instance id"), "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f");
    click(screen.getByLabelText("By physical function"));
    const pf = await screen.getByLabelText("Physical Function");
    await select(pf, "eth0");

    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Create hostdev network with virtual functions", async (done) => {
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual({
        type: "hostdev",
        name: "host0",
        vf: ["0000:3d:02.3", "0000:3d:02.2"],
        vlans: [{ tag: 24 }],
      });
      done();
    };

    await renderWithNetwork();
    await select(screen.getByLabelText("Network type"), "SR-IOV pool");
    await type(screen.getByLabelText("Name"), "host0");

    const by_vf = await screen.findByLabelText<HTMLInputElement>("By virtual functions");
    expect(by_vf.checked).toBeTruthy();
    await select(screen.getByLabelText("Virtual Functions"), "eth7");
    await select(screen.getByLabelText("Virtual Functions"), "eth8");
    await type(screen.getByLabelText("VLAN tag"), "24");

    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });
});

describe("Network properties loading", () => {
  function makeNetworkData(data) {
    const empty = {
      type: null,
      autostart: false,
      bridge: null,
      mtu: null,
      nat: null,
      ipv4: null,
      ipv6: null,
      domain: null,
      dns: null,
      interfaces: [],
      vf: [],
      pf: null,
      virtualport: null,
      vlantrunk: null,
      vlans: [],
      name: null,
      uuid: null,
    };
    return Object.assign(empty, _cloneDeep(data));
  }

  test("Render minimal NAT network", async (done) => {
    const net = {
      type: "nat",
      autostart: true,
      bridge: "virbr2",
      mtu: 7000,
      name: "private0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      ipv4: {
        address: "192.168.10.0",
        prefix: 24,
      },
    };

    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["nat"]);
    expect(screen.getByLabelText<HTMLInputElement>("Start during virtual host boot").checked).toBeTruthy();
    expect(screen.getByLabelText<HTMLInputElement>("Bridge").value).toStrictEqual("virbr2");
    expect(screen.getByLabelText<HTMLInputElement>("Maximum Transmission Unit (MTU)").value).toStrictEqual("7000");
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv4 Network address" }).value).toStrictEqual(
      "192.168.10.0"
    );
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv4 Network address prefix" }).value).toStrictEqual(
      "24"
    );
    expect(screen.getByLabelText<HTMLInputElement>("Enable IPv6").checked).toBeFalsy();

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Render network with all addressing fields", async (done) => {
    const net = {
      type: "open",
      bridge: "virbr2",
      name: "open0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      domain: "tf.local",
      ipv4: {
        address: "192.168.10.0",
        prefix: 24,
        dhcpranges: [
          { start: "192.168.10.10", end: "192.168.10.20" },
          { start: "192.168.10.110", end: "192.168.10.120" },
        ],
        hosts: [
          { mac: "2A:C3:A7:A6:01:00", name: "dev-srv", ip: "192.168.10.2" },
          { mac: "2A:C3:A7:A6:01:01", ip: "192.168.10.3" },
        ],
        bootpfile: "pxelinux.0",
        bootpserver: "192.168.10.2",
        tftp: "/path/to/tftproot",
      },
      ipv6: {
        address: "2001:db8:ac10:fd01::",
        prefix: 64,
        dhcpranges: [{ start: "2001:db8:ac10:fd01::10", end: "2001:db8:ac10:fd01::20" }],
        hosts: [{ id: "0:3:0:1:0:16:3e:11:22:33", name: "peter.xyz", ip: "2001:db8:ac10:fd01::2" }],
      },
      dns: {
        hosts: [{ address: "192.168.10.1", names: ["host", "gateway"] }],
        srvs: [
          {
            name: "srv1",
            protocol: "tcp",
            domain: "test-domain-name.com",
            target: "test.example.com",
            port: 1111,
            priority: 11,
            weight: 111,
          },
          {
            name: "srv2",
            protocol: "udp",
          },
        ],
        txts: [
          { name: "example", value: "foo" },
          { name: "bar", value: "other value" },
        ],
        forwarders: [{ address: "8.8.4.4" }, { address: "192.168.1.1", domain: "example.com" }, { domain: "acme.com" }],
      },
    };

    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["open"]);
    expect(screen.getByLabelText<HTMLInputElement>("Start during virtual host boot").checked).toBeFalsy();
    expect(screen.getByLabelText<HTMLInputElement>("Bridge").value).toStrictEqual("virbr2");
    expect(screen.getByLabelText<HTMLInputElement>("Maximum Transmission Unit (MTU)").value).toStrictEqual("");
    expect(screen.getByLabelText<HTMLInputElement>("Domain name").value).toStrictEqual("tf.local");

    // IPv4 fields checks
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv4 Network address" }).value).toStrictEqual(
      "192.168.10.0"
    );
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv4 Network address prefix" }).value).toStrictEqual(
      "24"
    );
    expect(screen.getByTitle<HTMLInputElement>("DHCP address range 0 start").value).toStrictEqual("192.168.10.10");
    expect(screen.getByTitle<HTMLInputElement>("DHCP address range 0 end").value).toStrictEqual("192.168.10.20");
    expect(screen.getByTitle<HTMLInputElement>("DHCP address range 1 start").value).toStrictEqual("192.168.10.110");
    expect(screen.getByTitle<HTMLInputElement>("DHCP address range 1 end").value).toStrictEqual("192.168.10.120");
    expect(screen.getByTitle<HTMLInputElement>("DHCP host 0 address").value).toStrictEqual("192.168.10.2");
    expect(screen.getByTitle<HTMLInputElement>("DHCP host 0 MAC address").value).toStrictEqual("2A:C3:A7:A6:01:00");
    expect(screen.getByTitle<HTMLInputElement>("DHCP host 0 name").value).toStrictEqual("dev-srv");
    expect(screen.getByTitle<HTMLInputElement>("DHCP host 1 address").value).toStrictEqual("192.168.10.3");
    expect(screen.getByTitle<HTMLInputElement>("DHCP host 1 MAC address").value).toStrictEqual("2A:C3:A7:A6:01:01");
    expect(screen.getByLabelText<HTMLInputElement>("BOOTP image file").value).toStrictEqual("pxelinux.0");
    expect(screen.getByLabelText<HTMLInputElement>("BOOTP server").value).toStrictEqual("192.168.10.2");
    expect(screen.getByLabelText<HTMLInputElement>("TFTP root path").value).toStrictEqual("/path/to/tftproot");

    // IPv6 fields checks
    expect(screen.getByLabelText<HTMLInputElement>("Enable IPv6").checked).toBeTruthy();
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv6 Network address" }).value).toStrictEqual(
      "2001:db8:ac10:fd01::"
    );
    expect(screen.getByRole<HTMLInputElement>("textbox", { name: "IPv6 Network address prefix" }).value).toStrictEqual(
      "64"
    );
    expect(screen.getByTitle<HTMLInputElement>("DHCPv6 address range 0 start").value).toStrictEqual(
      "2001:db8:ac10:fd01::10"
    );
    expect(screen.getByTitle<HTMLInputElement>("DHCPv6 address range 0 end").value).toStrictEqual(
      "2001:db8:ac10:fd01::20"
    );
    expect(screen.getByTitle<HTMLInputElement>("DHCPv6 host 0 address").value).toStrictEqual("2001:db8:ac10:fd01::2");
    expect(screen.getByTitle<HTMLInputElement>("DHCPv6 host 0 DUID").value).toStrictEqual("0:3:0:1:0:16:3e:11:22:33");
    expect(screen.getByTitle<HTMLInputElement>("DHCPv6 host 0 name").value).toStrictEqual("peter.xyz");

    // DNS fields checks
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 0 domain name").value).toStrictEqual("");
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 0 address").value).toStrictEqual("8.8.4.4");
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 1 domain name").value).toStrictEqual("example.com");
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 1 address").value).toStrictEqual("192.168.1.1");
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 2 domain name").value).toStrictEqual("acme.com");
    expect(screen.getByTitle<HTMLInputElement>("DNS forwarder 2 address").value).toStrictEqual("");
    expect(screen.getByTitle<HTMLInputElement>("DNS host 0 names").value).toStrictEqual("host,gateway");
    expect(screen.getByTitle<HTMLInputElement>("DNS host 0 address").value).toStrictEqual("192.168.10.1");
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 0 service").value).toStrictEqual("srv1");
    expect(fieldValuesByName("dns_srvs0_protocol")).toStrictEqual(["tcp"]);
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 0 domain name").value).toStrictEqual(
      "test-domain-name.com"
    );
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 0 target hostname").value).toStrictEqual(
      "test.example.com"
    );
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 0 port").value).toStrictEqual("1111");
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 0 priority").value).toStrictEqual("11");
    expect(screen.getByTitle<HTMLInputElement>("DNS SRV record 1 service").value).toStrictEqual("srv2");
    expect(fieldValuesByName("dns_srvs1_protocol")).toStrictEqual(["udp"]);
    expect(screen.getByTitle<HTMLInputElement>("DNS TXT record 0 name").value).toStrictEqual("example");
    expect(screen.getByTitle<HTMLInputElement>("DNS TXT record 0 value").value).toStrictEqual("foo");
    expect(screen.getByTitle<HTMLInputElement>("DNS TXT record 1 name").value).toStrictEqual("bar");
    expect(screen.getByTitle<HTMLInputElement>("DNS TXT record 1 value").value).toStrictEqual("other value");

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Render openVSwitch network", async (done) => {
    const net = {
      type: "bridge",
      autostart: true,
      bridge: "ovsbr0",
      name: "ovs0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      virtualport: { type: "openvswitch", interfaceid: "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f" },
      vlantrunk: true,
      vlans: [{ tag: 42, native: "untagged" }, { tag: 47 }],
    };

    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["bridge"]);
    expect(screen.getByLabelText<HTMLInputElement>("Bridge").value).toStrictEqual("ovsbr0");
    expect(fieldValuesByName("virtualport_type")).toStrictEqual(["openvswitch"]);
    expect(screen.getByLabelText<HTMLInputElement>("Interface id").value).toStrictEqual(
      "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f"
    );
    expect(screen.getByLabelText<HTMLInputElement>("VLAN tags trunking").checked).toBeTruthy();
    expect(screen.getByTitle<HTMLInputElement>("VLAN 0 tag").value).toStrictEqual("42");
    expect(fieldValuesByName("vlans0_native")).toStrictEqual(["untagged"]);
    expect(screen.getByTitle<HTMLInputElement>("VLAN 1 tag").value).toStrictEqual("47");
    expect(fieldValuesByName("vlans1_native")).toStrictEqual([""]);

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Render macvtap passthrough network with interfaces", async (done) => {
    const net = {
      type: "macvtap",
      macvtapmode: "passthrough",
      name: "passthrough0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      virtualport: { type: "802.1qbh", profileid: "testprofile" },
      interfaces: ["eth7", "eth8"],
    };

    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["macvtap"]);
    expect(fieldValuesByName("macvtapmode")).toStrictEqual(["passthrough"]);
    expect(fieldValuesByName("virtualport_type")).toStrictEqual(["802.1qbh"]);
    expect(screen.getByLabelText<HTMLInputElement>("By profile id").checked).toBeTruthy();
    expect(screen.getByLabelText<HTMLInputElement>("Profile id").value).toStrictEqual("testprofile");
    expect(screen.getByLabelText<HTMLInputElement>("By interfaces").checked).toBeTruthy();
    expect(fieldValuesByName("interfaces")).toStrictEqual(["eth7", "eth8"]);

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Render macvtap private network with physical function", async (done) => {
    const net = {
      type: "macvtap",
      macvtapmode: "private",
      name: "private0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      virtualport: {
        type: "802.1qbh",
        managerid: "mgrid",
        typeid: "testtype",
        typeidversion: "testversion",
        instanceid: "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f",
      },
      pf: "eth0",
    };
    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["macvtap"]);
    expect(fieldValuesByName("macvtapmode")).toStrictEqual(["private"]);
    expect(fieldValuesByName("virtualport_type")).toStrictEqual(["802.1qbh"]);
    expect(screen.getByLabelText<HTMLInputElement>("Virtual Station Interface (VSI) parameters").checked).toBeTruthy();
    expect(screen.getByLabelText<HTMLInputElement>("VSI manager id").value).toStrictEqual("mgrid");
    expect(screen.getByLabelText<HTMLInputElement>("VSI type id").value).toStrictEqual("testtype");
    expect(screen.getByLabelText<HTMLInputElement>("VSI type id version").value).toStrictEqual("testversion");
    expect(screen.getByLabelText<HTMLInputElement>("VSI instance id").value).toStrictEqual(
      "09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f"
    );
    expect(screen.getByLabelText<HTMLInputElement>("By physical function").checked).toBeTruthy();
    expect(fieldValuesByName("pf")).toStrictEqual(["eth0"]);

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });

  test("Render hostdev network with virtual functions", async (done) => {
    const net = {
      type: "hostdev",
      name: "host0",
      uuid: "1ff4eea5-5902-4c4f-a359-29c8521c9b31",
      vf: ["0000:3d:02.3", "0000:3d:02.2"],
      vlans: [{ tag: 24 }],
    };

    onSubmit = ({ definition }) => {
      expect(definition).toStrictEqual(net);
      done();
    };
    await renderWithNetwork(makeNetworkData(net));
    expect(fieldValuesByName("type")).toStrictEqual(["hostdev"]);
    expect(screen.getByLabelText<HTMLInputElement>("By virtual functions").checked).toBeTruthy();
    expect(fieldValuesByName("vf")).toStrictEqual(["0000:3d:02.3", "0000:3d:02.2"]);
    expect(screen.getByLabelText<HTMLInputElement>("VLAN tag").value).toStrictEqual("24");

    // Check that the form is valid
    expect(screen.getByText<HTMLButtonElement>("Submit").disabled).toBeFalsy();
    click(screen.getByText("Submit"));
  });
});
