/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.ldap;

/**
 * Transport security mode used to reach a directory server.
 */
public enum LdapTransport {

    /** Plain, unencrypted LDAP (typically port 389). For test and trusted-network use only. */
    PLAIN(389, false),

    /** LDAP over TLS, negotiated at connection time (LDAPS, typically port 636). */
    LDAPS(636, true),

    /** Plain LDAP upgraded to TLS via the StartTLS extended operation (typically port 389). */
    STARTTLS(389, true);

    private final int defaultPort;
    private final boolean secure;

    LdapTransport(int defaultPortIn, boolean secureIn) {
        this.defaultPort = defaultPortIn;
        this.secure = secureIn;
    }

    /**
     * @return the default TCP port commonly associated with this transport
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * @return {@code true} if this transport encrypts traffic to the directory
     */
    public boolean isSecure() {
        return secure;
    }
}
