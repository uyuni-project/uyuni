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
 */

package com.suse.manager.hub;

import com.suse.manager.xmlrpc.iss.RestHubExternalClient;
import com.suse.utils.CertificateUtils;

import java.io.IOException;
import java.security.cert.CertificateException;

public class HubClientFactory {

    /**
     * Creates a new ISS client for the internal API
     * @param fqdn the FQDN of the remote server
     * @param token the token to access the server
     * @param rootCA the root certificate, if needed
     * @return a client to invoke the internal APIs
     * @throws CertificateException when the certificate is not parseable
     */
    public HubInternalClient newInternalClient(String fqdn, String token, String rootCA) throws CertificateException {
        return new DefaultHubInternalClient(fqdn, token, CertificateUtils.parse(rootCA));
    }

    /**
     * Creates a new ISS client for the internal API
     * @param fqdn the FQDN of the remote server
     * @param username the username
     * @param password the password
     * @param rootCA the root certificate, if needed
     * @return a client to invoke the internal APIs
     * @throws IOException when the client cannot be created
     * @throws CertificateException when the certificate is not parseable
     */
    public HubExternalClient newExternalClient(String fqdn, String username, String password, String rootCA)
        throws CertificateException, IOException {
        return new RestHubExternalClient(fqdn, username, password, CertificateUtils.parse(rootCA));
    }

}
