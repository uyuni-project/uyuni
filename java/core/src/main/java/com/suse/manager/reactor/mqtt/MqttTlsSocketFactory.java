/*
 * Copyright (c) 2026 SUSE LLC
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
package com.suse.manager.reactor.mqtt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Locale;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Builds the socket factory used to reach the MQTT broker over TLS.
 *
 * <p>The broker certificate is checked against the CA certificates in a PEM file
 * when one is configured, and against the JVM's default trust store otherwise.
 * A client certificate is only presented when a PKCS#12 keystore is configured,
 * for brokers that require one.</p>
 */
final class MqttTlsSocketFactory {

    private MqttTlsSocketFactory() {
        // static helpers only
    }

    /**
     * Whether the broker URL asks for an encrypted connection.
     * @param brokerUrl the configured broker URL
     * @return true for {@code ssl://} URLs
     */
    static boolean isTlsUrl(String brokerUrl) {
        return brokerUrl != null && brokerUrl.trim().toLowerCase(Locale.ROOT).startsWith("ssl://");
    }

    /**
     * Build a socket factory for a TLS connection to the broker.
     *
     * @param caFile PEM file with the CA certificates to trust, or null for the JVM default
     * @param keystoreFile PKCS#12 keystore holding the client certificate and key, or null for none
     * @param keystorePassword password of the keystore, or null when it has none
     * @return the socket factory
     * @throws IOException if a file cannot be read, or the keystore password is wrong
     * @throws GeneralSecurityException if a file does not hold usable certificates or keys
     */
    static SSLSocketFactory build(String caFile, String keystoreFile, char[] keystorePassword)
            throws IOException, GeneralSecurityException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagers(keystoreFile, keystorePassword), trustManagers(caFile), null);
        return context.getSocketFactory();
    }

    private static TrustManager[] trustManagers(String caFile) throws IOException, GeneralSecurityException {
        if (isBlank(caFile)) {
            // null makes SSLContext fall back to the JVM default trust store
            return null;
        }

        Collection<? extends Certificate> certificates;
        try (InputStream in = Files.newInputStream(Path.of(caFile))) {
            certificates = CertificateFactory.getInstance("X.509").generateCertificates(in);
        }
        if (certificates.isEmpty()) {
            throw new CertificateException("No certificates found in " + caFile);
        }

        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        int i = 0;
        for (Certificate certificate : certificates) {
            trustStore.setCertificateEntry("mqtt-ca-" + i++, certificate);
        }

        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        return factory.getTrustManagers();
    }

    private static KeyManager[] keyManagers(String keystoreFile, char[] keystorePassword)
            throws IOException, GeneralSecurityException {
        if (isBlank(keystoreFile)) {
            return null;
        }

        char[] password = keystorePassword != null ? keystorePassword : new char[0];
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Path.of(keystoreFile))) {
            keyStore.load(in, password);
        }

        KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore, password);
        return factory.getKeyManagers();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
