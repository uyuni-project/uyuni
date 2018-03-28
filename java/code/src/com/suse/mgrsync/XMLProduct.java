/**
 * Copyright (c) 2014--2015 SUSE LLC
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

package com.suse.mgrsync;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Represents a product in channels.xml format (element product_id).
 */
@Root(name = "product_id", strict = false)
public class XMLProduct implements Comparable<XMLProduct> {

    /** The name. */
    @Attribute
    private String name;

    /** The id. */
    private Long id;

    /** The version. */
    @Attribute(required = false) // Sometimes can be absent for unknown reasons
    private String version;

    /** end of life */
    @Attribute
    private String eol;

    /**
     * Default constructor for bean compatibility.
     */
    public XMLProduct() {
    }

    /**
     * Standard constructor.
     *
     * @param nameIn the name
     * @param idIn the id
     * @param versionIn the version
     */
    public XMLProduct(String nameIn, Long idIn, String versionIn) {
        name = nameIn;
        id = idIn;
        version = versionIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Text
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    @Text
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version.
     * @param versionIn the version
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * True if this product reached end of life, false otherwise.
     *
     * @return eol
     */
    public Boolean getEol() {
        return (this.eol + "").toUpperCase().equals("Y");
    }


    /**
     * Sets if this product reached end of life.
     *
     * @param eolIn true if end of life
     */
    public void setEol(boolean eolIn) {
        if (eolIn) {
            this.eol = "Y";
        }
        else {
            this.eol = "N";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof XMLProduct)) {
            return false;
        }
        XMLProduct other = (XMLProduct) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "XMLProduct [name=" + name + ", id=" + id + ", version=" + version + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(XMLProduct other) {
        return new CompareToBuilder().append(id, other.id).toComparison();
    }

    /**
     * Copy the current object into a new one
     *
     * @return a new XMLProduct that is a copy of the current one
     */
    public XMLProduct copy() {
        XMLProduct xmlProduct = new XMLProduct();
        xmlProduct.setId(this.getId());
        xmlProduct.setName(this.getName());
        xmlProduct.setVersion(this.getVersion());
        return xmlProduct;
    }
}
