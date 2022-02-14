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

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.IOFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidProxyVersionException;
import com.redhat.rhn.frontend.xmlrpc.MethodInvalidParamException;
import com.redhat.rhn.frontend.xmlrpc.NotSupportedException;
import com.redhat.rhn.frontend.xmlrpc.ProxyAlreadyRegisteredException;
import com.redhat.rhn.frontend.xmlrpc.ProxyMissingEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.ProxyNotActivatedException;
import com.redhat.rhn.frontend.xmlrpc.ProxySystemIsSatelliteException;
import com.redhat.rhn.frontend.xmlrpc.SSLCertFaultException;
import com.redhat.rhn.frontend.xmlrpc.SystemIdInstantiationException;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertPair;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ProxyHandler
 * @xmlrpc.namespace proxy
 * @xmlrpc.doc Provides methods to activate/deactivate a proxy
 * server.
 */
public class ProxyHandler extends BaseHandler {
    private static final Logger LOG = Logger.getLogger(ProxyHandler.class);
    private final XmlRpcSystemHelper xmlRpcSystemHelper;
    private final SystemManager systemManager;

    /**
     * @param xmlRpcSystemHelperIn XmlRpcSystemHelper
     * @param systemManagerIn the system manager
     */
    public ProxyHandler(XmlRpcSystemHelper xmlRpcSystemHelperIn,
                        SystemManager systemManagerIn) {
        xmlRpcSystemHelper = xmlRpcSystemHelperIn;
        systemManager = systemManagerIn;
    }


    /**
     * Create Monitoring Scout for proxy.
     * Implemented due to to backward compatibility
     * @param clientcert client certificate of the system.
     * @return string - actually an exception is thrown everytime
     * @throws NotSupportedException thrown everytime as this call is no longer supported
     * @since 10.7
     *
     * @xmlrpc.doc Create Monitoring Scout for proxy.
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype #param("string", "")
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
     * @xmlrpc.doc Test, if the system identified by the given client
     * certificate i.e. systemid file, is proxy.
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype #return_int_success()
     */
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
     * @xmlrpc.doc Deactivates the proxy identified by the given client
     * certificate i.e. systemid file.
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype #return_int_success()
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
     * @xmlrpc.doc Activates the proxy identified by the given client
     * certificate i.e. systemid file.
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.param #param_desc("string", "version", "Version of proxy to be
     * registered.")
     * @xmlrpc.returntype #return_int_success()
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
     * @xmlrpc.doc List available version of proxy channel for system
     * identified by the given client certificate i.e. systemid file.
     * @xmlrpc.param #param_desc("string", "systemid", "systemid file")
     * @xmlrpc.returntype  #array_single ("string", "version")
     */
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
     * @xmlrpc.doc List the proxies within the user's organization.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     * #array_begin()
     *   $SystemOverviewSerializer
     * #array_end()
     */
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
     * @xmlrpc.doc List the clients directly connected to a given Proxy.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("int","proxyId","the Proxy ID")
     * @xmlrpc.returntype #array_single("int", "clientId")
     */
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
     * @param server the FQDN of the server the proxy uses
     * @param maxCache the maximum memory cache size
     * @param email the email of proxy admin
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate in PEM format
     * @param proxyCrt proxy CRT content in PEM format
     * @param proxyKey proxy SSL private key in PEM format
     * @return the configuration file
     *
     * @xmlrpc.doc Compute and download the configuration for proxy containers
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "proxyName", "The FQDN of the proxy")
     * @xmlrpc.param #param("string", "server", "The server FQDN the proxy will connect to")
     * @xmlrpc.param #param("int", "maxCache", "Max cache size in MB")
     * @xmlrpc.param #param("string", "email", "The proxy admin email")
     * @xmlrpc.param #param("string", "rootCA", "The root CA used to sign the SSL certificate in PEM format")
     * @xmlrpc.param #array_single("string", "intermediate CAs used to sign the SSL certificate in PEM format")
     * @xmlrpc.param #param("string", "proxyCrt", "proxy CRT content in PEM format")
     * @xmlrpc.param #param("string", "proxyKey", "proxy SSL private key in PEM format")
     *  @xmlrpc.returntype #array_single("byte", "binary object - package file")
     */
    public byte[] containerConfig(User loggedInUser, String proxyName, String server, Integer maxCache, String email,
                                  String rootCA, List<String> intermediateCAs, String proxyCrt, String proxyKey) {
        try {
            SSLCertPair proxyCrtKey = new SSLCertPair(proxyCrt, proxyKey);
            if (proxyCrtKey.isInvalid()) {
                throw new InvalidParameterException("Both proxyCrt and proxyKey need to be provided");
            }

            return systemManager.createProxyContainerConfig(loggedInUser, proxyName, server, maxCache.longValue(),
                    email, rootCA, intermediateCAs, proxyCrtKey, null, null, null);
        }
        catch (InstantiationException e) {
            LOG.error("Failed to generate proxy system id", e);
            throw new SystemIdInstantiationException();
        }
        catch (IOException e) {
            LOG.error("Failed to generate container config", e);
            throw new IOFaultException(e);
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
     * @xmlrpc.doc Compute and download the configuration for proxy containers
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param("string", "proxyName", "The FQDN of the proxy")
     * @xmlrpc.param #param("string", "server", "The server FQDN the proxy will connect to")
     * @xmlrpc.param #param("int", "maxCache", "Max cache size in MB")
     * @xmlrpc.param #param("string", "email", "The proxy admin email")
     * @xmlrpc.param #param("string", "caCrt", "CA certificate to use to sign the SSL certificate in PEM format")
     * @xmlrpc.param #param("string", "caKey", "CA private key to use to sign the SSL certificate in PEM format")
     * @xmlrpc.param #param("string", "caPassword", "The CA private key password")
     * @xmlrpc.param #param("string", "cnames", "Proxy alternate cnames to set in the SSL certificate")
     * @xmlrpc.param #param("string", "country", "The 2-letter country code to set in the SSL certificate")
     * @xmlrpc.param #param("string", "state", "The state to set in the SSL certificate")
     * @xmlrpc.param #param("string", "city", "The city to set in the SSL certificate")
     * @xmlrpc.param #param("string", "org", "The organization to set in the SSL certificate")
     * @xmlrpc.param #param("string", "orgUnit", "The organization unit to set in the SSL certificate")
     * @xmlrpc.param #param("string", "sslEmail", "The email to set in the SSL certificate")
     *  @xmlrpc.returntype #array_single("byte", "binary object - package file")
     */
    public byte[] containerConfig(User loggedInUser, String proxyName, String server, Integer maxCache, String email,
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
            return systemManager.createProxyContainerConfig(loggedInUser, proxyName, server, maxCache.longValue(),
                    email, null, Collections.emptyList(), null, caCrtKey, caPassword, certData);
        }
        catch (InstantiationException e) {
            LOG.error("Failed to generate proxy system id", e);
            throw new SystemIdInstantiationException();
        }
        catch (IOException e) {
            LOG.error("Failed to generate container config", e);
            throw new IOFaultException(e);
        }
        catch (SSLCertGenerationException e) {
            LOG.error("Failed to generate SSL certificate", e);
            throw new SSLCertFaultException(e.getMessage());
        }
    }

    private String nullable(String value) {
        if ("".equals(value)) {
            return null;
        }
        return value;
    }
}
