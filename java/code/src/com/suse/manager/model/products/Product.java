/**
 * Copyright (c) 2013 SUSE
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

package com.suse.manager.model.products;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.redhat.rhn.frontend.struts.Selectable;

/**
 * A SUSE Product.
 */
@Root(strict = false)
public class Product implements Selectable, Comparable<Product> {

    /** The architecture. */
    @Attribute
    private String arch;

    /** The ident ID. */
    @Attribute
    private String ident;

    /** The product readable name. */
    @Attribute
    private String name;

    /** The ident ID of the parent product, if any. */
    @Attribute
    private String parent_product;

    /** The mandatory channels. */
    @Element(name = "mandatory_channels")
    private MandatoryChannels mandatoryChannels;

    /** The optional channels. */
    @Element(name = "optional_channels")
    private OptionalChannels optionalChannels;

    /** True if this product has been selected in the GUI. */
    private boolean selected;

    /**
     * Default constructor.
     */
    public Product() {
        // required by Simple XML
    }

    /**
     * Instantiates a new product.
     *
     * @param archIn the architecture
     * @param identIn the ident ID
     * @param nameIn the name
     * @param parentProductIdent the parent_product in
     * @param mandatoryChannelsIn the mandatory channels in
     * @param optionalChannelsIn the optional channels in
     */
    public Product(String archIn, String identIn, String nameIn, String parentProductIdent,
            MandatoryChannels mandatoryChannelsIn, OptionalChannels optionalChannelsIn) {
        super();
        arch = archIn;
        ident = identIn;
        name = nameIn;
        parent_product = parentProductIdent;
        mandatoryChannels = mandatoryChannelsIn;
        optionalChannels = optionalChannelsIn;
    }

    /**
     * Gets the architecture.
     * @return the architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets the ident ID.
     * @return the ident ID
     */
    public String getIdent() {
        return ident;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the parent product's ident ID.
     * @return the parent product
     */
    public String getParentProductIdent() {
        return parent_product;
    }

    /**
     * Gets the mandatory channels.
     * @return the mandatory channels
     */
    public List<Channel> getMandatoryChannels() {
        return mandatoryChannels.getChannels();
    }

    /**
     * Gets the optional channels.
     * @return the optional channels
     */
    public List<Channel> getOptionalChannels() {
        return optionalChannels.getChannels();
    }

    /**
     * Returns true if this product has already been synchronized or it is
     * synchronizing at the moment.
     * @return true or false
     */
    public boolean isSynchronizing() {
        return CollectionUtils.exists(
            CollectionUtils.union(getMandatoryChannels(), getOptionalChannels()),
            new Predicate() {
                @Override
                public boolean evaluate(Object channel) {
                    return ((Channel) channel).isSynchronizing();
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Product product) {
        int ret = 0;
        if (!this.name.equals(product.getName())) {
            ret = this.name.compareTo(product.name);
        }
        else if (!this.arch.equals(product.getArch())) {
            ret = this.arch.compareTo(product.getArch());
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(boolean selectedIn) {
        this.selected = selectedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectionKey() {
        return this.ident;
    }
}
