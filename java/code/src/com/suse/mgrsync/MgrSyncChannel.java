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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * SUSE Channels are available from NCC and SCC.
 */
@Root(name = "channel", strict = false)
public class MgrSyncChannel {
    @Attribute
    private String arch;

    @Attribute
    private String description;

    @Attribute
    private String family;

    @Attribute(name = "is_signed")
    private String isSigned;

    @Attribute
    private String label;

    @Attribute
    private String name;

    @Attribute
    private String optional;

    @Attribute
    private String parent;

    @Attribute(name = "product_name")
    private String productName;

    @Attribute(name = "product_version")
    private String productVersion;

    @Attribute(name = "source_url")
    private String sourceUrl;

    @Attribute
    private String summary;

    @Attribute(name = "update_tag")
    private String updateTag;

    @ElementList(empty= false)
    private List<MgrSyncProduct> products;

    @Element(name = "dist", required = false)
    private MgrSyncDistribution distribution;

    // Channel status (not an attribute of the xml file)
    private MgrSyncStatus status;

    /**
     * Get the list of products.
     * @return list of products
     */
    public List<MgrSyncProduct> getProducts() {
        if (products != null) {
            return Collections.unmodifiableList(products);
        }
        else {
            return null;
        }
    }

    /**
     * Set the list of products.
     * @param the list of products
     */
    public void setProducts(List<MgrSyncProduct> products) {
        this.products = products;
    }

    /**
     * @return the distribution
     */
    public MgrSyncDistribution getDistribution() {
        return distribution;
    }

    /**
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * Set the architecture.
     * @param archIn
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description.
     * @param descriptionIn
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * Set the channel family.
     * @param familyIn the channel family label to set
     */
    public void setFamily(String familyIn) {
        this.family = familyIn;
    }

    /**
     * @return the isSigned
     */
    public Boolean isSigned() {
        return (this.isSigned + "").toUpperCase().equals("Y");
    }


    /**
     * True if this channel is optional (non-mandatory), false otherwise.
     *
     * @return optional
     */
    public Boolean getOptional() {
        return (this.optional + "").toUpperCase().equals("Y");
    }


    /**
     * Sets if this channel is mandatory or optional.
     *
     * @param optionalIn true if optional
     */
    public void setOptional(boolean optionalIn) {
        if (optionalIn) {
            this.optional = "Y";
        }
        else {
            this.optional = "N";
        }
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the label
     * @param labelIn label to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name.
     * @param nameIn
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the optional
     */
    public Boolean isOptional() {
        return (this.optional + "").toUpperCase().equals("Y");
    }

    /**
     * @return the parent
     */
    public String getParent() {
        return parent;
    }

    /**
     * Set the parent channel label.
     * @param parentIn the parent channel label
     */
    public void setParent(String parentIn) {
        this.parent = parentIn;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return the productVersion
     */
    public String getProductVersion() {
        return productVersion;
    }

    /**
     * @return the sourceUrl
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Set the source URL.
     * @param sourceUrlIn the URL to set
     */
    public void setSourceUrl(String sourceUrlIn) {
        this.sourceUrl = sourceUrlIn;
    }

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Set the summary.
     * @param summary
     */
    public void setSummary(String summaryIn) {
        this.summary = summaryIn;
    }

    /**
     * @return the updateTag
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * Setter for updateTag
     * @param updateTagIn the update tag to set
     */
    public void setUpdateTag(String updateTagIn) {
        this.updateTag = updateTagIn;
    }

    /**
     * Get the status.
     * @return the status
     */
    public MgrSyncStatus getStatus() {
        return status;
    }

    /**
     * Set the status.
     * @param statusIn the status to set
     */
    public void setStatus(MgrSyncStatus statusIn) {
        this.status = statusIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("label", label)
            .toString();
    }
}
