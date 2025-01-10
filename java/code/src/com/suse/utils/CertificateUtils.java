/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.util.Optional;

public final class CertificateUtils {

    public static final Path CERTS_PATH = Path.of("/etc/pki/trust/anchors/");

    private static final Path LOCAL_TRUSTED_ROOT = CERTS_PATH.resolve("LOCAL-RHN-ORG-TRUSTED-SSL-CERT");

    private CertificateUtils() {
        // Prevent instantiation
    }

    /**
     * Loads the local trusted root certificate and returns it as PEM data
     * @return a string representation of the root certificate, in PEM format
     */
    public static String loadLocalTrustedRoot() throws IOException {
        return loadPEMCertificate(LOCAL_TRUSTED_ROOT);
    }

    /**
     * Load the specified certificate file and return it PEM data in a string
     * @param path the path of the certificate file to load
     * @return a string representation of the certificate, in PEM format
     * @throws IOException when reading the data from file fails
     */
    public static String loadPEMCertificate(Path path) throws IOException {
        if (!Files.isReadable(path)) {
            return null;
        }

        return Files.readString(path);
    }

    /**
     * Parse the given PEM certificate.
     * @param pemCertificate a string representing the PEM certificate. Might be empty or null
     * @return the certificate
     * @throws CertificateException when an error occurs while parsing the data
     */
    public static Optional<Certificate> parse(String pemCertificate) throws CertificateException {
        if (StringUtils.isEmpty(pemCertificate)) {
            return Optional.empty();
        }

        try (InputStream inputStream = new ByteArrayInputStream(pemCertificate.getBytes(StandardCharsets.UTF_8))) {
            return parse(inputStream);
        }
        catch (IOException ex) {
            throw new CertificateParsingException("Unable to load certificate from byte array", ex);
        }
    }

    /**
     * Parse a given PEM certificate
     * @param inputStream the input stream containing the PEM certificate
     * @return the certificate
     * @throws CertificateException when an error occurs while parsing the data
     */
    public static Optional<Certificate> parse(InputStream inputStream) throws CertificateException {
        if (inputStream == null) {
            return Optional.empty();
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return Optional.of(certificateFactory.generateCertificate(inputStream));
    }
}
