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

package com.suse.proxy;

import static com.suse.proxy.ProxyConfigUtils.EMAIL_FIELD;
import static com.suse.proxy.ProxyConfigUtils.INTERMEDIATE_CAS_FIELD;
import static com.suse.proxy.ProxyConfigUtils.MAX_CACHE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PARENT_FQDN_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_CERT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_KEY_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_PORT_FIELD;
import static com.suse.proxy.ProxyConfigUtils.PROXY_SSH_PARENT_PUB;
import static com.suse.proxy.ProxyConfigUtils.PROXY_SSH_PRIV;
import static com.suse.proxy.ProxyConfigUtils.PROXY_SSH_PUB;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_TAG;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_URL;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.ROOT_CA_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SERVER_ID_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_KEEP;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_REPLACE;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_HTTPD;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SALT_BROKER;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SQUID;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_SSH;
import static com.suse.proxy.ProxyContainerImagesEnum.PROXY_TFTPD;
import static com.suse.utils.Predicates.isProvided;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.utils.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Auxiliary builder for tests for setting up ProxyConfigUpdateJson class.
 */
public class ProxyConfigUpdateJsonBuilder {
    private Long serverId;
    private String parentFqdn;
    private Integer proxyPort;
    private Integer maxCache;
    private String email;
    private String useCertsMode;
    private String rootCA;
    private List<String> intermediateCAs;
    private String proxyCert;
    private String proxyKey;
    private String sourceMode;
    private String sshParentPub;
    private String sshPub;
    private String sshKey;
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

    public ProxyConfigUpdateJsonBuilder serverId(Long serverIdIn) {
        serverId = serverIdIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder parentFqdn(String parentFqdnIn) {
        parentFqdn = parentFqdnIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder proxyPort(Integer proxyPortIn) {
        proxyPort = proxyPortIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder maxCache(Integer maxCacheIn) {
        maxCache = maxCacheIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder email(String emailIn) {
        email = emailIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder replaceCerts(
            String rootCAIn,
            List<String> intermediateCAsIn,
            String proxyCertIn,
            String proxyKeyIn
    ) {
        useCertsMode = USE_CERTS_MODE_REPLACE;
        rootCA = rootCAIn;
        intermediateCAs = intermediateCAsIn;
        proxyCert = proxyCertIn;
        proxyKey = proxyKeyIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder keepCerts(
            String rootCAIn,
            List<String> intermediateCAsIn,
            String proxyCertIn,
            String proxyKeyIn
    ) {
        useCertsMode = USE_CERTS_MODE_KEEP;
        rootCA = rootCAIn;
        intermediateCAs = intermediateCAsIn;
        proxyCert = proxyCertIn;
        proxyKey = proxyKeyIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder sourceRPM() {
        sourceMode = SOURCE_MODE_RPM;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder sourceMode(String sourceModeIn) {
        this.sourceMode = sourceModeIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder sshParentPub(String sshParentPubIn) {
        this.sshParentPub = sshParentPubIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder sshPub(String sshPubIn) {
        this.sshPub = sshPubIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder sshKey(String sshKeyIn) {
        this.sshKey = sshKeyIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder registryMode(String registryModeIn) {
        this.registryMode = registryModeIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder registryBaseURL(String registryBaseURLIn) {
        registryBaseURL = registryBaseURLIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder registryBaseTag(String registryBaseTagIn) {
        registryBaseTag = registryBaseTagIn;
        return this;
    }

    @SuppressWarnings("java:S107")
    public ProxyConfigUpdateJsonBuilder sourceRegistryAdvanced(
            String registryHttpdURLIn, String registryHttpdTagIn,
            String registrySaltbrokerURLIn, String registrySaltbrokerTagIn,
            String registrySquidURLIn, String registrySquidTagIn,
            String registrySshURLIn, String registrySshTagIn,
            String registryTftpdURLIn, String registryTftpdTagIn
    ) {
        sourceMode = SOURCE_MODE_REGISTRY;
        registryMode = REGISTRY_MODE_ADVANCED;
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
        return this;
    }

    public ProxyConfigUpdateJson build() {
        JsonObject requestJsonObject = new JsonObject();
        requestJsonObject.addProperty(SERVER_ID_FIELD, this.serverId);
        requestJsonObject.addProperty(PARENT_FQDN_FIELD, this.parentFqdn);
        requestJsonObject.addProperty(PROXY_PORT_FIELD, this.proxyPort);
        requestJsonObject.addProperty(MAX_CACHE_FIELD, this.maxCache);
        requestJsonObject.addProperty(EMAIL_FIELD, this.email);
        requestJsonObject.addProperty(USE_CERTS_MODE_FIELD, this.useCertsMode);
        requestJsonObject.addProperty(ROOT_CA_FIELD, this.rootCA);
        requestJsonObject.addProperty(PROXY_SSH_PARENT_PUB, this.sshParentPub);
        requestJsonObject.addProperty(PROXY_SSH_PUB, this.sshPub);
        requestJsonObject.addProperty(PROXY_SSH_PRIV, this.sshKey);
        JsonArray intermediateCAsArray = new JsonArray();
        if (isProvided(this.intermediateCAs)) {
            for (String ca : this.intermediateCAs) {
                intermediateCAsArray.add(ca);
            }
        }
        requestJsonObject.add(INTERMEDIATE_CAS_FIELD, intermediateCAsArray);
        requestJsonObject.addProperty(PROXY_CERT_FIELD, this.proxyCert);
        requestJsonObject.addProperty(PROXY_KEY_FIELD, this.proxyKey);
        requestJsonObject.addProperty(SOURCE_MODE_FIELD, this.sourceMode);
        requestJsonObject.addProperty(REGISTRY_MODE, this.registryMode);
        requestJsonObject.addProperty(REGISTRY_BASE_URL, this.registryBaseURL);
        requestJsonObject.addProperty(REGISTRY_BASE_TAG, this.registryBaseTag);
        requestJsonObject.addProperty(PROXY_HTTPD.getUrlField(), this.registryHttpdURL);
        requestJsonObject.addProperty(PROXY_HTTPD.getTagField(), this.registryHttpdTag);
        requestJsonObject.addProperty(PROXY_SALT_BROKER.getUrlField(), this.registrySaltbrokerURL);
        requestJsonObject.addProperty(PROXY_SALT_BROKER.getTagField(), this.registrySaltbrokerTag);
        requestJsonObject.addProperty(PROXY_SQUID.getUrlField(), this.registrySquidURL);
        requestJsonObject.addProperty(PROXY_SQUID.getTagField(), this.registrySquidTag);
        requestJsonObject.addProperty(PROXY_SSH.getUrlField(), this.registrySshURL);
        requestJsonObject.addProperty(PROXY_SSH.getTagField(), this.registrySshTag);
        requestJsonObject.addProperty(PROXY_TFTPD.getUrlField(), this.registryTftpdURL);
        requestJsonObject.addProperty(PROXY_TFTPD.getTagField(), this.registryTftpdTag);

        return Json.GSON.fromJson(requestJsonObject, ProxyConfigUpdateJson.class);
    }

}
