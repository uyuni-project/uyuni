/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.cpe;

public final class CpeBuilder {
    private final Cpe cpe = new Cpe();

    public CpeBuilder withVendor(String vendor) {
        cpe.setVendor(vendor);
        return this;
    }

    public CpeBuilder withProduct(String product) {
        cpe.setProduct(product);
        return this;
    }

    public CpeBuilder withVersion(String version) {
        cpe.setVersion(version);
        return this;
    }

    public CpeBuilder withUpdate(String update) {
        cpe.setUpdate(update);
        return this;
    }

    public Cpe build() {
        return cpe;
    }
}
