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
package com.suse.manager.webui.utils;

import org.jose4j.jwt.JwtClaims;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility functions to generate download access tokens.
 */
public class DownloadTokenBuilder extends TokenBuilder {

    /**
     * The organization the token will give access to
     */
    private final long orgId;

    /**
     * By default, a token gives access to all channels in the organization.
     * If this is set, only the specified channels will be allowed
     * (whitelist of channel label list)
     */
    private Optional<Set<String>> onlyChannels = Optional.empty();

    /**
     * Constructs a token builder.
     * @param orgIdIn Organization id the generated tokens will give access to
     */
    public DownloadTokenBuilder(long orgIdIn) {
        this.orgId = orgIdIn;
    }

    /**
     * The token would only allow access to the given list of channels
     * in the organization.
     * @param channels list of channels to allow access to
     */
    public void onlyChannels(Set<String> channels) {
        this.onlyChannels = Optional.of(channels);
    }

    /**
     * @return the current token JWT claims
     */
    @Override
    public JwtClaims getClaims() {
        JwtClaims claims = super.getClaims();
        claims.setClaim("org", this.orgId);
        onlyChannels.ifPresent(channels ->
                claims.setStringListClaim("onlyChannels",
                        channels.stream().collect(Collectors.toList())));
        return claims;
    }
}
