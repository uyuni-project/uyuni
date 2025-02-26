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

package com.suse.manager.model.hub;

import java.util.Map;

public class UpdatableServerData {
    private final boolean rootCADefined;

    private final String rootCA;

    private final boolean gpgKeyDefined;

    private final String gpgKey;

    /**
     * Builds an instance from the given map
     * @param dataMap the map containing the new values. null as value is supported.
     */
    public UpdatableServerData(Map<String, String> dataMap) {
        if (null != dataMap) {
            this.rootCADefined = dataMap.containsKey("root_ca");
            this.rootCA = dataMap.get("root_ca");
            this.gpgKeyDefined = dataMap.containsKey("gpg_key");
            this.gpgKey = dataMap.get("gpg_key");
        }
        else {
            this.rootCADefined = false;
            this.rootCA = null;
            this.gpgKeyDefined = false;
            this.gpgKey = null;
        }
    }

    /**
     * Check if the root CA is defined
     * @return true if the root ca is part of this object
     */
    public boolean hasRootCA() {
        return rootCADefined;
    }

    /**
     * Retrieves the value of the root CA, possibly null.
     * @return the value of the root CA.
     * @throws IllegalStateException if the field is not defined in this object.
     * Use {@link #hasRootCA()} to check beforehand.
     */
    public String getRootCA() {
        if (!rootCADefined) {
            throw new IllegalStateException("rootCA is not defined");
        }

        return rootCA;
    }

    /**
     * Check if the GPG key is defined
     * @return true if the GPG key is part of this object
     */
    public boolean hasGpgKey() {
        return gpgKeyDefined;
    }

    /**
     * Returns the value of the GPG key, possibly null.
     * @return the value of the GPG key
     * @throws IllegalStateException if the field is not defined in this object
     * Use {@link #hasGpgKey()} to check beforehand.
     */
    public String getGpgKey() {
        if (!gpgKeyDefined) {
            throw new IllegalStateException("gpgKey is not defined");
        }

        return gpgKey;
    }
}
