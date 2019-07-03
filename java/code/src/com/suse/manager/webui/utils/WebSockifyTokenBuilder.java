/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.utils;

import org.jose4j.jwt.JwtClaims;

/**
 * Utility functions to generate WebSockify tokens.
 */
public class WebSockifyTokenBuilder extends TokenBuilder {

    private String host;

    private int port;

    /**
     * Construct a token builder
     * @param hostIn host to connect to
     * @param portIn port to connect to
     */
    public WebSockifyTokenBuilder(String hostIn, int portIn) {
        this.host = hostIn;
        this.port = portIn;
        setExpirationTimeMinutesInTheFuture(5);
    }

    @Override
    public JwtClaims getClaims() {
        JwtClaims claims = super.getClaims();
        claims.setClaim("host", this.host);
        claims.setClaim("port", this.port);
        return claims;
    }
}
