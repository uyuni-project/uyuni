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

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a virtual network DNS definition
 */
public class DnsDef {
    private List<DnsForwarderDef> forwarders = new ArrayList<>();
    private List<DnsHostDef> hosts = new ArrayList<>();
    private List<DnsSrvDef> srvs = new ArrayList<>();
    private List<DnsTxtDef> txts = new ArrayList<>();

    /**
     * @return value of forwarders
     */
    public List<DnsForwarderDef> getForwarders() {
        return forwarders;
    }

    /**
     * @param forwardersIn value of forwarders
     */
    public void setForwarders(List<DnsForwarderDef> forwardersIn) {
        forwarders = forwardersIn;
    }

    /**
     * @return value of hosts
     */
    public List<DnsHostDef> getHosts() {
        return hosts;
    }

    /**
     * @param hostsIn value of hosts
     */
    public void setHosts(List<DnsHostDef> hostsIn) {
        hosts = hostsIn;
    }

    /**
     * @return value of srvs
     */
    public List<DnsSrvDef> getSrvs() {
        return srvs;
    }

    /**
     * @param srvsIn value of srvs
     */
    public void setSrvs(List<DnsSrvDef> srvsIn) {
        srvs = srvsIn;
    }

    /**
     * @return value of txts
     */
    public List<DnsTxtDef> getTxts() {
        return txts;
    }

    /**
     * @param txtsIn value of txts
     */
    public void setTxts(List<DnsTxtDef> txtsIn) {
        txts = txtsIn;
    }

    /**
     * Parse dns XML node
     *
     * @param node the node to parse
     *
     * @return the parsed definition
     */
    public static Optional<DnsDef> parse(Element node) {
        if (node == null) {
            return Optional.empty();
        }
        DnsDef def = new DnsDef();
        for (Object child : node.getChildren("forwarder")) {
            def.forwarders.add(DnsForwarderDef.parse((Element)child));
        }

        for (Object child : node.getChildren("host")) {
            def.hosts.add(DnsHostDef.parse((Element)child));
        }

        for (Object child : node.getChildren("srv")) {
            def.srvs.add(DnsSrvDef.parse((Element)child));
        }

        for (Object child : node.getChildren("txt")) {
            def.txts.add(DnsTxtDef.parse((Element)child));
        }

        return Optional.of(def);
    }
}
