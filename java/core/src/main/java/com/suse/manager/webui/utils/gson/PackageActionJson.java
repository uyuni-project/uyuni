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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.frontend.dto.PackageListItem;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data required to schedule an action (install/update/remove) on packages
 */
public class PackageActionJson extends ScheduledRequestJson {

    private String actionType;

    private List<String> selectedPackages = new ArrayList<>();

    /**
     * Get the action type of this action
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * Set the action type of this action
     * @param actionTypeIn the action t ype to set
     */
    public void setActionType(String actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * Return the list of selected packages
     * @return the list of selection keys of the package currently in the selection
     */
    public List<String> getSelectedPackages() {
        return selectedPackages;
    }

    /**
     * Set the list of selected packages
     * @param selectedPackagesIn the list of selection keys to mark as selected
     */
    public void setSelectedPackages(List<String> selectedPackagesIn) {
        this.selectedPackages = selectedPackagesIn;
    }

    /**
     * Returns the selected packages as a list of package maps.
     * @return A list of packages described by a map
     */
    public List<Map<String, Long>> getSelectedPackageMap() {
        if (CollectionUtils.isEmpty(selectedPackages)) {
            return Collections.emptyList();
        }

        return selectedPackages.stream()
                               .map(PackageListItem::parse)
                               .map(PackageListItem::getKeyMap)
                               .collect(Collectors.toList());

    }
}
