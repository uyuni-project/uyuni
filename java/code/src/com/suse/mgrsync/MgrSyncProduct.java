/**
 * Copyright (c) 2014 SUSE
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Represents a product in channels.xml format (element product_id).
 * @author bo
 */
@Root(name = "product_id", strict = false)
public class MgrSyncProduct {

    /** The name. */
    @Attribute
    private String name;

    /** The id. */
    private Integer id;

    /** The version. */
    @Attribute(required = false) // Sometimes can be absent for unknown reasons
    private String version;

    /**
     * Default constructor for bean compatibility.
     */
    public MgrSyncProduct() {
    }

    /**
     * Standard constructor.
     *
     * @param nameIn the name
     * @param idIn the id
     * @param versionIn the version
     */
    public MgrSyncProduct(String nameIn, Integer idIn, String versionIn) {
        name = nameIn;
        id = idIn;
        version = versionIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Text
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    @Text
    public void setId(Integer idIn) {
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
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
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
        if (!(obj instanceof MgrSyncProduct)) {
            return false;
        }
        MgrSyncProduct other = (MgrSyncProduct) obj;
        return new EqualsBuilder().append(id, other.id).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MgrSyncProduct [name=" + name + ", id=" + id + ", version=" + version + "]";
    }
}
