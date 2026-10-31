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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TLS material for the MQTT tests, generated with the JDK's keytool so that no
 * private key has to be kept in the repository.
 *
 * <p>The client certificate is self-signed, so its certificate also serves as the
 * CA file. The tests only check that the files are loaded, not a handshake.</p>
 */
final class MqttTlsTestFiles {

    static final String KEYSTORE_PASSWORD = "test-password";

    private final Path caFile;
    private final Path keystoreFile;
    private final Path notACertificate;

    private MqttTlsTestFiles(Path dir) {
        caFile = dir.resolve("ca.pem");
        keystoreFile = dir.resolve("client.p12");
        notACertificate = dir.resolve("not-a-certificate.pem");
    }

    /**
     * @return PEM file holding the certificate, usable as the CA file
     */
    String getCaFile() {
        return caFile.toString();
    }

    /**
     * @return PKCS#12 keystore holding the certificate and its key
     */
    String getKeystoreFile() {
        return keystoreFile.toString();
    }

    /**
     * @return a file that contains no certificate
     */
    String getNotACertificate() {
        return notACertificate.toString();
    }

    /**
     * Generate the files in the given directory.
     * @param dir an empty directory, usually a JUnit temporary directory
     * @return the generated files
     * @throws IOException if keytool fails or the files cannot be written
     * @throws InterruptedException if waiting for keytool is interrupted
     */
    static MqttTlsTestFiles create(Path dir) throws IOException, InterruptedException {
        MqttTlsTestFiles files = new MqttTlsTestFiles(dir);
        keytool("-genkeypair", "-alias", "uyuni-publisher", "-keyalg", "RSA", "-keysize", "2048",
                "-dname", "CN=uyuni-publisher", "-validity", "2", "-storetype", "PKCS12",
                "-keystore", files.keystoreFile.toString(),
                "-storepass", KEYSTORE_PASSWORD, "-keypass", KEYSTORE_PASSWORD);
        keytool("-exportcert", "-rfc", "-alias", "uyuni-publisher",
                "-keystore", files.keystoreFile.toString(), "-storepass", KEYSTORE_PASSWORD,
                "-file", files.caFile.toString());
        Files.writeString(files.notACertificate, "This file intentionally holds no certificates.\n");
        return files;
    }

    private static void keytool(String... args) throws IOException, InterruptedException {
        String keytool = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        List<String> command = new ArrayList<>(List.of(keytool));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        if (process.waitFor() != 0) {
            throw new IOException("keytool failed: " + output);
        }
    }
}
