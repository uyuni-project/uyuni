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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.webui.utils.token;

import org.jose4j.jwt.JwtClaims;

public class IssTokenBuilder extends AbstractTokenBuilder<IssTokenBuilder> {

    private final String fqdn;

    /**
     * Create an instance specifying the FQDN
     *
     * @param fqdnIn the FQDN of the hub/peripheral this token belongs to
     */
    public IssTokenBuilder(String fqdnIn) {
        this.fqdn = fqdnIn;
    }

    @Override
    public JwtClaims getClaims() {
        JwtClaims claims = super.getClaims();
        claims.setClaim("fqdn", fqdn);
        return claims;
    }
}
