/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers.download;

import java.util.Optional;

/**
 * Container for package metadata extracted from package filenames and download paths.
 * Holds NEVRA (Name, Epoch, Version, Release, Architecture) information along with
 * optional organization ID and checksum from the download URL.
 */
public class PackageInfo {
    private final String name;
    private final String version;
    private final String release;
    private final String epoch;
    private final String arch;
    private Optional<Long> orgId = Optional.empty();
    private Optional<String> checksum = Optional.empty();

    /**
     * Constructor
     * @param nameIn package name
     * @param epochIn epoch
     * @param versionIn version
     * @param releaseIn release
     * @param archIn architecture
     */
    public PackageInfo(String nameIn, String epochIn, String versionIn, String releaseIn, String archIn) {
        this.name = nameIn;
        this.version = versionIn;
        this.release = releaseIn;
        this.epoch = epochIn;
        this.arch = archIn;
    }

    /**
     * @return package name
     */
    public String getName() {
        return name;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return release
     */
    public String getRelease() {
        return release;
    }

    /**
     * @return epoch
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * @return architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * Set the checksum
     * @param checksumIn the checksum
     */
    public void setChecksum(String checksumIn) {
        checksum = Optional.ofNullable(checksumIn);
    }

    /**
     * Return the checksum if available
     * @return the optional checksum
     */
    public Optional<String> getChecksum() {
        return checksum;
    }

    /**
     * Set the org id
     * @param orgIdIn the org id
     */
    public void setOrgId(Long orgIdIn) {
        orgId = Optional.ofNullable(orgIdIn);
    }

    /**
     * Return the org id if available
     * @return the optional org id
     */
    public Optional<Long> getOrgId() {
        return orgId;
    }
}
