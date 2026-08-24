/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.xmlrpc.proxy;

import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.ApiResponseWrapper;
import com.suse.manager.api.docs.ApiEndpointDoc;
import com.suse.manager.api.docs.LegacyDocResponse;
import com.suse.manager.api.docs.PublicApiEndpoint;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import spark.route.HttpMethod;

/**
 * API contract for {@link ProxyHandler}.
 */
@Tag(name = "proxy", description = "Provides methods to activate/deactivate a proxy")
public interface ProxyHandlerApi {

    /**
     * Creates a monitoring scout for a proxy.
     *
     * @param clientcert the client certificate of the system
     * @return nothing, this call always throws
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Create Monitoring Scout for proxy.",
        requestClass = ClientCertRequest.class,
        responseClass = StringResponse.class
    )
    String createMonitoringScout(String clientcert);

    /**
     * Tells whether the system identified by the given client certificate is a proxy.
     *
     * @param clientcert the client certificate of the system
     * @return 1 on success
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Test, if the system identified by the given client certificate is proxy.",
        method = HttpMethod.get,
        isIntegerResponse = true
    )
    int isProxy(
        @Parameter(name = "clientcert", in = ParameterIn.QUERY, required = true,
                description = "client certificate file") String clientcert);

    /**
     * Deactivates the proxy identified by the given client certificate.
     *
     * @param clientcert the client certificate of the system
     * @return 1 on success
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Deactivates the proxy identified by the given client certificate.",
        requestClass = ClientCertRequest.class,
        isIntegerResponse = true
    )
    int deactivateProxy(String clientcert);

    /**
     * Activates the proxy identified by the given client certificate.
     *
     * @param clientcert the client certificate of the system
     * @param version the version of the proxy to be activated
     * @return 1 on success
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "Activates the proxy identified by the given client certificate.",
        requestClass = ActivateProxyRequest.class,
        isIntegerResponse = true
    )
    int activateProxy(String clientcert, String version);

    /**
     * Lists the available versions of the proxy channel for a system.
     *
     * @param clientcert the client certificate of the system
     * @return the available proxy channel versions
     */
    @PublicApiEndpoint
    @ApiEndpointDoc(
        summary = "List available version of proxy channel for system identified by " +
            "the given client certificate.",
        method = HttpMethod.get,
        responseClass = ProxyChannelListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "version")
    )
    List<String> listAvailableProxyChannels(
        @Parameter(name = "clientcert", in = ParameterIn.QUERY, required = true,
                description = "client certificate file") String clientcert);

    /**
     * Lists the proxies within the user's organization.
     *
     * @param loggedInUser the current user
     * @return the proxies of the organization
     */
    @ApiEndpointDoc(
        summary = "List the proxies within the user's organization.",
        method = HttpMethod.get,
        responseClass = ShortSystemInfoListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "system")
    )
    Object[] listProxies(@Parameter(hidden = true) User loggedInUser);

    /**
     * Lists the clients directly connected to a proxy.
     *
     * @param loggedInUser the current user
     * @param proxyId the proxy id
     * @return the ids of the connected clients
     */
    @ApiEndpointDoc(
        summary = "List the clients directly connected to a given Proxy.",
        method = HttpMethod.get,
        responseClass = ProxyClientListResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "clientId")
    )
    List<Long> listProxyClients(
        @Parameter(hidden = true) User loggedInUser,
        @Parameter(name = "proxyId", in = ParameterIn.QUERY, required = true,
                description = "the Proxy ID") Integer proxyId);

    /**
     * Computes the configuration for proxy containers, signing the certificate with the given CA.
     *
     * @param loggedInUser the current user
     * @param proxyName the FQDN of the proxy
     * @param proxyPort the SSH port the proxy listens on
     * @param server the server FQDN the proxy will connect to
     * @param maxCache the max cache size in MB
     * @param email the proxy admin email
     * @param rootCA the root CA used to sign the SSL certificate
     * @param intermediateCAs the intermediate CAs
     * @param proxyCrt the proxy CRT content
     * @param proxyKey the proxy SSL private key
     * @return the configuration archive
     */
    @ApiEndpointDoc(
        summary = "Compute and download the configuration for proxy containers",
        requestClass = ContainerConfigWithCertRequest.class,
        responseClass = ContainerConfigResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "binary object - package file")
    )
    byte[] containerConfig(User loggedInUser, String proxyName, Integer proxyPort, String server,
            Integer maxCache, String email, String rootCA, List<String> intermediateCAs,
            String proxyCrt, String proxyKey);

    /**
     * Computes the configuration for proxy containers without adding the TLS certificate.
     *
     * @param loggedInUser the current user
     * @param proxyName the FQDN of the proxy
     * @param proxyPort the SSH port the proxy listens on
     * @param server the server FQDN the proxy will connect to
     * @param maxCache the max cache size in MB
     * @param email the proxy admin email
     * @return the configuration archive
     */
    @ApiEndpointDoc(
        summary = "Compute and download the configuration for proxy containers without adding " +
            "the TLS certificate",
        requestClass = ContainerConfigRequest.class,
        responseClass = ContainerConfigResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "binary object - package file")
    )
    byte[] containerConfig(User loggedInUser, String proxyName, Integer proxyPort, String server,
            Integer maxCache, String email);

    /**
     * Computes the configuration for proxy containers, generating the certificate from a CA.
     *
     * @param loggedInUser the current user
     * @param proxyName the FQDN of the proxy
     * @param proxyPort the SSH port the proxy listens on
     * @param server the server FQDN the proxy will connect to
     * @param maxCache the max cache size in MB
     * @param email the proxy admin email
     * @param caCrt the CA certificate used to sign the SSL certificate
     * @param caKey the CA private key used to sign the SSL certificate
     * @param caPassword the CA private key password
     * @param cnames the proxy alternate cnames
     * @param country the country of the SSL certificate
     * @param state the state of the SSL certificate
     * @param city the city of the SSL certificate
     * @param org the organization of the SSL certificate
     * @param orgUnit the organization unit of the SSL certificate
     * @param sslEmail the email of the SSL certificate
     * @return the configuration archive
     */
    @ApiEndpointDoc(
        summary = "Compute and download the configuration for proxy containers",
        requestClass = ContainerConfigWithCaRequest.class,
        responseClass = ContainerConfigResponse.class,
        legacyDocResponse = @LegacyDocResponse(name = "binary object - package file")
    )
    byte[] containerConfig(User loggedInUser, String proxyName, Integer proxyPort, String server,
            Integer maxCache, String email, String caCrt, String caKey, String caPassword,
            List<String> cnames, String country, String state, String city, String org,
            String orgUnit, String sslEmail);

    /**
     * Deploys a proxy container on a salt minion, using the images installed as RPMs.
     *
     * @param loggedInUser the current user
     * @param hostId the id of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of the proxy admin
     * @param rootCA the CA certificate
     * @param intermediateCAs the intermediate CAs
     * @param proxyCert the proxy certificate
     * @param proxyKey the proxy private key
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deploy a proxy container on given salt minion. It expects that the images " +
            "are installed as RPMs.",
        requestClass = BootstrapProxyRequest.class,
        isIntegerResponse = true
    )
    int bootstrapProxy(User loggedInUser, Integer hostId, String parentFqdn, Integer proxyPort,
            Integer maxCache, String email, String rootCA, List<String> intermediateCAs,
            String proxyCert, String proxyKey);

    /**
     * Deploys a proxy container on a salt minion, using the given registry for the images.
     *
     * @param loggedInUser the current user
     * @param hostId the id of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of the proxy admin
     * @param rootCA the CA certificate
     * @param intermediateCAs the intermediate CAs
     * @param proxyCert the proxy certificate
     * @param proxyKey the proxy private key
     * @param registryBaseURL the image registry
     * @param registryBaseTag the image tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deploy a proxy container on given salt minion. Use the given registry for the images.",
        requestClass = BootstrapProxyWithRegistryRequest.class,
        isIntegerResponse = true
    )
    int bootstrapProxy(User loggedInUser, Integer hostId, String parentFqdn, Integer proxyPort,
            Integer maxCache, String email, String rootCA, List<String> intermediateCAs,
            String proxyCert, String proxyKey, String registryBaseURL, String registryBaseTag);

    /**
     * Deploys a proxy container on a salt minion, with an individual registry for each image.
     *
     * @param loggedInUser the current user
     * @param hostId the id of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of the proxy admin
     * @param rootCA the CA certificate
     * @param intermediateCAs the intermediate CAs
     * @param proxyCert the proxy certificate
     * @param proxyKey the proxy private key
     * @param registryHttpdURL the httpd image registry
     * @param registryHttpdTag the httpd image tag
     * @param registrySaltbrokerURL the salt broker image registry
     * @param registrySaltbrokerTag the salt broker image tag
     * @param registrySquidURL the squid image registry
     * @param registrySquidTag the squid image tag
     * @param registrySshURL the ssh image registry
     * @param registrySshTag the ssh image tag
     * @param registryTftpdURL the tftpd image registry
     * @param registryTftpdTag the tftpd image tag
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Deploy a proxy container on given salt minion. Allows individual registry for each image.",
        requestClass = BootstrapProxyWithRegistriesRequest.class,
        isIntegerResponse = true
    )
    int bootstrapProxy(User loggedInUser, Integer hostId, String parentFqdn, Integer proxyPort,
            Integer maxCache, String email, String rootCA, List<String> intermediateCAs,
            String proxyCert, String proxyKey, String registryHttpdURL, String registryHttpdTag,
            String registrySaltbrokerURL, String registrySaltbrokerTag, String registrySquidURL,
            String registrySquidTag, String registrySshURL, String registrySshTag,
            String registryTftpdURL, String registryTftpdTag);

    /**
     * Backs up the configuration of proxies so they can be migrated later.
     *
     * @param loggedInUser the current user
     * @param sids the ids of the proxies to back up
     * @return 1 on success
     */
    @ApiEndpointDoc(
        summary = "Backup the configuration of proxies in order to migrate them later.",
        requestClass = BackupConfigurationRequest.class,
        isIntegerResponse = true
    )
    int backupConfiguration(User loggedInUser, List<Integer> sids);

    @Schema(name = "ApiResponseString")
    interface StringResponse extends ApiResponseWrapper<String> { }

    @Schema(name = "ApiResponseProxyChannelList")
    interface ProxyChannelListResponse extends ApiResponseWrapper<List<String>> { }

    @Schema(name = "ApiResponseProxyClientList")
    interface ProxyClientListResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseContainerConfig")
    interface ContainerConfigResponse extends ApiResponseWrapper<List<Integer>> { }

    @Schema(name = "ApiResponseShortSystemInfoList")
    interface ShortSystemInfoListResponse extends ApiResponseWrapper<List<ShortSystemInfoDoc>> { }

    @Schema(name = "ProxyClientCertRequest")
    interface ClientCertRequest {

        /**
         * @return the client certificate of the system
         */
        @Schema(description = "client certificate file", requiredMode = Schema.RequiredMode.REQUIRED)
        String getClientcert();
    }

    @Schema(name = "ProxyActivateRequest")
    @JsonPropertyOrder({"clientcert", "version"})
    interface ActivateProxyRequest {

        /**
         * @return the client certificate of the system
         */
        @Schema(description = "client certificate file", requiredMode = Schema.RequiredMode.REQUIRED)
        String getClientcert();

        /**
         * @return the version of the proxy to be activated
         */
        @Schema(description = "Version of proxy to be activated", requiredMode = Schema.RequiredMode.REQUIRED)
        String getVersion();
    }

    @Schema(name = "ProxyContainerConfigRequest")
    @JsonPropertyOrder({"proxyName", "proxyPort", "server", "maxCache", "email"})
    interface ContainerConfigRequest {

        /**
         * @return the FQDN of the proxy
         */
        @Schema(description = "The FQDN of the proxy", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyName();

        /**
         * @return the SSH port the proxy listens on
         */
        @Schema(description = "The SSH port the proxy listens on", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyPort();

        /**
         * @return the server FQDN the proxy will connect to
         */
        @Schema(description = "The server FQDN the proxy will connect to",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getServer();

        /**
         * @return the max cache size in MB
         */
        @Schema(description = "Max cache size in MB", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMaxCache();

        /**
         * @return the proxy admin email
         */
        @Schema(description = "The proxy admin email", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();
    }

    @Schema(name = "ProxyContainerConfigWithCertRequest")
    @JsonPropertyOrder({"proxyName", "proxyPort", "server", "maxCache", "email", "rootCA", "intermediateCAs",
        "proxyCrt", "proxyKey"})
    interface ContainerConfigWithCertRequest {

        /**
         * @return the FQDN of the proxy
         */
        @Schema(description = "The FQDN of the proxy", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyName();

        /**
         * @return the SSH port the proxy listens on
         */
        @Schema(description = "The SSH port the proxy listens on", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyPort();

        /**
         * @return the server FQDN the proxy will connect to
         */
        @Schema(description = "The server FQDN the proxy will connect to",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getServer();

        /**
         * @return the max cache size in MB
         */
        @Schema(description = "Max cache size in MB", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMaxCache();

        /**
         * @return the proxy admin email
         */
        @Schema(description = "The proxy admin email", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return the root CA used to sign the SSL certificate
         */
        @Schema(name = "rootCA", description = "The root CA used to sign the SSL certificate in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRootCA();

        /**
         * @return the intermediate CAs
         */
        @Schema(name = "intermediateCAs", description = "a list of intermediate CAs in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getIntermediateCAs();

        /**
         * @return the proxy CRT content
         */
        @Schema(description = "proxy CRT content in PEM format", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyCrt();

        /**
         * @return the proxy SSL private key
         */
        @Schema(description = "proxy SSL private key in PEM format", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyKey();
    }

    @Schema(name = "ProxyContainerConfigWithCaRequest")
    @JsonPropertyOrder({"proxyName", "proxyPort", "server", "maxCache", "email", "caCrt", "caKey", "caPassword",
        "cnames", "country", "state", "city", "org", "orgUnit", "sslEmail"})
    interface ContainerConfigWithCaRequest {

        /**
         * @return the FQDN of the proxy
         */
        @Schema(description = "The FQDN of the proxy", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyName();

        /**
         * @return the SSH port the proxy listens on
         */
        @Schema(description = "The SSH port the proxy listens on", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyPort();

        /**
         * @return the server FQDN the proxy will connect to
         */
        @Schema(description = "The server FQDN the proxy will connect to",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getServer();

        /**
         * @return the max cache size in MB
         */
        @Schema(description = "Max cache size in MB", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMaxCache();

        /**
         * @return the proxy admin email
         */
        @Schema(description = "The proxy admin email", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return the CA certificate used to sign the SSL certificate
         */
        @Schema(description = "CA certificate to use to sign the SSL certificate in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCaCrt();

        /**
         * @return the CA private key used to sign the SSL certificate
         */
        @Schema(description = "CA private key to use to sign the SSL certificate in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCaKey();

        /**
         * @return the CA private key password
         */
        @Schema(description = "The CA private key password", requiredMode = Schema.RequiredMode.REQUIRED)
        String getCaPassword();

        /**
         * @return the proxy alternate cnames
         */
        @Schema(description = "Proxy alternate cnames to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getCnames();

        /**
         * @return the country of the SSL certificate
         */
        @Schema(description = "The 2-letter country code to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCountry();

        /**
         * @return the state of the SSL certificate
         */
        @Schema(description = "The state to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getState();

        /**
         * @return the city of the SSL certificate
         */
        @Schema(description = "The city to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getCity();

        /**
         * @return the organization of the SSL certificate
         */
        @Schema(description = "The organization to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrg();

        /**
         * @return the organization unit of the SSL certificate
         */
        @Schema(description = "The organization unit to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getOrgUnit();

        /**
         * @return the email of the SSL certificate
         */
        @Schema(description = "The email to set in the SSL certificate",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getSslEmail();
    }

    @Schema(name = "ProxyBootstrapRequest")
    @JsonPropertyOrder({"hostId", "parentFqdn", "proxyPort", "maxCache", "email", "rootCA", "intermediateCAs",
        "proxyCert", "proxyKey"})
    interface BootstrapProxyRequest {

        /**
         * @return the id of the target minion
         */
        @Schema(description = "The ID of the target minion", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getHostId();

        /**
         * @return the FQDN of the server the proxy uses
         */
        @Schema(description = "The FQDN of the server the proxy uses",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getParentFqdn();

        /**
         * @return the SSH port the proxy listens on
         */
        @Schema(description = "The SSH port the proxy listens on", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getProxyPort();

        /**
         * @return the maximum memory cache size
         */
        @Schema(description = "The maximum memory cache size", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getMaxCache();

        /**
         * @return the email of the proxy admin
         */
        @Schema(description = "The email of proxy admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String getEmail();

        /**
         * @return the CA certificate
         */
        @Schema(name = "rootCA", description = "CA certificate in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRootCA();

        /**
         * @return the intermediate CAs
         */
        @Schema(name = "intermediateCAs", description = "A list of intermediate CAs in PEM format",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<String> getIntermediateCAs();

        /**
         * @return the proxy certificate
         */
        @Schema(description = "Proxy certificate in PEM format", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyCert();

        /**
         * @return the proxy private key
         */
        @Schema(description = "Proxy private key in PEM format", requiredMode = Schema.RequiredMode.REQUIRED)
        String getProxyKey();
    }

    @Schema(name = "ProxyBootstrapWithRegistryRequest")
    @JsonPropertyOrder({"hostId", "parentFqdn", "proxyPort", "maxCache", "email", "rootCA", "intermediateCAs",
        "proxyCert", "proxyKey", "registryBaseURL", "registryBaseTag"})
    interface BootstrapProxyWithRegistryRequest extends BootstrapProxyRequest {

        /**
         * @return the image registry
         */
        @Schema(name = "registryBaseURL",
                description = "Image registry (e.g. https://registry.opensuse.org/uyuni/)",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryBaseURL();

        /**
         * @return the image tag
         */
        @Schema(description = "Image tag (e.g. latest)", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryBaseTag();
    }

    @Schema(name = "ProxyBootstrapWithRegistriesRequest")
    @JsonPropertyOrder({"hostId", "parentFqdn", "proxyPort", "maxCache", "email", "rootCA", "intermediateCAs",
        "proxyCert", "proxyKey", "registryHttpdURL", "registryHttpdTag", "registrySaltbrokerURL",
        "registrySaltbrokerTag", "registrySquidURL", "registrySquidTag", "registrySshURL", "registrySshTag",
        "registryTftpdURL", "registryTftpdTag"})
    interface BootstrapProxyWithRegistriesRequest extends BootstrapProxyRequest {

        /**
         * @return the httpd image registry
         */
        @Schema(name = "registryHttpdURL", description = "Httpd image registry",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryHttpdURL();

        /**
         * @return the httpd image tag
         */
        @Schema(description = "Httpd image tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryHttpdTag();

        /**
         * @return the salt broker image registry
         */
        @Schema(name = "registrySaltbrokerURL", description = "Salt broker image registry",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySaltbrokerURL();

        /**
         * @return the salt broker image tag
         */
        @Schema(description = "Salt broker image tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySaltbrokerTag();

        /**
         * @return the squid image registry
         */
        @Schema(name = "registrySquidURL", description = "Squid image registry",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySquidURL();

        /**
         * @return the squid image tag
         */
        @Schema(description = "Squid image tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySquidTag();

        /**
         * @return the ssh image registry
         */
        @Schema(name = "registrySshURL", description = "Ssh image registry",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySshURL();

        /**
         * @return the ssh image tag
         */
        @Schema(description = "Ssh image tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistrySshTag();

        /**
         * @return the tftpd image registry
         */
        @Schema(name = "registryTftpdURL", description = "Tftpd image registry",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryTftpdURL();

        /**
         * @return the tftpd image tag
         */
        @Schema(description = "Tftpd image tag", requiredMode = Schema.RequiredMode.REQUIRED)
        String getRegistryTftpdTag();
    }

    @Schema(name = "ProxyBackupConfigurationRequest")
    interface BackupConfigurationRequest {

        /**
         * @return the ids of the proxies to back up
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<Integer> getSids();
    }

    @Schema(name = "ShortSystemInfo", description = "system")
    @JsonPropertyOrder({"id", "name", "lastCheckin", "created", "lastBoot"})
    interface ShortSystemInfoDoc {

        /**
         * @return the system id
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Integer getId();

        /**
         * @return the system name
         */
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String getName();

        /**
         * @return the last time the server successfully checked in
         */
        @Schema(name = "last_checkin", description = "last time server\n        successfully checked in",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastCheckin();

        /**
         * @return the server registration time
         */
        @Schema(description = "server registration time", requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getCreated();

        /**
         * @return the last server boot time
         */
        @Schema(name = "last_boot", description = "last server boot time",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @LegacyDocResponse(type = "dateTime.iso8601")
        Date getLastBoot();
    }
}
