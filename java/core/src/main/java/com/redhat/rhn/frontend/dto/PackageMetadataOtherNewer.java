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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.manager.action.ActionManager;

public class PackageMetadataOtherNewer extends PackageMetadata {
    /**
     * Constructs a PackageMetadataOtherNewer
     *
     * @param systemIn       PackageListItem for the current system
     * @param otherIn        PackageListItem for the profile or other system
     * @param compareParamIn The parameter to the comparison string.
     */
    public PackageMetadataOtherNewer(PackageListItem systemIn, PackageListItem otherIn, String compareParamIn) {
        super(systemIn, otherIn, KEY_OTHER_NEWER, compareParamIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComparison() {
        LocalizationService ls = LocalizationService.getInstance();
        if (compareParam != null) {
            return ls.getMessage("message.othernewer", compareParam);
        }
        return ls.getMessage("message.profilenewer");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateActionStatus() {
        actionStatus = ACTION_UPGRADE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActionStatus() {
        LocalizationService ls = LocalizationService.getInstance();
        return ls.getMessage("message.actionupgrade", other.getEvr());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePackageRunTransaction(Long packageDeltaId) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "compare returned [KEY_OTHER_NEWER]; deleting package [{}-{}] " +
                            "from system installing package [{}-{}] to system",
                    getName(), getSystemEvr(), getName(), getOther().getEvr());
        }

        if (ActionManager.isPackageRemovable(getName())) {
            handlePackageRunTransaction(packageDeltaId, ActionFactory.TXN_OPERATION_DELETE, getSystem());
        }
        handlePackageRunTransaction(packageDeltaId, ActionFactory.TXN_OPERATION_INSERT, getOther());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemoveFromMissingPackages() {
        return true;
    }
}
