/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.model.hub;

import java.util.Map;

public class UpdatableServerData {
    public static final String GPG_KEY_DATA_KEY = "gpg_key";
    public static final String PQC_CERT_DATA_KEY = "pqc_cert";

    private final boolean rootCADefined;

    private final String rootCA;

    private final boolean gpgKeyDefined;

    private final String gpgKey;

    private final boolean pqcCertDefined;

    private final String pqcCert;

    /**
     * Builds an instance from the given map
     * @param dataMap the map containing the new values. null as value is supported.
     */
    public UpdatableServerData(Map<String, String> dataMap) {
        if (null != dataMap) {
            this.rootCADefined = dataMap.containsKey("root_ca");
            this.rootCA = dataMap.get("root_ca");
            this.gpgKeyDefined = dataMap.containsKey(GPG_KEY_DATA_KEY);
            this.gpgKey = dataMap.get(GPG_KEY_DATA_KEY);
            this.pqcCertDefined = dataMap.containsKey(PQC_CERT_DATA_KEY);
            this.pqcCert = dataMap.get(PQC_CERT_DATA_KEY);
        }
        else {
            this.rootCADefined = false;
            this.rootCA = null;
            this.gpgKeyDefined = false;
            this.gpgKey = null;
            this.pqcCertDefined = false;
            this.pqcCert = null;
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

    /**
     * Check if the PQC certificate is defined
     * @return true if the PQC certificate is part of this object
     */
    public boolean hasPqcCert() {
        return pqcCertDefined;
    }

    /**
     * Returns the value of the PQC certificate, possibly null.
     * @return the value of the PQC certificate
     * @throws IllegalStateException if the field is not defined in this object
     * Use {@link #hasPqcCert()} to check beforehand.
     */
    public String getPqcCert() {
        if (!pqcCertDefined) {
            throw new IllegalStateException("pqcCert is not defined");
        }

        return pqcCert;
    }
}
