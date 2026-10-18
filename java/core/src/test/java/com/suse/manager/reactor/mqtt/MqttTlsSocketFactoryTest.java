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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateException;

/**
 * Unit test for {@link MqttTlsSocketFactory}.
 */
public class MqttTlsSocketFactoryTest {

    private static final char[] KEYSTORE_PASSWORD = MqttTlsTestFiles.KEYSTORE_PASSWORD.toCharArray();

    private static MqttTlsTestFiles files;

    /**
     * Generate the certificate and keystore used by the tests.
     * @param tempDir where the files are generated
     * @throws Exception if keytool fails
     */
    @BeforeAll
    public static void createFiles(@TempDir Path tempDir) throws Exception {
        files = MqttTlsTestFiles.create(tempDir);
    }

    /**
     * Only ssl:// URLs ask for TLS.
     */
    @Test
    public void testIsTlsUrl() {
        assertTrue(MqttTlsSocketFactory.isTlsUrl("ssl://broker:8883"));
        assertTrue(MqttTlsSocketFactory.isTlsUrl(" SSL://broker:8883"));
        assertFalse(MqttTlsSocketFactory.isTlsUrl("tcp://broker:1883"));
        assertFalse(MqttTlsSocketFactory.isTlsUrl(""));
        assertFalse(MqttTlsSocketFactory.isTlsUrl(null));
    }

    /**
     * Without any files, the JVM default trust store is used.
     * @throws Exception if the factory cannot be built
     */
    @Test
    public void testDefaultTrustStore() throws Exception {
        assertNotNull(MqttTlsSocketFactory.build(null, null, null));
        assertNotNull(MqttTlsSocketFactory.build(" ", "", null));
    }

    /**
     * A PEM file with a CA certificate is accepted as the trust anchor.
     * @throws Exception if the factory cannot be built
     */
    @Test
    public void testCustomCa() throws Exception {
        assertNotNull(MqttTlsSocketFactory.build(files.getCaFile(), null, null));
    }

    /**
     * A PKCS#12 keystore supplies the client certificate for brokers that require one.
     * @throws Exception if the factory cannot be built
     */
    @Test
    public void testClientCertificate() throws Exception {
        assertNotNull(MqttTlsSocketFactory.build(files.getCaFile(), files.getKeystoreFile(),
                KEYSTORE_PASSWORD));
    }

    /**
     * A missing CA file is an error, not a silent fallback to the default trust store.
     */
    @Test
    public void testMissingCaFile() {
        assertThrows(IOException.class,
                () -> MqttTlsSocketFactory.build("/nonexistent/mqtt-ca.pem", null, null));
    }

    /**
     * A file without any certificate in it is rejected.
     */
    @Test
    public void testFileWithoutCertificates() {
        assertThrows(CertificateException.class,
                () -> MqttTlsSocketFactory.build(files.getNotACertificate(), null, null));
    }

    /**
     * A wrong keystore password is rejected.
     */
    @Test
    public void testWrongKeystorePassword() {
        assertThrows(IOException.class,
                () -> MqttTlsSocketFactory.build(null, files.getKeystoreFile(), "wrong".toCharArray()));
    }
}
