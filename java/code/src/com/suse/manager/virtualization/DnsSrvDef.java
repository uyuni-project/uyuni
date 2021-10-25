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

import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a virtual network DNS SRV record definition
 */
public class DnsSrvDef {
    private String name;
    private String protocol;
    private Optional<String> domain;
    private Optional<String> target;
    private Optional<Integer> port;
    private Optional<Integer> priority;
    private Optional<Integer> weight;

    /**
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn value of name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return value of protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @param protocolIn value of protocol
     */
    public void setProtocol(String protocolIn) {
        protocol = protocolIn;
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
     * @return value of target
     */
    public Optional<String> getTarget() {
        return target;
    }

    /**
     * @param targetIn value of target
     */
    public void setTarget(Optional<String> targetIn) {
        target = targetIn;
    }

    /**
     * @return value of port
     */
    public Optional<Integer> getPort() {
        return port;
    }

    /**
     * @param portIn value of port
     */
    public void setPort(Optional<Integer> portIn) {
        port = portIn;
    }

    /**
     * @return value of priority
     */
    public Optional<Integer> getPriority() {
        return priority;
    }

    /**
     * @param priorityIn value of priority
     */
    public void setPriority(Optional<Integer> priorityIn) {
        priority = priorityIn;
    }

    /**
     * @return value of weight
     */
    public Optional<Integer> getWeight() {
        return weight;
    }

    /**
     * @param weightIn value of weight
     */
    public void setWeight(Optional<Integer> weightIn) {
        weight = weightIn;
    }

    /**
     * Parse a DNS srv XML node
     *
     * @param node the node to parse
     * @return the parsed srv definition
     */
    public static DnsSrvDef parse(Element node) {
        DnsSrvDef def = new DnsSrvDef();
        def.setName(node.getAttributeValue("service"));
        def.setProtocol(node.getAttributeValue("protocol"));

        Function<String, Optional<Integer>> asInt = str -> Optional.ofNullable(str).map(Integer::parseInt);

        def.setDomain(Optional.ofNullable(node.getAttributeValue("domain")));
        def.setTarget(Optional.ofNullable(node.getAttributeValue("target")));
        def.setPort(asInt.apply(node.getAttributeValue("port")));
        def.setPriority(asInt.apply(node.getAttributeValue("priority")));
        def.setWeight(asInt.apply(node.getAttributeValue("weight")));
        return def;
    }
}
