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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.util.Map;
import java.util.Optional;

public final class CertificateUtils {

    public static final Path CERTS_PATH = Path.of("/etc/pki/trust/anchors/");

    private static final Path LOCAL_TRUSTED_ROOT = CERTS_PATH.resolve("LOCAL-RHN-ORG-TRUSTED-SSL-CERT");

    private static final Logger LOG = LogManager.getLogger(CertificateUtils.class);

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

    /**
     * Saves multiple root ca certificates in the local filesystem
     *
     * @param filenameToRootCaCertMap maps filename to root ca certificate actual content
     */
    public static void saveCertificates(Map<String, String> filenameToRootCaCertMap) {
        if ((null == filenameToRootCaCertMap) || filenameToRootCaCertMap.isEmpty()) {
            return; // nothing to do
        }

        for (Map.Entry<String, String> pair : filenameToRootCaCertMap.entrySet()) {
            String fileName = pair.getKey();
            String rootCaCertContent = pair.getValue();

            if (fileName.isEmpty()) {
                LOG.error("Illegal empty certificate file name");
                continue;
            }

            try {
                if (rootCaCertContent.isEmpty()) {
                    CertificateUtils.safeDeleteCertificate(fileName);
                    LOG.warn("CA certificate file: {} successfully removed", fileName);
                }
                else {
                    CertificateUtils.safeSaveCertificate(fileName, rootCaCertContent);
                    LOG.warn("CA certificate file: {} successfully written", fileName);
                }
            }
            catch (IOException e) {
                LOG.error("Error when {} CA certificate file [{}] {}",
                        rootCaCertContent.isEmpty() ? "removing" : "writing", fileName, e);
            }
            catch (IllegalArgumentException e) {
                LOG.error("Illegal certificate file name [{}] {}", fileName, e);
            }
        }
    }

    /**
     * gets a safe path to a certificate, given its filename
     *
     * @param fileName the certificate filename
     * @return the path in which the certificate should reside
     * @throws IllegalArgumentException when the certificate filename is not valid or there is an attack attempt
     */
    public static Path getCertificateSafePath(String fileName) throws IllegalArgumentException {
        if (null == fileName || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        if (!fileName.matches("[a-zA-Z0-9._-]+")) {
            throw new IllegalArgumentException("File name contains invalid characters");
        }

        Path filePath = CERTS_PATH.resolve(fileName).normalize();
        if (!filePath.startsWith(CERTS_PATH)) {
            //Prevent unauthorized access through path traversal (CWE-22)
            throw new IllegalArgumentException("Attempted path traversal attack detected");
        }
        if (Files.isSymbolicLink(filePath)) {
            throw new IllegalArgumentException("Refusing to delete/create symbolic link: " + filePath);
        }

        return filePath;
    }

    /**
     * safely saves a certificate file, given its filename
     *
     * @param fileName      the certificate filename
     * @param caCertContent the certificate content
     * @throws IOException              when an error occurs while saving the certificate file
     * @throws IllegalArgumentException when the certificate filename is not valid or there is an attack attempt
     */
    public static void safeSaveCertificate(String fileName, String caCertContent)
            throws IOException, IllegalArgumentException {
        try (FileWriter fw = new FileWriter(getCertificateSafePath(fileName).toFile(), false)) {
            fw.write(caCertContent);
        }
    }

    /**
     * safely deletes a certificate file, given its filename
     *
     * @param fileName the certificate filename
     * @throws IOException              when an error occurs while deleting the certificate file
     * @throws IllegalArgumentException when the certificate filename is not valid or there is an attack attempt
     */
    public static void safeDeleteCertificate(String fileName)
            throws IOException, IllegalArgumentException {
        Files.delete(getCertificateSafePath(fileName));
    }

    /**
     * gets a system command to check if a service to update the ca certificates is present
     *
     * @return system command
     */
    public static String[] getCertificatesUpdateServiceCmd() {
        return new String[]{"systemctl", "is-active", "--quiet", "ca-certificates.path"};
    }

    /**
     * gets a system command to update the ca certificates
     * a tool to be run as a "backup" if an update service is not present
     *
     * @return system command
     */
    public static String[] getCertificatesUpdateShellCmd() {
        return new String[]{"/usr/share/rhn/certs/update-ca-cert-trust.sh"};
    }
}
