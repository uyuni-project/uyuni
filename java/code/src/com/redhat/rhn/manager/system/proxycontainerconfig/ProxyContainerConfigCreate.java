/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.manager.system.proxycontainerconfig;

import static java.util.Arrays.asList;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.iface.SaltApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Main handler for creating proxy container configuration files.
 * Three files should be created and compressed into a tarball:
 *  - config.yaml : yaml configuration file that contains the server FQDN, max cache size, email, server version,
 *                  proxy FQDN, and root CA
 *  - httpd.yaml : yaml file containing the system ID, server certificate, and server key
 *  - ssh.yaml : yaml file containing the server SSH public key, proxy SSH key, and proxy SSH public key
 *
 * This flow is divided into three main steps:
 * - Acquire and validate all necessary data
 * - Compute contents for files
 * - Create tar archive with all necessary files
 *
 */
public class ProxyContainerConfigCreate {
    private final List<ProxyContainerConfigCreateContextHandler> contextHandlerChain = new ArrayList<>();

    /**
     * Constructor
     */
    public ProxyContainerConfigCreate() {
        this.contextHandlerChain.addAll(asList(
                new ProxyContainerConfigCreateAcquisitor(),
                new ProxyContainerConfigCreateGenerateFileMaps(),
                new ProxyContainerConfigCreateTarCreate()
        ));
    }

    /**
     * Create and provide proxy container configuration.
     *
     * @param saltApi                  the Salt API instance
     * @param systemEntitlementManager the system entitlement manager instance
     * @param user                     the current user
     * @param serverFqdn               the FQDN of the server the proxy uses
     * @param proxyFqdn                the FQDN of the proxy
     * @param proxyPort                the SSH port the proxy listens on
     * @param maxCache                 the maximum memory cache size
     * @param email                    the email of proxy admin
     * @param rootCA                   root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs          intermediate CAs used to sign the SSL certificate in PEM format
     * @param proxyCertKey             proxy CRT and key pair
     * @param caPair                   the CA certificate and key used to sign the certificate to generate.
     *                                 Can be omitted if proxyCertKey is not provided
     * @param caPassword               the CA private key password.
     *                                 Can be omitted if proxyCertKey is not provided
     * @param certData                 the data needed to generate the new proxy SSL certificate.
     *                                 Can be omitted if proxyCertKey is not provided
     * @param certManager              the SSLCertManager to use
     * @return the configuration file
     */
    public byte[] create(
            SaltApi saltApi, SystemEntitlementManager systemEntitlementManager, User user,
            String serverFqdn, String proxyFqdn, Integer proxyPort, Long maxCache, String email,
            String rootCA, List<String> intermediateCAs, SSLCertPair proxyCertKey,
            SSLCertPair caPair, String caPassword, SSLCertData certData, SSLCertManager certManager
    ) {
        ProxyContainerConfigCreateContext context = new ProxyContainerConfigCreateContext(
                saltApi, user, systemEntitlementManager, serverFqdn, proxyFqdn, proxyPort, maxCache, email, rootCA,
                intermediateCAs, proxyCertKey, caPair, caPassword, certData, certManager
        );
        for (ProxyContainerConfigCreateContextHandler handler : contextHandlerChain) {
            handler.handle(context);
        }
        return context.getConfigTar();
    }

}
