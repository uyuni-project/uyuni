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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.manager.system.proxycontainerconfig;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import com.suse.manager.ssl.SSLCertData;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.iface.SaltApi;

import java.util.List;
import java.util.Map;

public interface ProxyContainerConfigCreateFacade {

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
     * @return the tarball configuration file as a byte array
     */
    byte[] create(
            SaltApi saltApi, SystemEntitlementManager systemEntitlementManager, User user,
            String serverFqdn, String proxyFqdn, Integer proxyPort, Long maxCache, String email,
            String rootCA, List<String> intermediateCAs, SSLCertPair proxyCertKey,
            SSLCertPair caPair, String caPassword, SSLCertData certData, SSLCertManager certManager
    );

    /**
     * Create and provide proxy container configuration files.
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
     * @param sshPub                   the proxy SSH public key if known
     * @param sshPriv                  the proxy SSH private key if known
     * @param sshParent                the parent SSH public key if known
     * @return the configuration files as a map
     */
    Map<String, Object> createFiles(
            SaltApi saltApi, SystemEntitlementManager systemEntitlementManager, User user,
            String serverFqdn, String proxyFqdn, Integer proxyPort, Long maxCache, String email,
            String rootCA, List<String> intermediateCAs, SSLCertPair proxyCertKey,
            SSLCertPair caPair, String caPassword, SSLCertData certData, SSLCertManager certManager,
            String sshPub, String sshPriv, String sshParent
    );

}
