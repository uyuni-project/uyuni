/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.domain.Labeled;

public enum SystemSearchHelperIndex implements Labeled {
    //These vars store the name of a lucene index on the search server

    PACKAGES_INDEX("package"),
    SERVER_INDEX("server"),
    HARDWARE_DEVICE_INDEX("hwdevice"),
    SNAPSHOT_TAG_INDEX("snapshotTag"),
    SERVER_CUSTOM_INFO_INDEX("serverCustomInfo");

    private final String label;

    SystemSearchHelperIndex(String labelIn) {
        label = labelIn;
    }

    @Override
    public String getLabel() {
        return label;
    }

    /**
     * Gets the query string
     *
     * @param stringIn string to compare equals
     * @return true if input string is equal to label
     */
    public boolean equalsIndex(String stringIn) {
        return getLabel().equals(stringIn);
    }
}
