/*
 * Copyright (c) 2019--2024 SUSE LLC
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
package com.suse.manager.webui.utils.token;

import org.jose4j.jwt.JwtClaims;

/**
 * Utility functions to generate WebSockify tokens.
 */
public class WebSockifyTokenBuilder extends AbstractTokenBuilder<WebSockifyTokenBuilder> {

    private final String host;

    private final int port;

    /**
     * Construct a token builder
     * @param hostIn host to connect to
     * @param portIn port to connect to
     */
    public WebSockifyTokenBuilder(String hostIn, int portIn) {
        this.host = hostIn;
        this.port = portIn;
        expiringAfterMinutes(5);
    }

    @Override
    protected JwtClaims getClaims() {
        JwtClaims claims = super.getClaims();
        claims.setClaim("host", this.host);
        claims.setClaim("port", this.port);
        return claims;
    }
}
