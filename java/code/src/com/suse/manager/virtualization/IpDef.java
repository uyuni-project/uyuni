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

import com.suse.utils.Ip;

import com.google.gson.annotations.SerializedName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Element;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the IP configuration of a virtual network
 */
public class IpDef {
    private static final Logger LOG = LogManager.getLogger(IpDef.class);

    private String address;
    private Integer prefix;

    @SerializedName("dhcpranges")
    private List<Range<String>> dhcpRanges = new ArrayList<>();
    private List<DhcpHostDef> hosts = new ArrayList<>();

    @SerializedName("bootpfile")
    private Optional<String> bootpFile = Optional.empty();

    @SerializedName("bootpserver")
    private Optional<String> bootpServer = Optional.empty();
    private Optional<String> tftp = Optional.empty();

    /**
     * @return value of address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param addressIn value of address
     */
    public void setAddress(String addressIn) {
        address = addressIn;
    }

    /**
     * @return value of prefix
     */
    public Integer getPrefix() {
        return prefix;
    }

    /**
     * @param prefixIn value of prefix
     */
    public void setPrefix(Integer prefixIn) {
        prefix = prefixIn;
    }

    /**
     * @return value of dhcpRange
     */
    public List<Range<String>> getDhcpRanges() {
        return dhcpRanges;
    }

    /**
     * @param dhcpRangesIn value of dhcpRange
     */
    public void setDhcpRanges(List<Range<String>> dhcpRangesIn) {
        dhcpRanges = dhcpRangesIn;
    }

    /**
     * @return value of host
     */
    public List<DhcpHostDef> getHosts() {
        return hosts;
    }

    /**
     * @param hostsIn value of host
     */
    public void setHosts(List<DhcpHostDef> hostsIn) {
        hosts = hostsIn;
    }

    /**
     * @return value of bootpFile
     */
    public Optional<String> getBootpFile() {
        return bootpFile;
    }

    /**
     * @param bootpFileIn value of bootpFile
     */
    public void setBootpFile(Optional<String> bootpFileIn) {
        bootpFile = bootpFileIn;
    }

    /**
     * @return value of bootpServer
     */
    public Optional<String> getBootpServer() {
        return bootpServer;
    }

    /**
     * @param bootpServerIn value of bootpServer
     */
    public void setBootpServer(Optional<String> bootpServerIn) {
        bootpServer = bootpServerIn;
    }

    /**
     * @return value of tftp
     */
    public Optional<String> getTftp() {
        return tftp;
    }

    /**
     * @param tftpIn value of tftp
     */
    public void setTftp(Optional<String> tftpIn) {
        tftp = tftpIn;
    }

    /**
     * Parse IP XML node
     * @param node the node to parse
     * @return the parsed IP definition
     */
    public static Optional<IpDef> parse(Element node) {
        IpDef def = new IpDef();
        String address = node.getAttributeValue("address");

        String prefixStr = node.getAttributeValue("prefix");
        String netmask = node.getAttributeValue("netmask");
        int prefix = 0;
        if (prefixStr != null) {
            prefix = Integer.parseInt(prefixStr);
        }
        else if (netmask != null) {
            // Convert netmask into prefix
            try {
                prefix = Ip.netmaskToPrefix(netmask);
            }
            catch (UnknownHostException e) {
                LOG.error("Invalid netmask: {}", netmask);
                return Optional.empty();
            }
        }
        def.setPrefix(prefix);

        // Address needs to be the network address (not the gateway one)
        try {
            def.setAddress(Ip.getNetworkAddress(address, prefix));
        }
        catch (UnknownHostException e) {
            LOG.error("Invalid IP address: {}", address);
            return Optional.empty();
        }

        Element dhcpNode = node.getChild("dhcp");
        if (dhcpNode != null) {
            for (Object o : dhcpNode.getChildren("range")) {
                Optional<Range<String>> range = Range.parse((Element)o);
                range.ifPresent(r -> def.dhcpRanges.add(r));
            }

            for (Object o : dhcpNode.getChildren("host")) {
                def.hosts.add(DhcpHostDef.parse((Element)o));
            }

            Element bootpNode = dhcpNode.getChild("bootp");
            if (bootpNode != null) {
                def.setBootpFile(Optional.ofNullable(bootpNode.getAttributeValue("file")));
                def.setBootpServer(Optional.ofNullable(bootpNode.getAttributeValue("server")));
            }
        }
        Element tftpNode = node.getChild("tftp");
        if (tftpNode != null) {
            def.setTftp(Optional.ofNullable(tftpNode.getAttributeValue("root")));
        }
        return Optional.of(def);
    }
}
