/*
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

package com.redhat.rhn.frontend.xmlrpc.system;

/**
 * Installed SUSE product info containing all the fields in a
 * {@link com.redhat.rhn.domain.server.InstalledProduct} with the extra SUSE-specific
 * information such as 'friendlyName'
 */

public class SUSEInstalledProduct {
    private String name;
    private String version;
    private String arch;
    private String release;
    private boolean baseproduct;
    private String friendlyName;

    /**
     * Instantiates a new Suse installed product.
     */
    public SUSEInstalledProduct() {
        super();
    }

    /**
     * Instantiates a new Suse installed product.
     *
     * @param nameIn          the name of the product
     * @param versionIn       the version of the product
     * @param archIn          the architecture of the product
     * @param releaseIn       the release label of the product
     * @param isBaseProductIn true for a base product, false for addon products
     * @param friendlyNameIn  human-friendly name of the product
     */
    public SUSEInstalledProduct(String nameIn, String versionIn,
            String archIn, String releaseIn, boolean isBaseProductIn,
            String friendlyNameIn) {
        name = nameIn;
        version = versionIn;
        arch = archIn;
        release = releaseIn;
        baseproduct = isBaseProductIn;
        friendlyName = friendlyNameIn;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets arch.
     *
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets release.
     *
     * @return the release
     */
    public String getRelease() {
        return release;
    }

    /**
     * Is baseproduct boolean.
     *
     * @return the boolean
     */
    public boolean isBaseproduct() {
        return baseproduct;
    }

    /**
     * Gets friendly name.
     *
     * @return the friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }
}
