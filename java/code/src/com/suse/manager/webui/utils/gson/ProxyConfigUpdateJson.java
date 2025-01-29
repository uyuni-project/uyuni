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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents the data sent from the UI to convert a minion into a proxy container configuration
 */
public class ProxyConfigUpdateJson {

    private Long serverId;

    @SerializedName("parentFQDN")
    private String parentFqdn;

    private Integer proxyPort;

    @SerializedName("maxSquidCacheSize")
    private Integer maxCache;

    @SerializedName("proxyAdminEmail")
    private String email;

    private String useCertsMode;
    private String rootCA;
    private List<String> intermediateCAs;
    @SerializedName("proxyCertificate")
    private String proxyCert;
    private String proxyKey;

    private String sourceMode;
    private String registryMode;
    private String registryBaseURL;
    private String registryBaseTag;
    private String registryHttpdURL;
    private String registryHttpdTag;
    private String registrySaltbrokerURL;
    private String registrySaltbrokerTag;
    private String registrySquidURL;
    private String registrySquidTag;
    private String registrySshURL;
    private String registrySshTag;
    private String registryTftpdURL;
    private String registryTftpdTag;


    public String getParentFqdn() {
        return parentFqdn;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public Integer getMaxCache() {
        return maxCache;
    }

    public String getEmail() {
        return email;
    }

    public String getRootCA() {
        return rootCA;
    }

    public List<String> getIntermediateCAs() {
        return intermediateCAs;
    }

    public String getProxyCert() {
        return proxyCert;
    }

    public String getProxyKey() {
        return proxyKey;
    }

    public String getSourceMode() {
        return sourceMode;
    }

    public String getRegistryMode() {
        return registryMode;
    }

    public String getRegistryBaseURL() {
        return registryBaseURL;
    }

    public String getRegistryBaseTag() {
        return registryBaseTag;
    }

    public String getRegistryHttpdURL() {
        return registryHttpdURL;
    }

    public String getRegistryHttpdTag() {
        return registryHttpdTag;
    }

    public String getRegistrySaltbrokerURL() {
        return registrySaltbrokerURL;
    }

    public String getRegistrySaltbrokerTag() {
        return registrySaltbrokerTag;
    }

    public String getRegistrySquidURL() {
        return registrySquidURL;
    }

    public String getRegistrySquidTag() {
        return registrySquidTag;
    }

    public String getRegistrySshURL() {
        return registrySshURL;
    }

    public String getRegistrySshTag() {
        return registrySshTag;
    }

    public String getRegistryTftpdURL() {
        return registryTftpdURL;
    }

    public String getRegistryTftpdTag() {
        return registryTftpdTag;
    }

    public Long getServerId() {
        return serverId;
    }

    public String getUseCertsMode() {
        return useCertsMode;
    }
}
