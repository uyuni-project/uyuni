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

import java.util.Objects;

public class RegisterJson {

    private String token;

    private String rootCA;

    private String gpgKey;

    private String pqcCert;

    /**
     * Default constructor
     */
    public RegisterJson() {
        this(null, null, null, null);
    }

    /**
     * Builds a request instance
     * @param tokenIn the token
     * @param rootCAIn the root certificate
     * @param gpgKeyIn the gpg key
     * @param pqcCertIn the PQC cert
     */
    public RegisterJson(String tokenIn, String rootCAIn, String gpgKeyIn, String pqcCertIn) {
        this.token = tokenIn;
        this.rootCA = rootCAIn;
        this.gpgKey = gpgKeyIn;
        this.pqcCert = pqcCertIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCAIn) {
        this.rootCA = rootCAIn;
    }

    public String getGpgKey() {
        return gpgKey;
    }

    public void setGpgKey(String gpgKeyIn) {
        gpgKey = gpgKeyIn;
    }

    public String getPqcCert() {
        return pqcCert;
    }

    public void setPqcCert(String pqcCertIn) {
        pqcCert = pqcCertIn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegisterJson that)) {
            return false;
        }
        return Objects.equals(getToken(), that.getToken()) &&
                Objects.equals(getRootCA(), that.getRootCA()) &&
                Objects.equals(getGpgKey(), that.getGpgKey()) &&
                Objects.equals(getPqcCert(), that.getPqcCert());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getRootCA(), getGpgKey(), getPqcCert());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegisterJson{");
        sb.append("token='").append(token).append('\'');
        sb.append(", rootCA='").append(rootCA).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
