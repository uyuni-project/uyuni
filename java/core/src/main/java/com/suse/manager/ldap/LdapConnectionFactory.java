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

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.util.ssl.SSLUtil;

import java.security.GeneralSecurityException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

/**
 * Creates the LDAP connections used by {@link UnboundIdLdapAuthenticationService}.
 *
 * <p>This class is the seam that lets the service be exercised against an in-memory directory
 * server in tests: tests subclass it and override {@link #socketFactory(LdapServerConfig)} (or the
 * connection factory wiring) so no real TLS material or network access is required.</p>
 *
 * <p>Supports {@link LdapTransport#PLAIN} and {@link LdapTransport#LDAPS}. Custom trust material
 * (administrator-uploaded directory CA certificates) and StartTLS are not supported yet; until
 * then a secure transport relies on the JVM default trust store.</p>
 */
public class LdapConnectionFactory {

    private static final int INITIAL_POOL_CONNECTIONS = 1;
    private static final int MAX_POOL_CONNECTIONS = 4;

    /**
     * Builds the {@link LDAPConnectionOptions} (timeouts, follow-referrals policy) for a server.
     *
     * @param config the server configuration
     * @return connection options derived from the configuration
     */
    protected LDAPConnectionOptions connectionOptions(LdapServerConfig config) {
        LDAPConnectionOptions options = new LDAPConnectionOptions();
        options.setConnectTimeoutMillis(config.getConnectTimeoutMillis());
        options.setResponseTimeoutMillis(config.getResponseTimeoutMillis());
        options.setFollowReferrals(false);
        return options;
    }

    /**
     * Returns the socket factory used to reach the directory, or {@code null} for a plain
     * (cleartext) connection.
     *
     * @param config the server configuration
     * @return a socket factory for secure transports, or {@code null} for {@link LdapTransport#PLAIN}
     * @throws LdapException if a TLS socket factory cannot be created
     */
    protected SocketFactory socketFactory(LdapServerConfig config) throws LdapException {
        if (config.getTransport() == LdapTransport.PLAIN) {
            return null;
        }
        if (config.getTransport() == LdapTransport.STARTTLS) {
            throw new LdapException("StartTLS is not supported yet; use LDAPS or PLAIN");
        }
        try {
            // Until custom CA trust is wired in, rely on the JVM default trust store.
            SSLUtil sslUtil = new SSLUtil();
            SSLSocketFactory factory = sslUtil.createSSLSocketFactory();
            return factory;
        }
        catch (GeneralSecurityException e) {
            throw new LdapException("Unable to initialize TLS for LDAPS connection", e);
        }
    }

    /**
     * Opens a single, unauthenticated connection. Used for the credential bind so that the
     * pooled service connections keep their service-account identity.
     *
     * @param config the server configuration
     * @return a new open connection
     * @throws LdapException if the connection cannot be established
     */
    public LDAPConnection openConnection(LdapServerConfig config) throws LdapException {
        try {
            SocketFactory factory = socketFactory(config);
            LDAPConnectionOptions options = connectionOptions(config);
            if (factory == null) {
                return new LDAPConnection(options, config.getHost(), config.getPort());
            }
            return new LDAPConnection(factory, options, config.getHost(), config.getPort());
        }
        catch (LDAPException e) {
            throw new LdapException("Unable to connect to LDAP server " + config.getHost(), e);
        }
    }

    /**
     * Creates a connection pool bound as the configured service account (or anonymous when
     * explicitly allowed). The pool is used for user and group searches.
     *
     * @param config the server configuration
     * @return a ready connection pool the caller must close
     * @throws LdapException if the connection or service bind fails
     */
    public LDAPConnectionPool createServicePool(LdapServerConfig config) throws LdapException {
        LDAPConnection connection = openConnection(config);
        try {
            if (config.getBindDn().isPresent()) {
                connection.bind(new SimpleBindRequest(config.getBindDn().get(),
                        config.getBindPassword().orElse("")));
            }
            return new LDAPConnectionPool(connection, INITIAL_POOL_CONNECTIONS, MAX_POOL_CONNECTIONS);
        }
        catch (LDAPException e) {
            connection.close();
            throw new LdapException("Service-account bind failed for LDAP server " + config.getHost(), e);
        }
    }
}
