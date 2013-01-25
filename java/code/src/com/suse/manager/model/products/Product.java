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

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Product implements Comparable<Product> {

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
}
