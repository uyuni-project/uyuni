/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package org.cobbler;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Cobbler Network Object
 *
 * @see <a href="https://cobbler.readthedocs.io/en/v3.3.3/code-autodoc/cobbler.items.html#cobbler.items.system.NetworkInterface">RTFD - Cobbler - 3.3.3 - NetworkInterface</a>
 */
public class Network {
    /**
     * The name of the network interface
     */
    private final String name;
    /**
     * The network mask of the interface
     */
    private String netmask;
    /**
     * The IPv4 address of the interface
     */
    private String ipAddress;
    /**
     * The IPv6 address of the interface
     */
    private String ipv6Address;
    /**
     * The DNS name of the interface
     */
    private String dnsname;
    /**
     * The IPv6 secondaries of the network interface
     */
    private List<String> ipv6Secondaries;
    /**
     * Whether the interface is statically configured or dynamically
     */
    private boolean isStatic;
    /**
     * The MAC address of the interface
     */
    private String macAddress;
    /**
     * The field name for the netmask of the corresponding Cobbler object
     */
    private String netmaskVariableName;
    /**
     * The field name for the master of the bonding interface of the corresponding Cobbler object
     */
    private String bondingMasterVariableName;
    /**
     * The field name for the master of the bonding type of the corresponding Cobbler object
     */
    private String bondingTypeVariableName;
    /**
     * The bonding master of the interface
     */
    private String bondingMaster;
    /**
     * The bonding option of the interface
     */
    private String bondingOptions;
    /**
     * The bonding of the interface
     */
    private String bonding;
    /**
     * The name of the master interface of the interface
     */
    private static String bondingMASTER;
    /**
     * The name of the slave interface of the interface
     */
    private static String bondingSLAVE;
    /**
     * The constant for the bonding option not available.
     */
    private static final String BONDING_NA = "na";

    /**
     * Constructor to create a new network interface
     *
     * @param nameIn     the name of the network
     * @param connection CobblerConnection object
     */
    public Network(CobblerConnection connection, String nameIn) {
        name = nameIn;

        // several variable names changed in cobbler 2.2
        if (connection.getVersion() >= 2.2) {
            netmaskVariableName = "netmask";
            bondingMasterVariableName = "interfacemaster";
            bondingTypeVariableName = "interfacetype";
            bondingMASTER = "bond";
            bondingSLAVE = "bond_slave";
        }
        else {
            netmaskVariableName = "subnet";
            bondingMasterVariableName = "bondingmaster";
            bondingTypeVariableName = "bonding";
            bondingMASTER = "master";
            bondingSLAVE = "slave";
        }
    }

    /**
     * Intentionally given default/package scope returns a nicely formatted map
     * that can be used by the system record to set it in xmlrpc.
     *
     * See also https://github.com/openSUSE/cobbler/blob/uyuni/master/cobbler/items/system.py#L672
     *
     * @return a map representation of the interface
     */
    Map<String, Object> toMap() {
        Map<String, Object> inet = new HashMap<>();
        addToMap(inet, "mac_address-" + name, macAddress);
        addToMap(inet, netmaskVariableName + "-" + name, netmask);
        addToMap(inet, "ip_address-" + name, ipAddress);
        addToMap(inet, "static-" + name, isStatic);
        addToMap(inet, "ipv6_address-" + name, ipv6Address);
        addToMap(inet, "ipv6_secondaries-" + name, ipv6Secondaries);
        addToMap(inet, "dns_name-" + name, dnsname);
        addToMap(inet, bondingTypeVariableName + "-" + name, bonding);
        addToMap(inet, bondingMasterVariableName + "-" + name, bondingMaster);
        addToMap(inet, "bonding_opts-" + name, bondingOptions);
        return inet;
    }

    /**
     * This helper method generates the XML-RPC Struct that Cobbler expects when having a network interface
     *
     * @param inet Map that contains the data in the Cobbler format.
     * @param key The name of the property. Must be in the format {@code property-interfacename}.
     * @param value The value for the property.
     */
    private void addToMap(Map<String, Object> inet, String key, Object value) {
        // do not put null values and empty strings
        if (value != null && (!(value instanceof String) ||
                !StringUtils.isBlank((String) value))) {
            inet.put(key, value);
        }
    }

    /**
     * Given an interface name and map generated by the system record this
     * method creates a new Network object.
     *
     * @param name      the name of the interface
     * @param ifaceInfo the interface information
     * @return the network object
     */
    static Network load(CobblerConnection connection, String name,
                        Map<String, Object> ifaceInfo) {
        Network net = new Network(connection, name);
        net.setMacAddress((String) ifaceInfo.get("mac_address"));
        net.setIpAddress((String) ifaceInfo.get("ip_address"));
        net.setStaticNetwork(ifaceInfo.containsKey("static") &&
                Boolean.TRUE.equals(ifaceInfo.get("static")));

        if (connection.getVersion() >= 2.2) {
            net.setNetmask((String) ifaceInfo.get("netmask"));
            net.setBondingMaster((String) ifaceInfo.get("interface_master"));
            net.setBonding((String) ifaceInfo.get("interface_type"));
        }
        else {
            net.setNetmask((String) ifaceInfo.get("subnet"));
            net.setBondingMaster((String) ifaceInfo.get("bonding_master"));
            net.setBonding((String) ifaceInfo.get("bonding"));
        }

        net.setIpv6Address((String) ifaceInfo.get("ipv6_address"));
        net.setIpv6Secondaries((ArrayList<String>) ifaceInfo.get("ipv6_secondaries"));
        net.setDnsname((String) ifaceInfo.get("dnsname"));
        net.setBondingOptions((String) ifaceInfo.get("bonding_opts"));

        return net;
    }

    /**
     * Getter for the name of an interface
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the network mask
     *
     * @return Returns the netmask.
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * Setter for the network mask
     *
     * @param netmaskIn The netmask to set.
     */
    public void setNetmask(String netmaskIn) {
        netmask = netmaskIn;
    }

    /**
     * Getter for the IPv4 address
     *
     * @return Returns the ipAddress.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Setter for the IPv4 address
     *
     * @param ipAddressIn The ipAddress to set.
     */
    public void setIpAddress(String ipAddressIn) {
        ipAddress = ipAddressIn;
    }

    /**
     * Getter for the IPv6 address
     *
     * @return Returns the IPv6 address of the interface.
     */
    public String getIpv6Address() {
        return ipv6Address;
    }

    /**
     * Setter for the IPv6 address
     *
     * @param addressIn The IPv6 address to set.
     */
    public void setIpv6Address(String addressIn) {
        this.ipv6Address = addressIn;
    }

    /**
     * Getter for the DNS name of the system
     *
     * @return Returns the dnsname
     */
    public String getDnsname() {
        return dnsname;
    }

    /**
     * Setter for the DNS name of the system
     *
     * @param dnsnameIn The dnsname set.
     */
    public void setDnsname(String dnsnameIn) {
        this.dnsname = dnsnameIn;
    }

    /**
     * Getter for the IPv6 secondaries
     *
     * @return Returns secondary IPv6 addresses of the interface.
     */
    public List<String> getIpv6Secondaries() {
        return ipv6Secondaries;
    }

    /**
     * Setter for the IPv6 secondaries
     *
     * @param secondariesIn List of secondary IPv6 addresses to set.
     */
    public void setIpv6Secondaries(List<String> secondariesIn) {
        this.ipv6Secondaries = secondariesIn;
    }

    /**
     * Returns if the network is statically or dynamically (via DHCP) defined
     *
     * @return Returns the isStatic.
     */
    public boolean isStaticNetwork() {
        return isStatic;
    }

    /**
     * Sets if the network is statically defined or not
     *
     * @param staticIn The isStatic to set.
     */
    public void setStaticNetwork(boolean staticIn) {
        isStatic = staticIn;
    }

    /**
     * Getter for the MAC address of the system
     *
     * @return Returns the macAddress.
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Setter for the MAC address of the system
     *
     * @param macAddressIn The macAddress to set.
     */
    public void setMacAddress(String macAddressIn) {
        macAddress = macAddressIn;
    }

    /**
     * Getter for the bonding master of the interface
     *
     * @return Returns the bonding master.
     */
    public String getBondingMaster() {
        return bondingMaster;
    }

    /**
     * Setter for the bonding master of the interface
     *
     * @param bondingMasterIn the bondingMaster to set.
     */
    public void setBondingMaster(String bondingMasterIn) {
        bondingMaster = bondingMasterIn;
    }

    /**
     * Getter for the bonding options
     *
     * @return Returns the bonding options.
     */
    public String getBondingOptions() {
        return bondingOptions;
    }

    /**
     * Setter for the bonding options
     *
     * @param bondingOptionsIn the bondingOptions to set.
     */
    public void setBondingOptions(String bondingOptionsIn) {
        bondingOptions = bondingOptionsIn;
    }

    /**
     * Set the Network as a bonding master.
     */
    public void makeBondingMaster() {
        bonding = bondingMASTER;
    }

    /**
     * Set the Network as a bonding slave.
     */
    public void makeBondingSlave() {
        bonding = bondingSLAVE;
    }

    /**
     * Set the Network as not applicable to bonding.
     */
    public void makeBondingNA() {
        bonding = BONDING_NA;
    }

    /**
     * Gets the current value for network bonding.
     *
     * @return Returns the bonding status [master, slave, na]
     */
    public String getBonding() {
        return bonding;
    }

    /**
     * Sets the network bonding to the given value.
     *
     * @param bondingIn The new value for the bonding field.
     */
    private void setBonding(String bondingIn) {
        bonding = bondingIn;
    }
}
