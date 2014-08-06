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
 *
 * @author bo
 */
@Root(name = "product_id", strict = false)
public class MgrSyncProduct {
    @Attribute
    private String name;
    private Integer id;
    @Attribute(required = false) // Sometimes can be absent for unknown reasons
    private String version;

    // Additional data (not attributes in XML)
    private MgrSyncStatus status;
    private String arch;

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

    @Text
    public Integer getId() {
        return id;
    }

    @Text
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public MgrSyncStatus getStatus() {
        return status;
    }

    public void setStatus(MgrSyncStatus statusIn) {
        status = statusIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        arch = archIn;
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
