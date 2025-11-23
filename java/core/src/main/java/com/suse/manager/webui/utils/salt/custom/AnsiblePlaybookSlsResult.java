/*
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

package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class AnsiblePlaybookSlsResult {

    @SerializedName("fullpath")
    private String fullPath;

    @SerializedName("custom_inventory")
    private String customInventory;

    /**
     * Constructor
     */
    public AnsiblePlaybookSlsResult() {
    }

    /**
     * Constructor
     * @param fullPathIn full path to playbook
     * @param customInventoryIn custom inventory of playbook
     */
    public AnsiblePlaybookSlsResult(String fullPathIn, String customInventoryIn) {
        this.fullPath = fullPathIn;
        this.customInventory = customInventoryIn;
    }

    /**
     * Gets the fullPath.
     *
     * @return fullPath
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     * Gets the customInventory.
     *
     * @return customInventory
     */
    public String getCustomInventory() {
        return customInventory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fullPath", fullPath)
                .append("customInventory", customInventory)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AnsiblePlaybookSlsResult that = (AnsiblePlaybookSlsResult) o;

        return new EqualsBuilder()
                .append(fullPath, that.fullPath)
                .append(customInventory, that.customInventory)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fullPath)
                .append(customInventory)
                .toHashCode();
    }
}
