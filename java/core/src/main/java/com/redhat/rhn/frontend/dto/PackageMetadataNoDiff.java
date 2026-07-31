/*
 * Copyright (c) 2026 SUSE LLC
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

public class PackageMetadataNoDiff extends PackageMetadata {

    /**
     * Constructs a PackageMetadataNoDiff
     * @param systemIn PackageListItem for the current system
     * @param otherIn PackageListItem for the profile or other system
     * @param compareParamIn The parameter to the comparison string.
     */
    public PackageMetadataNoDiff(PackageListItem systemIn, PackageListItem otherIn, String compareParamIn) {
        super(systemIn, otherIn, KEY_NO_DIFF, compareParamIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComparison() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateActionStatus() {
        actionStatus = ACTION_NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActionStatus() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePackageRunTransaction(Long packageDeltaId) {
        //does nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsOnOneSideOnly() {
        return false;
    }
}
