/**
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.scc.model;

import com.redhat.rhn.domain.product.SUSEProduct;

import com.google.gson.annotations.SerializedName;

public class SCCMinProductJson {
    private long id;
    private String identifier;
    private String version;
    @SerializedName("release_type")
    private String releaseType;
    private String arch;

    /**
     * Constructor
     *
     * @param idIn the scc id
     * @param identifierIn the identifier
     * @param versionIn the version
     * @param releaseTypeIn the release
     * @param archIn the arch
     */
    public SCCMinProductJson(long idIn, String identifierIn, String versionIn,
            String releaseTypeIn, String archIn) {
        this.id = id;
        this.identifier = identifier;
        this.version = version;
        this.releaseType = releaseType;
        this.arch = arch;
    }

    /**
     * Constructor
     *
     * @param product SUSE DB Product
     */
    public SCCMinProductJson(SUSEProduct product) {
        this.id = product.getProductId();
        this.identifier = product.getName();
        this.version = product.getVersion();
        this.releaseType = product.getRelease();
        this.arch = product.getArch().getLabel();
    }

    /**
     * @return Returns the scc id.
     */
    public long getId() {
        return id;
    }

    /**
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return Returns the releaseType.
     */
    public String getReleaseType() {
        return releaseType;
    }

    /**
     * @return Returns the arch.
     */
    public String getArch() {
        return arch;
    }
}
