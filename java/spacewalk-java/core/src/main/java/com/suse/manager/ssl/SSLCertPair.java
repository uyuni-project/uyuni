/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.ssl;

import java.util.Objects;

/**
 * SSL certificate and its key
 */
public class SSLCertPair {

    private String certificate;
    private String key;

    /**
     * Creates a new pair
     *
     * @param certificateIn certificate content in PEM format
     * @param keyIn private key content in PEM format
     */
    public SSLCertPair(String certificateIn, String keyIn) {
        certificate = certificateIn;
        key = keyIn;
    }

    /**
     * @return value of certificate
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * @param certificateIn value of certificate
     */
    public void setCertificate(String certificateIn) {
        certificate = certificateIn;
    }

    /**
     * @return value of key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param keyIn value of key
     */
    public void setKey(String keyIn) {
        key = keyIn;
    }

    /**
     * @return whether the pair has a proper certificate and key
     */
    public boolean isComplete() {
        return certificate != null && key != null;
    }

    /**
     * @return if the cert / key values are invalid
     */
    public boolean isInvalid() {
        return certificate == null ^ key == null;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        SSLCertPair that = (SSLCertPair) oIn;
        return Objects.equals(certificate, that.certificate) &&
                Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificate, key);
    }
}
