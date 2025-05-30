/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.proxy;

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.ProxyConfigUtils.USE_CERTS_MODE_REPLACE;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.UyuniGeneralException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidProxyVersionException;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.NotSupportedException;
import com.redhat.rhn.frontend.xmlrpc.ProxyAlreadyRegisteredException;
import com.redhat.rhn.frontend.xmlrpc.ProxyMissingEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.ProxyNotActivatedException;
import com.redhat.rhn.frontend.xmlrpc.ProxySystemIsSatelliteException;
import com.redhat.rhn.frontend.xmlrpc.SSLCertFaultException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.update.ProxyConfigUpdateFacade;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ProxyHandler
 * @apidoc.namespace proxy
 * @apidoc.doc Provides methods to activate/deactivate a proxy
 * server.
 */
public class ProxyHandler extends BaseHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyHandler.class);
    private final XmlRpcSystemHelper xmlRpcSystemHelper;
    private final SystemManager systemManager;
    private final ProxyConfigUpdateFacade proxyConfigUpdateFacade;

    /**
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     * @param systemManagerIn the system manager
     * @param proxyConfigUpdateFacadeIn the proxy config update facade
     */
    public ProxyHandler(XmlRpcSystemHelper xmlRpcSystemHelperIn,
                        SystemManager systemManagerIn,
                        ProxyConfigUpdateFacade proxyConfigUpdateFacadeIn) {
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
        systemManager = systemManagerIn;
        proxyConfigUpdateFacade = proxyConfigUpdateFacadeIn;
    }


    /**
     * Create Monitoring Scout for proxy.
     * Implemented due to to backward compatibility
     * @param clientcert client certificate of the system.
     * @return string - actually an exception is thrown everytime
     * @throws NotSupportedException thrown everytime as this call is no longer supported
     * @since 10.7
     *
     * @apidoc.doc Create Monitoring Scout for proxy.
     * @apidoc.param #param_desc("string", "clientcert", "client certificate file")
     * @apidoc.returntype #param("string", "")
     */
    public String createMonitoringScout(String clientcert)
        throws NotSupportedException {
        throw new NotSupportedException();
    }

    /**
     * Test, if the system identified by the given client certificate, is proxy.
     * @param clientcert client certificate of the system.
     * @return 1 if system is proxy, 0 otherwise.
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     *
     * @apidoc.doc Test, if the system identified by the given client
     * certificate i.e. systemid file, is proxy.
     * @apidoc.param #param_desc("string", "clientcert", "client certificate file")
     * @apidoc.returntype #return_int_success()
     */
    @ReadOnly
    public int isProxy(String clientcert)
        throws MethodInvalidParamException {
        Server server = validateClientCertificate(clientcert);
        return (server.isProxy() ? 1 : 0);
    }

    /**
     * Deactivates the system identified by the given client certificate.
     * @param clientcert client certificate of the system.
     * @return 1 if the deactivation succeeded, 0 otherwise.
     * @throws ProxyNotActivatedException thrown if server is not a proxy.
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     *
     * @apidoc.doc Deactivates the proxy identified by the given client
     * certificate i.e. systemid file.
     * @apidoc.param #param_desc("string", "clientcert", "client certificate file")
     * @apidoc.returntype #return_int_success()
     */
    public int deactivateProxy(String clientcert)
        throws ProxyNotActivatedException, MethodInvalidParamException {
        Server server = validateClientCertificate(clientcert);
        if (!server.isProxy()) {
            throw new ProxyNotActivatedException();
        }

        SystemManager.deactivateProxy(server);
        return 1;
    }

    /**
     * Activates the proxy identified by the given client certificate.
     * @param clientcert client certificate of the system.
     * @param version Proxy version
     * @return 1 if the deactivation succeeded, 0 otherwise.
     * @throws ProxyAlreadyRegisteredException thrown if system has already been
     * registered.
     * @throws MethodInvalidParamException thrown if certificate is invalid.
     * @throws ProxySystemIsSatelliteException thrown if client certificate is
     * for a satellite
     * @throws InvalidProxyVersionException thrown if version is not supported.
     * @throws ProxyMissingEntitlementException thrown if system does not have the
     * management entitlement.
     *
     * @apidoc.doc Activates the proxy identified by the given client
     * certificate i.e. systemid file.
     * @apidoc.param #param_desc("string", "clientcert", "client certificate file")
     * @apidoc.param #param_desc("string", "version", "Version of proxy to be
     * registered.")
     * @apidoc.returntype #return_int_success()
     */
    public int activateProxy(String clientcert, String version)
        throws ProxyAlreadyRegisteredException, MethodInvalidParamException,
               ProxySystemIsSatelliteException, InvalidProxyVersionException {

        Server server = validateClientCertificate(clientcert);
        if (server.isProxy()) {
            throw new ProxyAlreadyRegisteredException();
        }

        if (!(server.hasEntitlement(EntitlementManager.MANAGEMENT) || server.hasEntitlement(EntitlementManager.SALT))) {
            throw new ProxyMissingEntitlementException();
        }

        SystemManager.activateProxy(server, version);
        return 1;
    }

    /**
     * List available version of proxy channel for the system.
     * @param clientcert client certificate of the system.
     * @return 1 if the deactivation succeeded, 0 otherwise.
     * @since 10.5
     *
     * @apidoc.doc List available version of proxy channel for system
     * identified by the given client certificate i.e. systemid file.
     * @apidoc.param #param_desc("string", "clientcert", "client certificate file")
     * @apidoc.returntype  #array_single ("string", "version")
     */
    @ReadOnly
    public List<String> listAvailableProxyChannels(String clientcert) {

        Server server = validateClientCertificate(clientcert);

        ChannelFamily proxyFamily = ChannelFamilyFactory
            .lookupByLabel(ChannelFamilyFactory
                .PROXY_CHANNEL_FAMILY_LABEL,
                null);


        if (proxyFamily == null ||
                proxyFamily.getChannels() == null ||
                proxyFamily.getChannels().isEmpty()) {
            return Collections.emptyList();
        }

        /* We search for a proxy channel whose parent channel is our server's basechannel.
         * This will be the channel we attempt to subscribe the server to.
         */
        Channel baseChannel = server.getBaseChannel();
        return proxyFamily.getChannels().stream()
                .filter(p -> p.getProduct() != null)
                .filter(p -> baseChannel.equals(p.getParentChannel()) || baseChannel.equals(p))
                .map(p -> p.getProduct().getVersion())
                .collect(toList());
    }

    /**
     * List the proxies within the user's organization.
     * @param loggedInUser The current user
     * @return  list of Maps containing "id", "name", and "last_checkin"
     *
     * @apidoc.doc List the proxies within the user's organization.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ShortSystemInfoSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listProxies(User loggedInUser) {
        List<Server> proxies = ServerFactory.lookupProxiesByOrg(loggedInUser);
        List<Object> toReturn = new ArrayList<>();
        for (Server server : proxies) {
            toReturn.add(xmlRpcSystemHelper.format(server));
        }
        return toReturn.toArray();
    }

    /**
     * List the clients directly connected to a Proxy.
     *
     * @param loggedInUser the current user
     * @param proxyId the ID of the Proxy
     * @return list of client IDs
     *
     * @apidoc.doc List the clients directly connected to a given Proxy.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "proxyId", "the Proxy ID")
     * @apidoc.returntype #array_single("int", "clientId")
     */
    @ReadOnly
    public List<Long> listProxyClients(User loggedInUser, Integer proxyId) {
        Server server = xmlRpcSystemHelper.lookupServer(loggedInUser, proxyId);
        if (!server.isProxy()) {
            throw new ProxyNotActivatedException();
        }
        return SystemManager.listClientsThroughProxy(server.getId()).stream()
                .map(SystemOverview::getId)
                .collect(toList());
    }

    /**
     * Create and provide proxy container configuration with existing proxy certificate and key.
     *
     * @param loggedInUser the current user
     * @param proxyName  the FQDN of the proxy
     * @param proxyPort the SSH port the proxy listens on
     * @param server the FQDN of the server the proxy uses
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate in PEM format
     * @param proxyCrt proxy CRT content in PEM format
     * @param proxyKey proxy SSL private key in PEM format
     * @return the configuration file
     *
     * @apidoc.doc Compute and download the configuration for proxy containers
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "proxyName", "The FQDN of the proxy")
     * @apidoc.param #param_desc("int", "proxyPort", "The SSH port the proxy listens on")
     * @apidoc.param #param_desc("string", "server", "The server FQDN the proxy will connect to")
     * @apidoc.param #param_desc("int", "maxCache", "Max cache size in MB")
     * @apidoc.param #param_desc("string", "email", "The proxy admin email")
     * @apidoc.param #param_desc("string", "rootCA", "The root CA used to sign the SSL certificate in PEM format")
     * @apidoc.param #array_single_desc("string", "intermediateCAs",
     *                                  "intermediate CAs used to sign the SSL certificate in PEM format")
     * @apidoc.param #param_desc("string", "proxyCrt", "proxy CRT content in PEM format")
     * @apidoc.param #param_desc("string", "proxyKey", "proxy SSL private key in PEM format")
     *  @apidoc.returntype #array_single("byte", "binary object - package file")
     */
    public byte[] containerConfig(User loggedInUser, String proxyName, Integer proxyPort, String server,
                                  Integer maxCache, String email,
                                  String rootCA, List<String> intermediateCAs, String proxyCrt, String proxyKey) {
        try {
            SSLCertPair proxyCrtKey = new SSLCertPair(proxyCrt, proxyKey);
            if (proxyCrtKey.isInvalid()) {
                throw new InvalidParameterException("Both proxyCrt and proxyKey need to be provided");
            }

            return systemManager.createProxyContainerConfig(loggedInUser, proxyName, proxyPort, server,
                    maxCache.longValue(), email, rootCA, intermediateCAs, proxyCrtKey, null, null, null,
                    new SSLCertManager());
        }
        catch (SSLCertGenerationException e) {
            LOG.error("Failed to generate SSL certificate", e);
            throw new SSLCertFaultException(e.getMessage());
        }
    }

    /**
     * Create and provide proxy container configuration, generate the proxy certificate and key.
     *
     * @param loggedInUser the current user
     * @param proxyName  the FQDN of the proxy
     * @param proxyPort the SSH port the proxy listens on
     * @param server the FQDN of the server the proxy uses
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param caCrt CA certificate to use to sign the SSL certificate in PEM format
     * @param caKey CA private key to use to sign the SSL certificate in PEM format
     * @param caPassword the CA private key password
     * @param cnames proxy alternate cnames to set in the SSL certificate
     * @param country the 2-letter country code to set in the SSL certificate
     * @param state the state to set in the SSL certificate
     * @param city the city to set in the SSL certificate
     * @param org the organization to set in the SSL certificate
     * @param orgUnit the organization unit to set in the SSL certificate
     * @param sslEmail the email to set in the SSL certificate
     *
     * @return the configuration file
     *
     * @apidoc.doc Compute and download the configuration for proxy containers
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "proxyName", "The FQDN of the proxy")
     * @apidoc.param #param_desc("int", "proxyPort", "The SSH port the proxy listens on")
     * @apidoc.param #param_desc("string", "server", "The server FQDN the proxy will connect to")
     * @apidoc.param #param_desc("int", "maxCache", "Max cache size in MB")
     * @apidoc.param #param_desc("string", "email", "The proxy admin email")
     * @apidoc.param #param_desc("string", "caCrt", "CA certificate to use to sign the SSL certificate in PEM format")
     * @apidoc.param #param_desc("string", "caKey", "CA private key to use to sign the SSL certificate in PEM format")
     * @apidoc.param #param_desc("string", "caPassword", "The CA private key password")
     * @apidoc.param #array_single_desc("string", "cnames", "Proxy alternate cnames to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "country", "The 2-letter country code to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "state", "The state to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "city", "The city to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "org", "The organization to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "orgUnit", "The organization unit to set in the SSL certificate")
     * @apidoc.param #param_desc("string", "sslEmail", "The email to set in the SSL certificate")
     *  @apidoc.returntype #array_single("byte", "binary object - package file")
     */
    public byte[] containerConfig(User loggedInUser, String proxyName, Integer proxyPort, String server,
                                  Integer maxCache, String email,
                                  String caCrt, String caKey, String caPassword,
                                  List<String> cnames, String country, String state, String city,
                                  String org, String orgUnit, String sslEmail) {
        try {
            SSLCertPair caCrtKey = new SSLCertPair(caCrt, caKey);
            if (caCrtKey.isInvalid()) {
                throw new InvalidParameterException("Both caCrt and caKey need to be provided");
            }

            SSLCertData certData = new SSLCertData(nullable(proxyName), cnames, nullable(country),
                    nullable(state), nullable(city), nullable(org), nullable(orgUnit), nullable(sslEmail));
            return systemManager.createProxyContainerConfig(loggedInUser, proxyName, proxyPort, server,
                    maxCache.longValue(), email, null, List.of(), null, caCrtKey, caPassword, certData,
                    new SSLCertManager());
        }
        catch (SSLCertGenerationException e) {
            LOG.error("Failed to generate SSL certificate", e);
            throw new SSLCertFaultException(e.getMessage());
        }
    }


    /**
     * Deploy a proxy container on given salt minion. It expects that the images are installed as RPMs.
     *
     * @param loggedInUser the current user
     * @param hostId the ID of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA CA certificate in PEM format
     * @param intermediateCAs a list of intermediate CAs in PEM format
     * @param proxyCert proxy certificate in PEM format
     * @param proxyKey proxy private key in PEM format
     *
     * @return 1 on success
     *
     * @apidoc.doc Deploy a proxy container on given salt minion. It expects that the images are installed as RPMs.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "hostId", "The ID of the target minion")
     * @apidoc.param #param_desc("string", "parentFqdn", "The FQDN of the server the proxy uses")
     * @apidoc.param #param_desc("int", "proxyPort", "The SSH port the proxy listens on")
     * @apidoc.param #param_desc("int", "maxCache", "The maximum memory cache size")
     * @apidoc.param #param_desc("string", "email", "The email of proxy admin")
     * @apidoc.param #param_desc("string", "rootCA", "CA certificate in PEM format")
     * @apidoc.param #array_single_desc("string", "intermediateCAs", "A list of intermediate CAs in PEM format")
     * @apidoc.param #param_desc("string", "proxyCert", "Proxy certificate in PEM format")
     * @apidoc.param #param_desc("string", "proxyKey", "Proxy private key in PEM format")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapProxy(User loggedInUser,
        Integer hostId, String parentFqdn,
        Integer proxyPort, Integer maxCache, String email,
        String rootCA, List<String> intermediateCAs, String proxyCert, String proxyKey) {

        return bootstrapProxy(
            loggedInUser,
            hostId, parentFqdn,
            proxyPort, maxCache, email,
            rootCA, intermediateCAs, proxyCert, proxyKey,
            SOURCE_MODE_RPM,
            null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null);
    }

    /**
     * Deploy a proxy container on given salt minion. Use the given registry for the images.
     *
     * @param loggedInUser the current user
     * @param hostId the ID of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA CA certificate in PEM format
     * @param intermediateCAs a list of intermediate CAs in PEM format
     * @param proxyCert proxy certificate in PEM format
     * @param proxyKey proxy private key in PEM format
     * @param registryBaseURL image registry (e.g. "https://registry.opensuse.org/uyuni/")
     * @param registryBaseTag image tag (e.g. "latest")
     *
     * @return 1 on success
     *
     * @apidoc.doc Deploy a proxy container on given salt minion. Use the given registry for the images.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "hostId", "The ID of the target minion")
     * @apidoc.param #param_desc("string", "parentFqdn", "The FQDN of the server the proxy uses")
     * @apidoc.param #param_desc("int", "proxyPort", "The SSH port the proxy listens on")
     * @apidoc.param #param_desc("int", "maxCache", "The maximum memory cache size")
     * @apidoc.param #param_desc("string", "email", "The email of proxy admin")
     * @apidoc.param #param_desc("string", "rootCA", "CA certificate in PEM format")
     * @apidoc.param #array_single_desc("string", "intermediateCAs", "A list of intermediate CAs in PEM format")
     * @apidoc.param #param_desc("string", "proxyCert", "Proxy certificate in PEM format")
     * @apidoc.param #param_desc("string", "proxyKey", "Proxy private key in PEM format")
     * @apidoc.param #param_desc("string", "registryBaseURL",
     *                           "Image registry (e.g. https://registry.opensuse.org/uyuni/)")
     * @apidoc.param #param_desc("string", "registryBaseTag", "Image tag (e.g. latest)")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapProxy(User loggedInUser,
        Integer hostId, String parentFqdn,
        Integer proxyPort, Integer maxCache, String email,
        String rootCA, List<String> intermediateCAs, String proxyCert, String proxyKey,
        String registryBaseURL, String registryBaseTag) {

        return bootstrapProxy(
            loggedInUser,
            hostId, parentFqdn,
            proxyPort, maxCache, email,
            rootCA, intermediateCAs, proxyCert, proxyKey,
            SOURCE_MODE_REGISTRY,
            REGISTRY_MODE_SIMPLE,
            registryBaseURL, registryBaseTag,
            null, null,
            null, null,
            null, null,
            null, null,
            null, null);
    }

    /**
     * Deploy a proxy container on given salt minion. Allows individual registry for each image.
     *
     * @param loggedInUser the current user
     * @param hostId the ID of the target minion
     * @param parentFqdn the FQDN of the server the proxy uses
     * @param proxyPort the SSH port the proxy listens on
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA CA certificate in PEM format
     * @param intermediateCAs a list of intermediate CAs in PEM format
     * @param proxyCert proxy certificate in PEM format
     * @param proxyKey proxy private key in PEM format
     * @param registryHttpdURL Httpd image registry
     * @param registryHttpdTag Httpd image tag
     * @param registrySaltbrokerURL Salt broker image registry
     * @param registrySaltbrokerTag Salt broker image tag
     * @param registrySquidURL Squid image registry
     * @param registrySquidTag Squid image tag
     * @param registrySshURL Ssh image registry
     * @param registrySshTag Ssh image tag
     * @param registryTftpdURL Tftpd image registry
     * @param registryTftpdTag Tftpd image tag
     *
     * @return 1 on success
     *
     * @apidoc.doc Deploy a proxy container on given salt minion. Allows individual registry for each image.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "hostId", "The ID of the target minion")
     * @apidoc.param #param_desc("string", "parentFqdn", "The FQDN of the server the proxy uses")
     * @apidoc.param #param_desc("int", "proxyPort", "The SSH port the proxy listens on")
     * @apidoc.param #param_desc("int", "maxCache", "The maximum memory cache size")
     * @apidoc.param #param_desc("string", "email", "The email of proxy admin")
     * @apidoc.param #param_desc("string", "rootCA", "CA certificate in PEM format")
     * @apidoc.param #array_single_desc("string", "intermediateCAs", "A list of intermediate CAs in PEM format")
     * @apidoc.param #param_desc("string", "proxyCert", "Proxy certificate in PEM format")
     * @apidoc.param #param_desc("string", "proxyKey", "Proxy private key in PEM format")
     * @apidoc.param #param_desc("string", "registryHttpdURL", "Httpd image registry")
     * @apidoc.param #param_desc("string", "registryHttpdTag", "Httpd image tag")
     * @apidoc.param #param_desc("string", "registrySaltbrokerURL", "Salt broker image registry")
     * @apidoc.param #param_desc("string", "registrySaltbrokerTag", "Salt broker image tag")
     * @apidoc.param #param_desc("string", "registrySquidURL", "Squid image registry")
     * @apidoc.param #param_desc("string", "registrySquidTag", "Squid image tag")
     * @apidoc.param #param_desc("string", "registrySshURL", "Ssh image registry")
     * @apidoc.param #param_desc("string", "registrySshTag", "Ssh image tag")
     * @apidoc.param #param_desc("string", "registryTftpdURL", "Tftpd image registry")
     * @apidoc.param #param_desc("string", "registryTftpdTag", "Tftpd image tag")
     * @apidoc.returntype #return_int_success()
     */
    public int bootstrapProxy(User loggedInUser,
        Integer hostId, String parentFqdn,
        Integer proxyPort, Integer maxCache, String email,
        String rootCA, List<String> intermediateCAs, String proxyCert, String proxyKey,
        String registryHttpdURL, String registryHttpdTag,
        String registrySaltbrokerURL, String registrySaltbrokerTag,
        String registrySquidURL, String registrySquidTag,
        String registrySshURL, String registrySshTag,
        String registryTftpdURL, String registryTftpdTag
        ) {

        return bootstrapProxy(
            loggedInUser,
            hostId, parentFqdn,
            proxyPort, maxCache, email,
            rootCA, intermediateCAs, proxyCert, proxyKey,
            SOURCE_MODE_REGISTRY,
            REGISTRY_MODE_ADVANCED,
            null, null,
            registryHttpdURL, registryHttpdTag,
            registrySaltbrokerURL, registrySaltbrokerTag,
            registrySquidURL, registrySquidTag,
            registrySshURL, registrySshTag,
            registryTftpdURL, registryTftpdTag);
    }

    private int bootstrapProxy(User loggedInUser,
        Integer hostId, String parentFqdn,
        Integer proxyPort, Integer maxCache, String email,
        String rootCA, List<String> intermediateCAs, String proxyCert, String proxyKey,
        String sourceMode,
        String registryMode,
        String registryBaseURL, String registryBaseTag,
        String registryHttpdURL, String registryHttpdTag,
        String registrySaltbrokerURL, String registrySaltbrokerTag,
        String registrySquidURL, String registrySquidTag,
        String registrySshURL, String registrySshTag,
        String registryTftpdURL, String registryTftpdTag
        ) {

        try {
            ProxyConfigUpdateJson request = new ProxyConfigUpdateJson(
                hostId.longValue(), parentFqdn,
                proxyPort, maxCache, email,
                USE_CERTS_MODE_REPLACE,
                rootCA, intermediateCAs, proxyCert, proxyKey,
                sourceMode,
                registryMode,
                registryBaseURL, registryBaseTag,
                registryHttpdURL, registryHttpdTag,
                registrySaltbrokerURL, registrySaltbrokerTag,
                registrySquidURL, registrySquidTag,
                registrySshURL, registrySshTag,
                registryTftpdURL, registryTftpdTag
            );

            proxyConfigUpdateFacade.update(request, systemManager, loggedInUser);
        }
        catch (RhnRuntimeException | UyuniGeneralException e) {
            LOG.error("Failed to apply proxy configuration to minion", e);
            throw new ValidationException(e.getMessage());
        }
        return 1;
    }

    private String nullable(String value) {
        if ("".equals(value)) {
            return null;
        }
        return value;
    }
}
