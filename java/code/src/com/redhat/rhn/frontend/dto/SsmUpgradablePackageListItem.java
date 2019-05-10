/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.dto;

/**
 * List item used to display package upgrades for systems in the SSM.
 *
 * @version $Revision$
 */
public class SsmUpgradablePackageListItem extends PackageListItem {

    private String advisory;
    private String advisoryType;
    private Long advisoryId;
    private Long numSystems;
    private boolean errataReboot;
    private boolean errataRestart;

    /** {@inheritDoc} */
    @Override
    public String getSelectionKey() {
        return getIdCombo() + "~*~" + getEpoch() + "-" + getVersion() + "-" + getRelease() + "@" + getAdvisoryId();
    }

    /**
     * @return description of the advisory
     */
    public String getAdvisory() {
        return advisory;
    }

    /**
     * @param advisoryIn description of the advisory
     */
    public void setAdvisory(String advisoryIn) {
        this.advisory = advisoryIn;
    }

    /**
     * @return type of advisory
     */
    public String getAdvisoryType() {
        return advisoryType;
    }

    /**
     * @param advisoryTypeIn type of advisory
     */
    public void setAdvisoryType(String advisoryTypeIn) {
        this.advisoryType = advisoryTypeIn;
    }

    /**
     * @return advisory ID
     */
    public Long getAdvisoryId() {
        return advisoryId;
    }

    /**
     * @param advisoryIdIn advisory ID
     */
    public void setAdvisoryId(Long advisoryIdIn) {
        this.advisoryId = advisoryIdIn;
    }

    /**
     * @return number of systems that have this package
     */
    public Long getNumSystems() {
        return numSystems;
    }

    /**
     * @param numSystemsIn number of systems that have this package
     */
    public void setNumSystems(Long numSystemsIn) {
        this.numSystems = numSystemsIn;
    }

    /**
     * Gets if {@code reboot_suggested} flag is set for the advisory.
     *
     * @return True if {@code reboot_suggested} flag is set.
     */
    public boolean isErrataReboot() {
        return errataReboot;
    }

    /**
     * Sets {@code reboot_suggested} flag for the advisory.
     *
     * @param errataRebootIn {@code reboot_suggested} flag
     */
    public void setErrataReboot(boolean errataRebootIn) {
        this.errataReboot = errataRebootIn;
    }

    /**
     * Gets if {@code restart_suggested} flag is set for the advisory.
     *
     * @return True if {@code restart_suggested} flag is set.
     */
    public boolean isErrataRestart() {
        return errataRestart;
    }

    /**
     * Sets {@code restart_suggested} flag for the advisory.
     *
     * @param errataRestartIn {@code restart_suggested} flag
     */
    public void setErrataRestart(boolean errataRestartIn) {
        this.errataRestart = errataRestartIn;
    }
}
