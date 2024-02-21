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

package com.suse.oval;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.frontend.dto.IdComboDto;

/**
 * A more compact version of {@link com.redhat.rhn.frontend.dto.PackageListItem} that contains the minimum amount of
 * information for package version comparison.
 * */
public class ShallowSystemPackage extends IdComboDto {
    private Long packageId;
    private Long id;
    private String name;
    private String evr;
    private String arch;
    private String epoch;
    private String version;
    private String release;
    private String type;

    /**
     * @return Returns the Id.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The Id to set.
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public String getEvr() {
        return evr;
    }

    public void setEvr(String evrIn) {
        this.evr = evrIn;
    }

    /**
     * @return Returns the packageId
     */
    public Long getPackageId() {
        return packageId;
    }

    /**
     * @param packageIdIn The packageId to set
     */
    public void setPackageId(Long packageIdIn) {
        packageId = packageIdIn;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String archIn) {
        this.arch = archIn;
    }

    public String getEpoch() {
        return epoch;
    }

    public void setEpoch(String epochIn) {
        this.epoch = epochIn;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String typeIn) {
        this.type = typeIn;
    }

    public PackageEvr getPackageEVR() {
        return new PackageEvr(epoch, version, release, type);
    }
}
