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

/**
 * JSON representation of a product.
 */
public class ProductJson {

    /** The id. */
    private Long id;

    /** The friendly name. */
    private String name;

    /** The product class */
    private String productClass;

    /** true if this is a base product */
    private Boolean base;

    /** true if this is a free product. */
    private Boolean free;

    /**
     * Standard constructor.
     *
     * @param idIn the id
     * @param nameIn the name
     * @param productClassIn the productClass
     * @param baseIn true if this is a base product
     * @param freeIn true if this is a free product
     */
    public ProductJson(Long idIn, String nameIn, String productClassIn,
                       Boolean baseIn, Boolean freeIn) {
        id = idIn;
        name = nameIn;
        productClass = productClassIn;
        base = baseIn;
        free = freeIn;
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
     * Gets the friendly name.
     *
     * @return the friendly name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the friendly name.
     *
     * @param nameIn the new friendly name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return the productClass
     */
    public String getProductClass() {
        return productClass;
    }

    /**
     * @param productClassIn the productClass to set
     */
    public void setProductClass(String productClassIn) {
        this.productClass = productClassIn;
    }

    /**
     * Returns true if this is a base product.
     *
     * @return true if this is a base product
     */
    public Boolean getBase() {
        return base;
    }

    /**
     * Set to true if this is a base product.
     *
     * @param baseIn true if this is a base product
     */
    public void setBase(Boolean baseIn) {
        base = baseIn;
    }

    /**
     * Checks if the product is free.
     *
     * @return true if this is a free product
     */
    public Boolean getFree() {
        return free;
    }

    /**
     * Changes whether this is a free product or not.
     *
     * @param freeIn true if this is a free product
     */
    public void setFree(Boolean freeIn) {
        free = freeIn;
    }
}
