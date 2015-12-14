/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.matcher;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON representation of a system
 */
public class JsonSystem {

    /** system id */
    private Long id;

    /** system profile name */
    private String name;

    /** Number of CPU Sockets or IFLs */
    private Long cpus;

    /** True if this system is made of metal */
    private Boolean physical;

    /** Virtual Guests running on this host */
    private List<Long> virtualSystemIds;

    /** Installed product IDs */
    private List<Long> productIds;

    /**
     * Constructor
     */
    public JsonSystem() {
        virtualSystemIds = new LinkedList<>();
        productIds = new LinkedList<>();
    }

    /**
     * Constructor
     * @param s the server
     */
    public JsonSystem(Server s) {
        id = s.getId();
        name = s.getName();
        if (s.getCpu() == null) {
            cpus = null;
        }
        else {
            cpus = s.getCpu().getNrsocket();
        }
        physical = !s.isVirtualGuest() && !s.isVirtualHost();

        SUSEProductSet productSet = s.getInstalledProductSet();
        SUSEProduct baseProduct = null;
        if (productSet != null) {
            baseProduct = productSet.getBaseProduct();
        }
        if (baseProduct != null) {
            productIds = new LinkedList<>();
            productIds.add(baseProduct.getProductId());

            // We have a Vendor Product, so add a SystemEntitlement if needed
            addSystemEntitlementToProducts(s);

            for (SUSEProduct p : productSet.getAddonProducts()) {
                productIds.add(p.getProductId());
            }
        }
        virtualSystemIds = new LinkedList<>();
        virtualSystemIds = s.getGuests().stream()
                .map(h -> h.getGuestSystem().getId())
                .collect(Collectors.toList());
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @return the cpus
     */
    public Long getCpus() {
        return cpus;
    }

    /**
     * @return true if this is a physical system
     */
    public Boolean getPhysical() {
        return physical;
    }

    /**
     * @return the product ids
     */
    public List<Long> getProductIds() {
        return productIds;
    }

    /**
     * @return the virtualSystemIds
     */
    public List<Long> getVirtualSystemIds() {
        return virtualSystemIds;
    }


    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }


    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }


    /**
     * @param cpusIn the cpus to set
     */
    public void setCpus(Long cpusIn) {
        this.cpus = cpusIn;
    }

    /**
     * @param physicalIn true if this is a physical system
     */
    public void setPhysical(Boolean physicalIn) {
        physical = physicalIn;
    }

    /**
     * @param productIdsIn the new product ids
     */
    public void setProductIds(List<Long> productIdsIn) {
        productIds = productIdsIn;
    }

    /**
     * @param virtualSystemIdsIn the virtual_systems_ids to set
     */
    public void setVirtualSystemIds(List<Long> virtualSystemIdsIn) {
        this.virtualSystemIds = virtualSystemIdsIn;
    }

    /**
     * Check if a System Entitlement is required and add the right one if needed.
     *
     * Criterias are:
     * - System uses a Vendor Channel or a clone of it (vendor product is installed) and
     * - System is managed either with management_entitled or saltstack_entitled
     *
     * This method always add Management and Provisioning because they are now merged
     * into one subscription.
     * This method check the architecture and the Virtualization entitlement to decide
     * which product flavor need to be added.
     * @param s the server
     */
    private void addSystemEntitlementToProducts(Server s) {
        if (s.hasEntitlement(EntitlementManager.MANAGEMENT) ||
                s.hasEntitlement(EntitlementManager.SALTSTACK)) {
            if (s.getServerArch().equals(ServerFactory.lookupServerArchByLabel("s390x"))) {
                addSystemEntitlementProduct("SUSE-Manager-Mgmt-Unlimited-Virtual-Z");
                addSystemEntitlementProduct("SUSE-Manager-Prov-Unlimited-Virtual-Z");
            }
            else if (s.hasVirtualizationEntitlement()) {
                addSystemEntitlementProduct("SUSE-Manager-Mgmt-Unlimited-Virtual");
                addSystemEntitlementProduct("SUSE-Manager-Prov-Unlimited-Virtual");
            }
            else {
                addSystemEntitlementProduct("SUSE-Manager-Mgmt-Single");
                addSystemEntitlementProduct("SUSE-Manager-Prov-Single");
            }
        }
    }

    private void addSystemEntitlementProduct(String productName) {
        SUSEProduct ent = SUSEProductFactory.findSUSEProduct(productName, "1.2", null,
                null, true);
        productIds.add(ent.getProductId());
    }
}

