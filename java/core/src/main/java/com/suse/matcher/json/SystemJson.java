/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.matcher.json;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * JSON representation of a system.
 */
public class SystemJson {

    /** The id. */
    private Long id;

    /** The profile name. */
    private String name;

    /** The populated CPU socket count. */
    private Integer cpus;

    /** True if this system is made of metal. */
    private Boolean physical;

    /** True if this system is a virtual host. */
    private Boolean virtualHost;

    /** Virtual machine ids. */
    private Set<Long> virtualSystemIds = new LinkedHashSet<>();

    /** Installed product ids. */
    private Set<Long> productIds = new LinkedHashSet<>();

    /**
     * Standard constructor.
     *
     * @param idIn the id
     * @param nameIn the name
     * @param cpusIn the cpus
     * @param physicalIn the physical
     * @param virtualHostIn true if this is a virtual host
     * @param virtualSystemIdsIn the virtual system ids
     * @param productIdsIn the product ids
     */
    public SystemJson(Long idIn, String nameIn, Integer cpusIn, Boolean physicalIn,
                      Boolean virtualHostIn, Set<Long> virtualSystemIdsIn, Set<Long> productIdsIn) {
        id = idIn;
        name = nameIn;
        cpus = cpusIn;
        physical = physicalIn;
        virtualHost = virtualHostIn;
        virtualSystemIds = virtualSystemIdsIn;
        productIds = productIdsIn;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the profile name.
     *
     * @return the profile name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the profile name.
     *
     * @param nameIn the new profile name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the populated CPU socket count.
     *
     * @return the populated CPU socket count
     */
    public Integer getCpus() {
        return cpus;
    }

    /**
     * Sets the populated CPU socket count.
     *
     * @param cpusIn the new populated CPU socket count
     */
    public void setCpus(Integer cpusIn) {
        cpus = cpusIn;
    }

    /**
     * Returns true if this system is made of metal.
     *
     * @return true if this system is made of metal
     */
    public Boolean getPhysical() {
        return physical;
    }

    /**
     * Sets the physicality of this system.
     *
     * @param physicalIn true if this system is made of metal
     */
    public void setPhysical(Boolean physicalIn) {
        physical = physicalIn;
    }

    /**
     * Returns true if this system is a virtual host.
     *
     * @return the true if this system is a virtual host
     */
    public Boolean getVirtualHost() {
        return virtualHost;
    }

    /**
     * Set to true if this system is a virtual host.
     *
     * @param virtualHostIn true if this system is a virtual host
     */
    public void setVirtualHost(Boolean virtualHostIn) {
        virtualHost = virtualHostIn;
    }

    /**
     * Gets the virtual machine ids.
     *
     * @return the virtual machine ids
     */
    public Set<Long> getVirtualSystemIds() {
        return virtualSystemIds;
    }

    /**
     * Sets the virtual machine ids.
     *
     * @param virtualSystemIdsIn the new virtual machine ids
     */
    public void setVirtualSystemIds(Set<Long> virtualSystemIdsIn) {
        virtualSystemIds = virtualSystemIdsIn;
    }

    /**
     * Gets the installed product ids.
     *
     * @return the installed product ids
     */
    public Set<Long> getProductIds() {
        return productIds;
    }

    /**
     * Sets the installed product ids.
     *
     * @param productIdsIn the new installed product ids
     */
    public void setProductIds(Set<Long> productIdsIn) {
        productIds = productIdsIn;
    }
}
