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

package com.suse.proxy.test;

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_KEEP;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_REPLACE;
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

    public ProxyConfigUpdateJsonBuilder replaceCerts(String rootCAIn, List<String> intermediateCAsIn, String proxyCertIn, String proxyKeyIn) {
        useCertsMode = USE_CERTS_MODE_REPLACE;
        rootCA = rootCAIn;
        intermediateCAs = intermediateCAsIn;
        proxyCert = proxyCertIn;
        proxyKey = proxyKeyIn;
        return this;
    }

    public ProxyConfigUpdateJsonBuilder keepCerts(String rootCAIn, List<String> intermediateCAsIn, String proxyCertIn, String proxyKeyIn) {
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
        requestJsonObject.addProperty("serverId", this.serverId);
        requestJsonObject.addProperty("parentFQDN", this.parentFqdn);
        requestJsonObject.addProperty("proxyPort", this.proxyPort);
        requestJsonObject.addProperty("maxSquidCacheSize", this.maxCache);
        requestJsonObject.addProperty("proxyAdminEmail", this.email);
        requestJsonObject.addProperty("useCertsMode", this.useCertsMode);
        requestJsonObject.addProperty("rootCA", this.rootCA);
        JsonArray intermediateCAsArray = new JsonArray();
        if (isProvided(this.intermediateCAs)) {
            for (String ca : this.intermediateCAs) {
                intermediateCAsArray.add(ca);
            }
        }
        requestJsonObject.add("intermediateCAs", intermediateCAsArray);
        requestJsonObject.addProperty("proxyCertificate", this.proxyCert);
        requestJsonObject.addProperty("proxyKey", this.proxyKey);
        requestJsonObject.addProperty("sourceMode", this.sourceMode);
        requestJsonObject.addProperty("registryMode", this.registryMode);
        requestJsonObject.addProperty("registryBaseURL", this.registryBaseURL);
        requestJsonObject.addProperty("registryBaseTag", this.registryBaseTag);
        requestJsonObject.addProperty("registryHttpdURL", this.registryHttpdURL);
        requestJsonObject.addProperty("registryHttpdTag", this.registryHttpdTag);
        requestJsonObject.addProperty("registrySaltbrokerURL", this.registrySaltbrokerURL);
        requestJsonObject.addProperty("registrySaltbrokerTag", this.registrySaltbrokerTag);
        requestJsonObject.addProperty("registrySquidURL", this.registrySquidURL);
        requestJsonObject.addProperty("registrySquidTag", this.registrySquidTag);
        requestJsonObject.addProperty("registrySshURL", this.registrySshURL);
        requestJsonObject.addProperty("registrySshTag", this.registrySshTag);
        requestJsonObject.addProperty("registryTftpdURL", this.registryTftpdURL);
        requestJsonObject.addProperty("registryTftpdTag", this.registryTftpdTag);

        return Json.GSON.fromJson(requestJsonObject, ProxyConfigUpdateJson.class);
    }

}
