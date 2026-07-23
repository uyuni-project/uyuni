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
 * Default {@link LdapServiceFactory} producing UnboundID-backed services.
 */
public class DefaultLdapServiceFactory implements LdapServiceFactory {

    private final LdapConnectionFactory connectionFactory;

    /**
     * Creates a factory using the default connection factory.
     */
    public DefaultLdapServiceFactory() {
        this(new LdapConnectionFactory());
    }

    /**
     * Creates a factory using a custom connection factory. Mainly useful for tests that point the
     * service at an in-memory directory server.
     *
     * @param connectionFactoryIn the connection factory to use
     */
    public DefaultLdapServiceFactory(LdapConnectionFactory connectionFactoryIn) {
        this.connectionFactory = connectionFactoryIn;
    }

    @Override
    public LdapAuthenticationService getInstance(LdapServerConfig configIn) {
        return new UnboundIdLdapAuthenticationService(configIn, connectionFactory);
    }
}
