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

@Root(strict = false)
public class Product implements Selectable, Comparable<Product> {

    @Attribute
    private String arch;

    @Attribute
    private String ident;

    @Attribute
    private String name;

    @Attribute
    private String parent_product;

    @Element(name="mandatory_channels")
    private MandatoryChannels mandatoryChannels;

    @Element(name="optional_channels")
    private OptionalChannels optionalChannels;

    // This product is selectable
    private boolean selected;

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

    public String getArch() {
        return arch;
    }

    public String getIdent() {
        return ident;
    }

    public String getName() {
        return name;
    }

    public String getParentProduct() {
        return parent_product;
    }

    public List<Channel> getMandatoryChannels() {
        return mandatoryChannels.getChannels();
    }

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

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String getSelectionKey() {
        return this.ident;
    }
}
