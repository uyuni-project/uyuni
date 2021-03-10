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
import java.util.Optional;

/**
 * Represents the IP configuration of a virtual network
 */
public class IpDef {
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
}
