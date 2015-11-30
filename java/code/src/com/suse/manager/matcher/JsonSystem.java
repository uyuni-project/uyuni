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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class JsonSystem {

    /** system id */
    private Long id;

    /** system profile name */
    private String name;

    /** Number of CPU Sockets or IFLs */
    private Long cpus;

    /** Virtual Guests running on this host */
    private List<Long> virtualSystemIds;

    /** Map with installed products; ids to names */
    private Map<Long, String> products;


    /**
     * Constructor
     */
    public JsonSystem() {
        products = new HashMap<>();
        virtualSystemIds = new LinkedList<>();
    }

    /**
     * Constructor
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

        products = new HashMap<>();

        SUSEProductSet productSet = s.getInstalledProductSet();
        SUSEProduct baseProduct = null;
        if (productSet != null) {
            baseProduct = productSet.getBaseProduct();
        }
        if (baseProduct != null) {
            products.put(baseProduct.getId(), baseProduct.getFriendlyName());
            for (SUSEProduct p : productSet.getAddonProducts()) {
                products.put(p.getId(), p.getFriendlyName());
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
     * @return the products
     */
    public Map<Long, String> getProducts() {
        return products;
    }


    /**
     * @return the virtualSystemIds
     */
    public List<Long> getVirtualSystemIds() {
        return virtualSystemIds;
    }


    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @param cpus the cpus to set
     */
    public void setCpus(Long cpus) {
        this.cpus = cpus;
    }


    /**
     * @param products the products to set
     */
    public void setProducts(Map<Long, String> products) {
        this.products = products;
    }


    /**
     * @param virtual_systems_ids the virtual_systems_ids to set
     */
    public void setVirtualSystemIds(List<Long> virtualSystemIds) {
        this.virtualSystemIds = virtualSystemIds;
    }
}

