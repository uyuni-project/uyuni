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
package com.suse.manager.webui.utils.gson;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertPair;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents the data sent from the UI to request a new proxy container configuration
 */
public class ProxyContainerConfigJson {

    private static final String CREATE_SSL = "create-ssl";
    private static final String USE_SSL = "use-ssl";
    private static final String NO_SSL = "no-ssl";

    private static final Pattern FQDN_PATTERN = Pattern.compile("^[A-Za-z0-9-]++(?:\\.[A-Za-z0-9-]++)*+$");

    @SerializedName("proxyFQDN")
    private String proxyFqdn;
    private Integer proxyPort;
    @SerializedName("serverFQDN")
    private String serverFqdn;
    @SerializedName("maxSquidCacheSize")
    private Long maxCache;
    @SerializedName("proxyAdminEmail")
    private String email;
    private String rootCA;
    private List<String> intermediateCAs;
    @SerializedName("proxyCertificate")
    private String proxyCert;
    private String proxyKey;
    @SerializedName("caCertificate")
    private String caCert;
    private String caKey;
    private String caPassword;
    private List<String> cnames;
    private String country;
    private String state;
    private String city;
    private String org;
    private String orgUnit;
    private String sslEmail;
    @SerializedName("sslMode")
    private String sslMode;

    /**
     * @return whether the data are consistent or not
     */
    public boolean isValid() {
        boolean valid = false;
        if (USE_SSL.equals(sslMode)) {
            valid = rootCA != null && proxyCert != null && proxyKey != null;
        }
        else if (CREATE_SSL.equals(sslMode)) {
            valid = caCert != null && caKey != null && caPassword != null;
        }
        else if (NO_SSL.equals(sslMode)) {
            valid = true;
        }
        return valid && isValidFqdn(proxyFqdn) && isValidFqdn(serverFqdn) && maxCache != null && email != null;
    }

    /**
     * @param fqdn the fqdn to check
     * @return whether the fqdn is valid or not
     */
    private boolean isValidFqdn(String fqdn) {
        return fqdn != null && FQDN_PATTERN.matcher(fqdn).matches();
    }

    /**
     * @return value of proxyFqdn
     */
    public String getProxyFqdn() {
        return proxyFqdn;
    }

    /**
     * @return value of proxyPort
     */
    public Integer getProxyPort() {
        return Optional.ofNullable(proxyPort).orElse(22);
    }

    /**
     * @return value of serverFqdn
     */
    public String getServerFqdn() {
        return serverFqdn;
    }

    /**
     * @return value of maxCache
     */
    public Long getMaxCache() {
        return maxCache;
    }

    /**
     * @return value of email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the proxy cert and key
     */
    public SSLCertPair getProxyCertPair() {
        if (USE_SSL.equals(sslMode)) {
            return new SSLCertPair(proxyCert, proxyKey);
        }
        return null;
    }

    /**
     * @return value of rootCA
     */
    public String getRootCA() {
        return rootCA;
    }

    /**
     * @return value of intermediateCAs
     */
    public List<String> getIntermediateCAs() {
        return intermediateCAs;
    }

    /**
     * @return value of caPassword
     */
    public String getCaPassword() {
        return caPassword;
    }

    /**
     * @return the CA cert and key
     */
    public SSLCertPair getCaPair() {
        if (CREATE_SSL.equals(sslMode)) {
            return new SSLCertPair(caCert, caKey);
        }
        return null;
    }

    /**
     * @return the data to generate a new certificate
     */
    public SSLCertData getCertData() {
        if (CREATE_SSL.equals(sslMode)) {
            return new SSLCertData(proxyFqdn, cnames, country, state, city, org, orgUnit, sslEmail);
        }
        return null;
    }
}
