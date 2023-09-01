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

import com.suse.oval.OsFamily;

import java.util.Objects;
import java.util.Optional;

public class Cpe {
    private static final SimpleCpeParser CPE_PARSER = new SimpleCpeParser();
    private String vendor;
    private String product;
    private String version;
    private String update;

    /**
     * Create a CPE with all components set to the empty string ""
     * */
    public Cpe() {
        vendor = "";
        product = "";
        version = "";
        update = "";
    }

    /**
     * Gets the vendor value or empty if not set
     *
     * @return the vendor value or an empty string if not set
     * */
    public String getVendor() {
        return vendor;
    }

    void setVendor(String vendorIn) {
        if (vendorIn != null) {
            this.vendor = vendorIn;
        }
    }

    /**
     * Gets the product value or empty if not set
     *
     * @return the product value or an empty string if not set
     * */
    public String getProduct() {
        return product;
    }

    void setProduct(String productIn) {
        if (productIn != null) {
            this.product = productIn;
        }
    }

    /**
     * Gets the version value or empty if not set
     *
     * @return the version value or an empty string if not set
     * */
    public String getVersion() {
        return version;
    }

    void setVersion(String versionIn) {
        if (versionIn != null) {
            this.version = versionIn;
        }
    }

    /**
     * Gets the update value or empty if not set
     *
     * @return the update value or an empty string if not set
     * */
    public String getUpdate() {
        return update;
    }

    void setUpdate(String updateIn) {
        if (updateIn != null) {
            this.update = updateIn;
        }
    }

    /**
     * Parse the given Cpe URI string and returns a {@link Cpe} object
     *
     * @return a cpe object with all the Cpe information
     * */
    public static Cpe parse(String cpe) {
        return CPE_PARSER.parse(cpe);
    }

    /**
     * Converts and returns the Cpe as plain text URI
     *
     * @return the cpe as URI
     * */
    public String asString() {
        String cpe = "cpe:/o:" + vendor + ":" + product + ":" + version + ":" + update;

        // Removing trailing colons ':'
        return cpe.replaceAll(":*$", "");
    }

    public Optional<OsFamily> toOsFamily() {
        if ("redhat".equals(vendor) && "enterprise_linux".equals(product)) {
            return Optional.of(OsFamily.REDHAT_ENTERPRISE_LINUX);
        }
        else if ("suse".equals(vendor) && "sles".equals(product)) {
            return Optional.of(OsFamily.SUSE_LINUX_ENTERPRISE_SERVER);
        }
        else if ("suse".equals(vendor) && "sled".equals(product)) {
            return Optional.of(OsFamily.SUSE_LINUX_ENTERPRISE_DESKTOP);
        }
        else if ("opensuse".equals(vendor) && "leap".equals(product)) {
            return Optional.of(OsFamily.openSUSE_LEAP);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cpe cpe = (Cpe) o;
        return Objects.equals(vendor, cpe.vendor) && Objects.equals(product, cpe.product) &&
                Objects.equals(version, cpe.version) && Objects.equals(update, cpe.update);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendor, product, version, update);
    }
}
