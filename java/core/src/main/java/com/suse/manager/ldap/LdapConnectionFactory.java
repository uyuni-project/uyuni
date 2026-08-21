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
import com.unboundid.ldap.sdk.PostConnectProcessor;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.StartTLSPostConnectProcessor;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.AggregateTrustManager;
import com.unboundid.util.ssl.JVMDefaultTrustManager;
import com.unboundid.util.ssl.SSLUtil;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Creates the LDAP connections used by {@link UnboundIdLdapAuthenticationService}.
 *
 * <p>This class is the seam that lets the service be exercised against an in-memory directory
 * server in tests: tests subclass it and override {@link #socketFactory(LdapServerConfig)} (or the
 * connection factory wiring) so no real TLS material or network access is required.</p>
 *
 * <p>Supports {@link LdapTransport#PLAIN}, {@link LdapTransport#LDAPS} and
 * {@link LdapTransport#STARTTLS}. When a custom root CA PEM is present on the configuration it is
 * trusted in addition to the JVM default trust store; otherwise secure transports rely on the JVM
 * defaults alone.</p>
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
     * Returns the socket factory used to reach the directory for {@link LdapTransport#LDAPS}, or
     * {@code null} for a plain (cleartext) connection / StartTLS handshake base connection.
     *
     * @param config the server configuration
     * @return a socket factory for LDAPS, or {@code null} for PLAIN and STARTTLS
     * @throws LdapServiceException if a TLS socket factory cannot be created
     */
    protected SocketFactory socketFactory(LdapServerConfig config) throws LdapServiceException {
        if (config.getTransport() != LdapTransport.LDAPS) {
            return null;
        }
        return createSslSocketFactory(config);
    }

    /**
     * Builds an SSL socket factory that trusts the optional custom CA plus the JVM defaults.
     *
     * @param config the server configuration
     * @return an SSL socket factory
     * @throws LdapServiceException if TLS material cannot be initialized
     */
    protected SSLSocketFactory createSslSocketFactory(LdapServerConfig config) throws LdapServiceException {
        try {
            TrustManager trustManager = createTrustManager(config);
            if (trustManager == null) {
                return new SSLUtil().createSSLSocketFactory();
            }
            return new SSLUtil(trustManager).createSSLSocketFactory();
        }
        catch (GeneralSecurityException e) {
            throw new LdapServiceException("Unable to initialize TLS for LDAP connection", e);
        }
    }

    /**
     * Opens a single, unauthenticated connection. Used for the credential bind so that the
     * pooled service connections keep their service-account identity. For StartTLS the connection
     * is upgraded before being returned.
     *
     * @param config the server configuration
     * @return a new open connection
     * @throws LdapServiceException if the connection cannot be established
     */
    public LDAPConnection openConnection(LdapServerConfig config) throws LdapServiceException {
        try {
            LDAPConnectionOptions options = connectionOptions(config);
            if (config.getTransport() == LdapTransport.STARTTLS) {
                LDAPConnection connection = new LDAPConnection(options, config.getHost(), config.getPort());
                try {
                    SSLSocketFactory sslFactory = createSslSocketFactory(config);
                    connection.processExtendedOperation(new StartTLSExtendedRequest(sslFactory));
                    return connection;
                }
                catch (LDAPException | LdapServiceException e) {
                    connection.close();
                    throw e;
                }
            }
            SocketFactory factory = socketFactory(config);
            if (factory == null) {
                return new LDAPConnection(options, config.getHost(), config.getPort());
            }
            return new LDAPConnection(factory, options, config.getHost(), config.getPort());
        }
        catch (LDAPException e) {
            throw new LdapServiceException("Unable to connect to LDAP server " + config.getHost(), e);
        }
    }

    /**
     * Creates a connection pool bound as the configured service account (or anonymous when
     * explicitly allowed). The pool is used for user and group searches.
     *
     * @param config the server configuration
     * @return a ready connection pool the caller must close
     * @throws LdapServiceException if the connection or service bind fails
     */
    public LDAPConnectionPool createServicePool(LdapServerConfig config) throws LdapServiceException {
        LDAPConnection connection = openConnection(config);
        try {
            var bindDn = config.getBindDn();
            if (bindDn.isPresent()) {
                connection.bind(new SimpleBindRequest(bindDn.get(),
                        config.getBindPassword().orElse("")));
            }
            if (config.getTransport() == LdapTransport.STARTTLS) {
                PostConnectProcessor postConnect =
                        new StartTLSPostConnectProcessor(createSslSocketFactory(config));
                return new LDAPConnectionPool(connection, INITIAL_POOL_CONNECTIONS, MAX_POOL_CONNECTIONS, postConnect);
            }
            return new LDAPConnectionPool(connection, INITIAL_POOL_CONNECTIONS, MAX_POOL_CONNECTIONS);
        }
        catch (LDAPException e) {
            connection.close();
            throw new LdapServiceException("Service-account bind failed for LDAP server " + config.getHost(), e);
        }
    }

    private static TrustManager createTrustManager(LdapServerConfig config) throws GeneralSecurityException {
        String rootCa = config.getRootCa().orElse(null);
        if (StringUtils.isBlank(rootCa)) {
            return null;
        }
        X509TrustManager customTrustManager = trustManagerForPem(rootCa);
        return new AggregateTrustManager(false, JVMDefaultTrustManager.getInstance(), customTrustManager);
    }

    private static X509TrustManager trustManagerForPem(String pemCertificate) throws GeneralSecurityException {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate = certificateFactory.generateCertificate(
                    new ByteArrayInputStream(pemCertificate.getBytes(StandardCharsets.UTF_8)));
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ldap-root-ca", certificate);

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                if (trustManager instanceof X509TrustManager x509TrustManager) {
                    return x509TrustManager;
                }
            }
            throw new GeneralSecurityException("No X509TrustManager available for custom LDAP CA");
        }
        catch (java.io.IOException | java.security.cert.CertificateException e) {
            throw new GeneralSecurityException("Unable to load LDAP root CA certificate", e);
        }
    }
}
