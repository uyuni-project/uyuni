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
 * Represents the data received from the UI to convert a minion into a proxy container configuration
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

    private String proxySshPub;
    private String proxySshPriv;
    private String parentSshPub;


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

    /**
     * Default constructor
     */
    public ProxyConfigUpdateJson() { }

    /**
     * Constructor to populate the data
     *
     * @param serverIdIn the ID of the target minion
     * @param parentFqdnIn the FQDN of the server the proxy uses
     * @param proxyPortIn the SSH port the proxy listens on
     * @param maxCacheIn the maximum memory cache size
     * @param emailIn the email of proxy admin
     * @param useCertsModeIn certificate handling mode
     * @param rootCAIn CA certificate in PEM format
     * @param intermediateCAsIn a list of intermediate CAs in PEM format
     * @param proxyCertIn proxy certificate in PEM format
     * @param proxyKeyIn proxy private key in PEM format
     * @param sourceModeIn image source mode
     * @param registryModeIn registry mode
     * @param registryBaseURLIn Base image registry
     * @param registryBaseTagIn Base image tag
     * @param registryHttpdURLIn Httpd image registry
     * @param registryHttpdTagIn Httpd image tag
     * @param registrySaltbrokerURLIn Salt broker image registry
     * @param registrySaltbrokerTagIn Salt broker image tag
     * @param registrySquidURLIn Squid image registry
     * @param registrySquidTagIn Squid image tag
     * @param registrySshURLIn Ssh image registry
     * @param registrySshTagIn Ssh image tag
     * @param registryTftpdURLIn Tftpd image registry
     * @param registryTftpdTagIn Tftpd image tag
     * @param proxySshPubIn The proxy SSH public key
     * @param proxySshPrivIn The proxy SSH private key
     * @param parentSshPubIn The SSH public key of the proxy's parent
     */

    public ProxyConfigUpdateJson(
        Long serverIdIn, String parentFqdnIn,
        Integer proxyPortIn, Integer maxCacheIn, String emailIn,
        String useCertsModeIn,
        String rootCAIn, List<String> intermediateCAsIn, String proxyCertIn, String proxyKeyIn,
        String sourceModeIn, String registryModeIn,
        String registryBaseURLIn, String registryBaseTagIn,
        String registryHttpdURLIn, String registryHttpdTagIn,
        String registrySaltbrokerURLIn, String registrySaltbrokerTagIn,
        String registrySquidURLIn, String registrySquidTagIn,
        String registrySshURLIn, String registrySshTagIn,
        String registryTftpdURLIn, String registryTftpdTagIn,
        String proxySshPubIn, String proxySshPrivIn,
        String parentSshPubIn
        ) {
        serverId = serverIdIn;
        parentFqdn = parentFqdnIn;
        proxyPort = proxyPortIn;
        maxCache = maxCacheIn;
        email = emailIn;
        useCertsMode = useCertsModeIn;
        rootCA = rootCAIn;
        intermediateCAs = intermediateCAsIn;
        proxyCert = proxyCertIn;
        proxyKey = proxyKeyIn;
        sourceMode = sourceModeIn;
        registryMode = registryModeIn;
        registryBaseURL = registryBaseURLIn;
        registryBaseTag = registryBaseTagIn;
        registryHttpdURL = registryHttpdURLIn;
        registryHttpdTag = registryHttpdTagIn;
        registrySaltbrokerURL = registrySaltbrokerURLIn;
        registrySaltbrokerTag = registrySaltbrokerTagIn;
        registrySquidURL = registrySquidURLIn;
        registrySquidTag = registrySquidTagIn;
        registrySshURL = registrySshURLIn;
        registrySshTag = registrySshTagIn;
        registryTftpdURL = registryTftpdURLIn;
        registryTftpdTag = registryTftpdTagIn;
        proxySshPub = proxySshPubIn;
        proxySshPriv = proxySshPrivIn;
        parentSshPub = parentSshPubIn;
    }

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

    public String getProxySshPub() {
        return proxySshPub;
    }

    public String getProxySshPriv() {
        return proxySshPriv;
    }

    public String getParentSshPub() {
        return parentSshPub;
    }

    public String getUseCertsMode() {
        return useCertsMode;
    }
}
