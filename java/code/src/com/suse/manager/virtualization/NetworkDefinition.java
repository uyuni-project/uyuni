/**
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents the virtual network create action request body structure.
 */
public class NetworkDefinition {
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
}
