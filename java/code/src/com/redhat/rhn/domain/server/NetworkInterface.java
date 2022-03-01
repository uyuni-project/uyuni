/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.manager.kickstart.IpAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * NetworkInterface
 */
public class NetworkInterface extends BaseDomainHelper implements
Serializable {

    private static final long serialVersionUID = 1L;
    private Long interfaceId;
    private Server server;
    private String name;
    private String hwaddr;
    private String module;
    private ArrayList<ServerNetAddress4> sa4 = null;
    private ArrayList<ServerNetAddress6> sa6 = null;
    private static final String IPV6_REGEX = "^(((?=(?>.*?::)(?!.*::)))(::)?" +
            "([0-9A-F]{1,4}::?){0,5}|([0-9A-F]{1,4}:){6})(\\2([0-9A-F]{1,4}(::?|$)){0,2}" +
            "|((25[0-5]|(2[0-4]|1\\d|[1-9])?\\d)(\\.|$)){4}|[0-9A-F]{1,4}:[0-9A-F]{1,4})" +
            "(?<![^:]:|\\.)\\z";
    private String primary;

    /**
     * @return Returns the interfaceid.
     */
    public Long getInterfaceId() {
        return interfaceId;
    }

    /**
     * @param id The interfaceId to set.
     */
    public void setInterfaceId(Long id) {
        this.interfaceId = id;
    }

    /**
     * @return Returns the hwaddr.
     */
    public String getHwaddr() {
        return hwaddr;
    }

    /**
     * @param h The hwaddr to set.
     */
    public void setHwaddr(String h) {
        this.hwaddr = h;
    }

    /**
     * @return Returns the module.
     */
    public String getModule() {
        return module;
    }

    /**
     * @param m The module to set.
     */
    public void setModule(String m) {
        this.module = m;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param s The server to set.
     */
    public void setServer(Server s) {
        this.server = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface castOther = (NetworkInterface) other;
        return new EqualsBuilder().append(this.getServer(), castOther.getServer())
                .append(this.getName(), castOther.getName())
                .append(this.getHwaddr(), castOther.getHwaddr())
                .append(this.getModule(), castOther.getModule())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getServer())
                .append(this.getName())
                .append(this.getHwaddr())
                .append(this.getModule())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "NetworkInterface - name: " + this.getName();
    }


    /**
     * findServerNetAddress4
     * @param id Id of the network interface to search on.
     */
    private void findServerNetAddress4(Long id) {
        if (sa4 != null) {
            return;
        }

        Session session = HibernateFactory.getSession();
        sa4 = (ArrayList<ServerNetAddress4>) session.getNamedQuery("ServerNetAddress4.lookup")
                .setParameter("interface_id", this.interfaceId)
                .list();
    }

    /**
     * findServerNetAddress6ByScope
     * @param scope Address scope to search for.
     * @return Returns list of IPv6 addresses of the given scope for the given interface.
     */
    private ArrayList<String> findServerNetAddress6ByScope(String scope) {
        Session session = HibernateFactory.getSession();
        ArrayList<ServerNetAddress6> ad6 = (ArrayList<ServerNetAddress6>)
                session.getNamedQuery("ServerNetAddress6.lookup_by_scope_and_id")
                .setParameter("interface_id", this.interfaceId)
                .setParameter("scope", scope)
                .list();

        if (ad6 == null) {
            return null;
        }
        ArrayList<String> addresses = new ArrayList<>();

        for (ServerNetAddress6 a : ad6) {
            addresses.add(a.getAddress());
        }
        return addresses;
    }

    /**
     * @return If available, returns list of global IPv6 addresses for a given interface.
     */
    public ArrayList<String> getGlobalIpv6Addresses() {
        ArrayList<String> addresses = findServerNetAddress6ByScope("universe");
        // RHEL-5 registration may return "global" rather than "universe"
        // for global addresses (a libnl thing).
        if (addresses == null || addresses.isEmpty()) {
            addresses = findServerNetAddress6ByScope("global");
        }
        return addresses;
    }

    /**
     * returns true if the NetworkInterface is disabled
     * @return if it's empty or not
     */
    public boolean isDisabled() {
        boolean ipv6Available = false;
        boolean ipv4Available = false;

        for (String a : getGlobalIpv6Addresses()) {
            if (a != null && !a.equals("")) {
                ipv6Available = true;
            }
        }

        for (ServerNetAddress4 a : getIPv4Addresses()) {
            if (a != null && !a.getAddress().equals("")) {
                ipv4Available = true;
            }
        }

        return !(ipv4Available || ipv6Available);
    }

    /**
     * @return true if valid
     */
    public boolean isIpValid() {
        try {
            for (ServerNetAddress4 addr4 : getIPv4Addresses()) {
                new IpAddress(addr4.getAddress());
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @return true if valid
     */
    public boolean isIpv6Valid() {
        try {
            for (ServerNetAddress6 addr6 : getIPv6Addresses()) {
                if (!addr6.getAddress().toUpperCase().matches(IPV6_REGEX)) {
                    return false;
                }
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * @return true if valid
     */
    public boolean isMacValid() {
        return !(StringUtils.isEmpty(this.getHwaddr()) ||
                this.getHwaddr().equals("00:00:00:00:00:00") ||
                this.getHwaddr().equals("fe:ff:ff:ff:ff:ff"));
    }

    /**
     * Returns if this network interface is valid and should be used
     * @return true if valid, else false
     */
    public boolean isValid() {
        return (isIpValid() || isIpv6Valid()) && isMacValid();
    }

    /**
     * true if the network card has a public ip address
     * and can thus useful in the cases of KSing
     * via ip address
     * @return true if the NIC has a public ip address.
     */
    public boolean isPublic() {
        boolean isPub = isValid();
        boolean hasAddress = false;

        for (ServerNetAddress4 addr4 : getIPv4Addresses()) {
            hasAddress = true;
            isPub = isPub && !(addr4.getAddress().equals("127.0.0.1") ||
                    addr4.getAddress().equals("0.0.0.0"));
        }

        for (ServerNetAddress6 addr6 : getIPv6Addresses()) {
            hasAddress = true;
            isPub = isPub && !addr6.getAddress().equals("::1");
        }

        return (isPub && hasAddress);
    }

    /**
     * true if the nic uses the "bonding" driver module
     * @return true if the nic is a bonding master
     */
    public boolean isBond() {
        // The "bonding" driver module is standard for linux bonds, but it's
        // always possible that someone wrote their own bonding driver. What to
        // do then?
        return "bonding".equals(module);
    }

    /**
     * isVirtBridge tells if nic is a virtual bridge
     * @return true if the nic is a virtual bridge, false otherwise
     */
    public boolean isVirtBridge() {
        return ("bridge".equals(module) && getName().startsWith("virbr"));
    }

    /**
     * Retrieve list of IPv4 addresses
     * @return List of ServerNetAddress4 objects
     */
    public ArrayList<ServerNetAddress4> getIPv4Addresses() {
        if (sa4 == null) {
            Session session = HibernateFactory.getSession();
            sa4 = (ArrayList<ServerNetAddress4>)
                    session.getNamedQuery("ServerNetAddress4.lookup")
                    .setParameter("interface_id", this.interfaceId).list();
        }

        return sa4;
    }

    /**
     * Return comma separated list of IPv4 addresses
     * @return comma separated list of IPv4 addresses
     */
    public String getIPv4AddressesAsString() {
        return getIPv4Addresses().stream().map(ServerNetAddress4::getAddress).collect(Collectors.joining(","));
    }

    /**
     * Retrieve list of IPv6 addresses
     * @return List of ServerNetAddress6 objects
     */
    public ArrayList<ServerNetAddress6> getIPv6Addresses() {
        if (sa6 == null) {
            Session session = HibernateFactory.getSession();
            sa6 = (ArrayList<ServerNetAddress6>)
                    session.getNamedQuery("ServerNetAddress6.lookup_by_id")
                    .setParameter("interface_id", this.interfaceId).list();
        }

        return sa6;
    }

    /**
     * Return comma separated list of IPv6 addresses
     * @return comma separated list of IPv6 addresses
     */
    public String getIPv6AddressesAsString() {
        return getIPv6Addresses().stream().map(ServerNetAddress6::getAddress).collect(Collectors.joining(","));
    }

    /**
     * Setter for sa4
     * @param sa4In sa4
     */
    public void setSa4(ArrayList<ServerNetAddress4> sa4In) {
        this.sa4 = sa4In;
    }

    /**
     * @return primary String which indicates primary interface
     */
    public String getPrimary() {
        return primary;
    }

    /**
     * @param primaryIn String which sets primary interface ('Y')
     */
    public void setPrimary(String primaryIn) {
        primary = primaryIn;
    }

    /**
     * @return Returns first most global ipv6 address
     */
    public String getGlobalIpv6Addr() {
        ArrayList<String> addrs = getGlobalIpv6Addresses();
        if (addrs == null) {
            addrs = findServerNetAddress6ByScope("site");
        }
        if (addrs == null) {
            addrs = findServerNetAddress6ByScope("link");
        }
        if (addrs == null) {
            addrs = findServerNetAddress6ByScope("host");
        }
        return ((addrs != null && addrs.iterator().hasNext()) ?
                addrs.iterator().next() : "::1");
    }
}
