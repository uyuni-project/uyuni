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
package com.suse.manager.virtualization;

import com.google.gson.annotations.SerializedName;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents the virtual network create action request body structure.
 */
public class NetworkDefinition {
    private static final Logger LOG = Logger.getLogger(NetworkDefinition.class);

    private String type;

    @SerializedName("macvtapmode")
    private Optional<String> macvtapMode = Optional.empty();
    private boolean autostart;
    private Optional<String> bridge = Optional.empty();
    private Optional<Integer> mtu = Optional.empty();
    @SerializedName("virtualport")
    private Optional<VirtualPortDef> virtualPort = Optional.empty();
    private List<String> interfaces = new ArrayList<>();

    @SerializedName("vf")
    private List<String> virtualFunctions = new ArrayList<>();

    @SerializedName("pf")
    private Optional<String> physicalFunction = Optional.empty();

    @SerializedName("vlantrunk")
    private Optional<Boolean> vlanTrunk = Optional.empty();
    private List<VlanDef> vlans = new ArrayList<>();
    private Optional<NatDef> nat = Optional.empty();

    private Optional<IpDef> ipv4 = Optional.empty();
    private Optional<IpDef> ipv6 = Optional.empty();
    private Optional<String> domain = Optional.empty();
    private Optional<DnsDef> dns = Optional.empty();

    // name and uuid will be empty when creating a new network
    private Optional<String> name = Optional.empty();
    private Optional<String> uuid = Optional.empty();

    /**
     * @return value of type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn value of type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return value of macvtapMode
     */
    public Optional<String> getMacvtapMode() {
        return macvtapMode;
    }

    /**
     * @param macvtapModeIn value of macvtapMode
     */
    public void setMacvtapMode(Optional<String> macvtapModeIn) {
        macvtapMode = macvtapModeIn;
    }

    /**
     * @return the forward mode as accepted by Salt-virt and libvirt
     */
    public String getForwardMode() {
        Map<String, Supplier<String>> converters = Map.of(
                "isolated", () -> null,
                "macvtap", () -> getMacvtapMode().orElse("vepa")
        );
        return converters.getOrDefault(getType(), this::getType).get();
    }

    /**
     * Set the forward mode
     *
     * @param modeIn the forward mode as found in the XML
     */
    public void setForwardMode(String modeIn) {
        if (modeIn == null) {
            setType("isolated");
        }
        else {
            Consumer<String> macvtapSetter = mode -> {
                setType("macvtap");
                setMacvtapMode(Optional.of(mode));
            };
            Map<String, Consumer<String>> converters = Map.of(
                    "private", macvtapSetter,
                    "passthrough", macvtapSetter,
                    "vepa", macvtapSetter
            );
            converters.getOrDefault(modeIn, this::setType).accept(modeIn);
        }
    }

    /**
     * @return value of autostart
     */
    public boolean isAutostart() {
        return autostart;
    }

    /**
     * @param autostartIn value of autostart
     */
    public void setAutostart(boolean autostartIn) {
        autostart = autostartIn;
    }

    /**
     * @return value of bridge
     */
    public Optional<String> getBridge() {
        return bridge;
    }

    /**
     * @param bridgeIn value of bridge
     */
    public void setBridge(Optional<String> bridgeIn) {
        bridge = bridgeIn;
    }

    /**
     * @return value of mtu
     */
    public Optional<Integer> getMtu() {
        return mtu;
    }

    /**
     * @param mtuIn value of mtu
     */
    public void setMtu(Optional<Integer> mtuIn) {
        mtu = mtuIn;
    }

    /**
     * @return value of virtualPort
     */
    public Optional<VirtualPortDef> getVirtualPort() {
        return virtualPort;
    }

    /**
     * @param virtualPortIn value of virtualPort
     */
    public void setVirtualPort(Optional<VirtualPortDef> virtualPortIn) {
        virtualPort = virtualPortIn;
    }

    /**
     * @return value of interfaces
     */
    public List<String> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfacesIn value of interfaces
     */
    public void setInterfaces(List<String> interfacesIn) {
        interfaces = interfacesIn;
    }

    /**
     * @return value of virtualFunctions
     */
    public List<String> getVirtualFunctions() {
        return virtualFunctions;
    }

    /**
     * @param virtualFunctionsIn value of virtualFunctions
     */
    public void setVirtualFunctions(List<String> virtualFunctionsIn) {
        virtualFunctions = virtualFunctionsIn;
    }

    /**
     * @return value of physicalFunction
     */
    public Optional<String> getPhysicalFunction() {
        return physicalFunction;
    }

    /**
     * @param physicalFunctionIn value of physicalFunction
     */
    public void setPhysicalFunction(Optional<String> physicalFunctionIn) {
        physicalFunction = physicalFunctionIn;
    }

    /**
     * @return value of vlanTrunk
     */
    public Optional<Boolean> getVlanTrunk() {
        return vlanTrunk;
    }

    /**
     * @param vlanTrunkIn value of vlanTrunk
     */
    public void setVlanTrunk(Optional<Boolean> vlanTrunkIn) {
        vlanTrunk = vlanTrunkIn;
    }

    /**
     * @return value of vlans
     */
    public List<VlanDef> getVlans() {
        return vlans;
    }

    /**
     * @param vlansIn value of vlans
     */
    public void setVlans(List<VlanDef> vlansIn) {
        vlans = vlansIn;
    }

    /**
     * @return value of nat
     */
    public Optional<NatDef> getNat() {
        return nat;
    }

    /**
     * @param natIn value of nat
     */
    public void setNat(Optional<NatDef> natIn) {
        nat = natIn;
    }

    /**
     * @return value of ipv4
     */
    public Optional<IpDef> getIpv4() {
        return ipv4;
    }

    /**
     * @param ipv4In value of ipv4
     */
    public void setIpv4(Optional<IpDef> ipv4In) {
        ipv4 = ipv4In;
    }

    /**
     * @return value of ipv6
     */
    public Optional<IpDef> getIpv6() {
        return ipv6;
    }

    /**
     * @param ipv6In value of ipv6
     */
    public void setIpv6(Optional<IpDef> ipv6In) {
        ipv6 = ipv6In;
    }

    /**
     * @return value of domain
     */
    public Optional<String> getDomain() {
        return domain;
    }

    /**
     * @param domainIn value of domain
     */
    public void setDomain(Optional<String> domainIn) {
        domain = domainIn;
    }

    /**
     * @return value of dns
     */
    public Optional<DnsDef> getDns() {
        return dns;
    }

    /**
     * @param dnsIn value of dns
     */
    public void setDns(Optional<DnsDef> dnsIn) {
        dns = dnsIn;
    }

    /**
     * @return value of name
     */
    public Optional<String> getName() {
        return name;
    }

    /**
     * @param nameIn value of name
     */
    public void setName(Optional<String> nameIn) {
        name = nameIn;
    }

    /**
     * @return value of uuid
     */
    public Optional<String> getUuid() {
        return uuid;
    }

    /**
     * @param uuidIn value of uuid
     */
    public void setUuid(Optional<String> uuidIn) {
        uuid = uuidIn;
    }

    /**
     * Parse libvirt network definition
     *
     * @param xml the libvirt definition XML string
     *
     * @return the created definition object
     */
    public static NetworkDefinition parse(String xml) {
        NetworkDefinition def = null;
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);

        try {
            Document doc = builder.build(new StringReader(xml));
            def = new NetworkDefinition();

            Element netElement = doc.getRootElement();
            Element forwardNode = netElement.getChild("forward");
            def.setForwardMode(forwardNode != null ? forwardNode.getAttributeValue("mode") : null);

            def.setName(Optional.ofNullable(netElement.getChildText("name")));
            def.setUuid(Optional.ofNullable(netElement.getChildText("uuid")));

            if (forwardNode != null) {
                for (Object child : forwardNode.getChildren()) {
                    Element node = (Element) child;
                    switch (node.getName()) {
                        case "nat": def.setNat(Optional.of(NatDef.parseNat(node))); break;
                        case "pf": def.setPhysicalFunction(Optional.of(node.getAttributeValue("dev"))); break;
                        case "interface": def.interfaces.add(node.getAttributeValue("dev")); break;
                        case "address": def.virtualFunctions.add(NetworkDefinition.getPCIAddress(node)); break;
                        default: LOG.error("Unexpected forward mode: " + node.getName());
                    }
                }
            }
            Element bridgeNode = netElement.getChild("bridge");
            if (bridgeNode != null) {
                def.setBridge(Optional.ofNullable(bridgeNode.getAttributeValue("name")));
            }
            Element mtuNode = netElement.getChild("mtu");
            if (mtuNode != null) {
                def.setMtu(Optional.of(Integer.parseInt(mtuNode.getAttributeValue("size"))));
            }
            def.setVirtualPort(VirtualPortDef.parse(netElement.getChild("virtualport")));
            Element vlanNode = netElement.getChild("vlan");
            if (vlanNode != null) {
                String trunk = vlanNode.getAttributeValue("trunk");
                if (trunk != null) {
                    def.setVlanTrunk(Optional.of(trunk.equals("yes")));
                }
                for (Object tag : vlanNode.getChildren("tag")) {
                    def.vlans.add(VlanDef.parse((Element)tag));
                }
            }
            Element domainNode = netElement.getChild("domain");
            if (domainNode != null) {
                def.setDomain(Optional.ofNullable(domainNode.getAttributeValue("name")));
            }
            def.setDns(DnsDef.parse(netElement.getChild("dns")));
            for (Object child : netElement.getChildren("ip")) {
                Element ipNode = (Element)child;
                String family = ipNode.getAttributeValue("family", "ipv4");
                if ("ipv4".equals(family) && def.getIpv4().isEmpty()) {
                    def.setIpv4(IpDef.parse(ipNode));
                }
                else if ("ipv6".equals(family) && def.getIpv6().isEmpty()) {
                    def.setIpv6(IpDef.parse(ipNode));
                }
            }
        }
        catch (Exception e) {
            LOG.error("failed to parse libvirt network XML definition: " + e.getMessage());
            def = null;
        }

        return def;
    }

    private static String getPCIAddress(Element node) {
        List<String> attributes = List.of("domain", "bus", "slot", "function");
        List<Integer> parts = attributes.stream().map(
                attr -> parseNumber(node.getAttributeValue(attr))
        ).collect(Collectors.toList());
        return String.format("%04x:%02x:%02x.%x", parts.get(0), parts.get(1), parts.get(2), parts.get(3)).toUpperCase();
    }

    private static int parseNumber(String value) {
        if (value.startsWith("0x")) {
            return Integer.parseInt(value.substring(2), 16);
        }
        return Integer.parseInt(value);
    }
}
