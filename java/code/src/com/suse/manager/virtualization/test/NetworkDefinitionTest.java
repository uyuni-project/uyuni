/*
 * Copyright (c) 2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.virtualization.test;

import com.suse.manager.virtualization.DnsDef;
import com.suse.manager.virtualization.IpDef;
import com.suse.manager.virtualization.NetworkDefinition;

import java.util.Arrays;
import java.util.Optional;

import junit.framework.TestCase;

public class NetworkDefinitionTest extends TestCase {

    public void testParseBridge() {
        String xml = "<network>\n" +
                     "  <name>default</name>\n" +
                     "  <uuid>8b612d0f-25b3-4bf5-b6f8-19cbd918fa11</uuid>\n" +
                     "  <forward mode='bridge'/>\n" +
                     "  <bridge name='br0'/>\n" +
                     "</network>\n";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals(Optional.of("default"), def.getName());
        assertEquals(Optional.of("8b612d0f-25b3-4bf5-b6f8-19cbd918fa11"), def.getUuid());
        assertEquals("bridge", def.getForwardMode());
        assertEquals(Optional.of("br0"), def.getBridge());
    }

    public void testParseNat() {
        String xml = "<network connections='2'>\n" +
                "  <name>default</name>\n" +
                "  <uuid>d6c95a31-16a2-473a-b8cd-7ad2fe2dd855</uuid>\n" +
                "  <mtu size='7000'/>\n" +
                "  <forward mode='nat'>\n" +
                "    <nat>\n" +
                "      <port start='1024' end='65535'/>\n" +
                "      <address start='192.168.122.5' end='192.168.122.6'/>\n" +
                "    </nat>\n" +
                "  </forward>\n" +
                "  <bridge name='virbr0' stp='on' delay='0'/>\n" +
                "  <mac address='52:54:00:cd:49:6b'/>\n" +
                "  <domain name='tf.local' localOnly='yes'/>\n" +
                "  <ip address='192.168.122.1' netmask='255.255.255.0'>\n" +
                "    <dhcp>\n" +
                "      <range start='192.168.122.2' end='192.168.122.254'/>\n" +
                "      <host mac='2A:C3:A7:A6:01:00' name='dev-srv' ip='192.168.122.110'/>\n" +
                "      <bootp file='pxelinux.0' server='192.168.122.110'/>\n" +
                "    </dhcp>\n" +
                "  </ip>\n" +
                "  <ip family='ipv6' address='2001:db8:ac10:fd01::1' prefix='64'>\n" +
                "    <dhcp>\n" +
                "      <range start='2001:db8:ac10:fd01::1:10' end='2001:db8:ac10:fd01::1:ff'/>\n" +
                "      <host id='0:3:0:1:0:16:3e:11:22:33' name='peter.xyz' ip='2001:db8:ac10:fd01::1:22'/>\n" +
                "    </dhcp>\n" +
                "  </ip>\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("nat", def.getForwardMode());
        assertEquals(Optional.of(7000), def.getMtu());
        assertEquals(Integer.valueOf(1024), def.getNat().orElseThrow().getPort().orElseThrow().getStart());
        assertEquals(Integer.valueOf(65535), def.getNat().orElseThrow().getPort().orElseThrow().getEnd());
        assertEquals("192.168.122.5", def.getNat().orElseThrow().getAddress().orElseThrow().getStart());
        assertEquals("192.168.122.6", def.getNat().orElseThrow().getAddress().orElseThrow().getEnd());
        assertEquals(Optional.of("virbr0"), def.getBridge());
        assertEquals(Optional.of("tf.local"), def.getDomain());

        IpDef ipv4 = def.getIpv4().orElseThrow();
        assertEquals("192.168.122.0", ipv4.getAddress());
        assertEquals(Integer.valueOf(24), ipv4.getPrefix());
        assertEquals("192.168.122.2", ipv4.getDhcpRanges().get(0).getStart());
        assertEquals("192.168.122.254", ipv4.getDhcpRanges().get(0).getEnd());
        assertEquals(Optional.of("2A:C3:A7:A6:01:00"), ipv4.getHosts().get(0).getMac());
        assertEquals("192.168.122.110", ipv4.getHosts().get(0).getIp());
        assertEquals(Optional.of("dev-srv"), ipv4.getHosts().get(0).getName());
        assertEquals(Optional.of("pxelinux.0"), ipv4.getBootpFile());
        assertEquals(Optional.of("192.168.122.110"), ipv4.getBootpServer());

        IpDef ipv6 = def.getIpv6().orElseThrow();
        assertEquals("2001:db8:ac10:fd01:0:0:0:0", ipv6.getAddress());
        assertEquals(Integer.valueOf(64), ipv6.getPrefix());
        assertEquals(Optional.of("0:3:0:1:0:16:3e:11:22:33"), ipv6.getHosts().get(0).getId());
        assertEquals(Optional.of("peter.xyz"), ipv6.getHosts().get(0).getName());
    }

    public void testParseDns() {
        String xml = "<network>\n" +
                "  <name>default</name>\n" +
                "  <uuid>81ff0d90-c91e-6742-64da-4a736edb9a9c</uuid>\n" +
                "  <forward dev='eth0' mode='nat'/>\n" +
                "  <bridge name='virbr0' stp='on' delay='0'/>\n" +
                "  <dns forwardPlainNames='no'>\n" +
                "    <host ip='192.168.122.1'>\n" +
                "      <hostname>host</hostname>\n" +
                "      <hostname>gateway</hostname>\n" +
                "    </host>\n" +
                "    <srv service='name' protocol='tcp' domain='test-domain-name.com' " +
                        "target='test.example.com' port='1111' priority='11' weight='111'/>\n" +
                "    <srv service='name2' protocol='tcp'/>\n" +
                "    <txt name='example' value='example value'/>\n" +
                "    <forwarder addr='8.8.4.4'/>\n" +
                "    <forwarder domain='example.com' addr='192.168.1.1'/>\n" +
                "    <forwarder domain='www.example.com'/>\n" +
                "  </dns>\n" +
                "  <ip address='192.168.122.1' netmask='255.255.255.0'/>" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        DnsDef dnsDef = def.getDns().orElseThrow();
        assertEquals("192.168.122.1", dnsDef.getHosts().get(0).getAddress());
        assertEquals(Arrays.asList("host", "gateway"), dnsDef.getHosts().get(0).getNames());
        assertEquals("name", dnsDef.getSrvs().get(0).getName());
        assertEquals("tcp", dnsDef.getSrvs().get(0).getProtocol());
        assertEquals(Optional.of("test-domain-name.com"), dnsDef.getSrvs().get(0).getDomain());
        assertEquals(Optional.of("test.example.com"), dnsDef.getSrvs().get(0).getTarget());
        assertEquals(Optional.of(1111), dnsDef.getSrvs().get(0).getPort());
        assertEquals(Optional.of(11), dnsDef.getSrvs().get(0).getPriority());
        assertEquals(Optional.of(111), dnsDef.getSrvs().get(0).getWeight());
        assertEquals("name2", dnsDef.getSrvs().get(1).getName());
        assertEquals("tcp", dnsDef.getSrvs().get(1).getProtocol());
        assertTrue(dnsDef.getSrvs().get(1).getDomain().isEmpty());
        assertEquals("example", dnsDef.getTxts().get(0).getName());
        assertEquals("example value", dnsDef.getTxts().get(0).getValue());
        assertEquals(Optional.of("8.8.4.4"), dnsDef.getForwarders().get(0).getAddress());
        assertTrue(dnsDef.getForwarders().get(0).getDomain().isEmpty());
        assertEquals(Optional.of("192.168.1.1"), dnsDef.getForwarders().get(1).getAddress());
        assertEquals(Optional.of("example.com"), dnsDef.getForwarders().get(1).getDomain());
        assertEquals(Optional.of("www.example.com"), dnsDef.getForwarders().get(2).getDomain());
        assertTrue(dnsDef.getForwarders().get(2).getAddress().isEmpty());
    }

    public void testParseOpenVSwitch() {
        String xml = "<network>\n" +
                "  <name>ovs-net</name>\n" +
                "  <forward mode='bridge'/>\n" +
                "  <bridge name='ovsbr0'/>\n" +
                "  <virtualport type='openvswitch'>\n" +
                "    <parameters interfaceid='09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f'/>\n" +
                "  </virtualport>\n" +
                "  <vlan trunk='yes'>\n" +
                "    <tag id='42' nativeMode='untagged'/>\n" +
                "    <tag id='47'/>\n" +
                "  </vlan>\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("openvswitch", def.getVirtualPort().orElseThrow().getType());
        assertEquals(Optional.of("09b11c53-8b5c-4eeb-8f00-d84eaa0aaa4f"),
                def.getVirtualPort().orElseThrow().getInterfaceId());
        assertTrue(def.getVlanTrunk().orElseThrow());
        assertEquals(42, def.getVlans().get(0).getTag());
        assertEquals(Optional.of("untagged"), def.getVlans().get(0).getNativeMode());
        assertEquals(47, def.getVlans().get(1).getTag());
    }

    public void testParseIsolated() {
        String xml = "<network>\n" +
                "  <name>private</name>\n" +
                "  <uuid>81ff0d90-c91e-6742-64da-4a736edb9a9b</uuid>\n" +
                "  <ip address='192.168.152.1' netmask='255.255.255.0' />\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("isolated", def.getType());
        assertTrue(def.getBridge().isEmpty());
    }

    public void testParsePassthrough() {
        String xml = "<network>\n" +
                "  <name>test</name>\n" +
                "  <uuid>81ff0d90-c91e-6742-64da-4a736edb9a9b</uuid>\n" +
                "  <forward mode='passthrough'>\n" +
                "    <interface dev='eth10'/>\n" +
                "    <interface dev='eth11'/>\n" +
                "  </forward>\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("macvtap", def.getType());
        assertEquals(Optional.of("passthrough"), def.getMacvtapMode());
        assertEquals(Arrays.asList("eth10", "eth11"), def.getInterfaces());
    }

    public void testParsePrivate() {
        String xml = "<network>\n" +
                "  <name>test</name>\n" +
                "  <uuid>81ff0d90-c91e-6742-64da-4a736edb9a9b</uuid>\n" +
                "  <forward mode='private'>\n" +
                "    <pf dev='eth0'/>\n" +
                "  </forward>\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("macvtap", def.getType());
        assertEquals(Optional.of("private"), def.getMacvtapMode());
        assertEquals(Optional.of("eth0"), def.getPhysicalFunction());
    }

    public void testParseHostdev() {
        String xml = "<network>\n" +
                "  <name>test</name>\n" +
                "  <uuid>81ff0d90-c91e-6742-64da-4a736edb9a9b</uuid>\n" +
                "  <forward mode='hostdev'>\n" +
                "    <driver name='vfio'/>\n" +
                "    <address type='pci' domain='0x000A' bus='0x12' slot='0x1' function='0x6'/>\n" +
                "    <address type='pci' domain='0' bus='16' slot='4' function='3'/>" +
                "  </forward>\n" +
                "</network>";
        NetworkDefinition def = NetworkDefinition.parse(xml);
        assertEquals("hostdev", def.getType());
        assertEquals(Arrays.asList("000A:12:01.6", "0000:10:04.3"), def.getVirtualFunctions());
    }
}
