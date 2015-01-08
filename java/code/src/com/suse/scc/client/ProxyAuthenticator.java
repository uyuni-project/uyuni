/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.scc.client;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Basic authentication for HTTP proxies.
 */
public class ProxyAuthenticator extends Authenticator {

    private final String user;
    private final String password;

    /**
     * Constructor taking the proxy credentials.
     *
     * @param userIn proxy username
     * @param passwordIn proxy password
     */
    public ProxyAuthenticator(String userIn, String passwordIn) {
        user = userIn;
        password = passwordIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        if (getRequestorType().equals(RequestorType.PROXY)) {
            return new PasswordAuthentication(user, password.toCharArray());
        }
        return null;
    }
}
