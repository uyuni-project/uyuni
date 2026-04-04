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

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CertificateUtils {

    public static final Path CERTS_PATH = Path.of("/etc/pki/trust/anchors/");

    private static final Path LOCAL_TRUSTED_ROOT = CERTS_PATH.resolve("LOCAL-RHN-ORG-TRUSTED-SSL-CERT");

    private static final Path GPG_PUBKEY = Path.of("/srv/susemanager/salt/gpg/mgr-gpg-pub.key");

    private static final Path CUSTOMER_GPG_DIR = Path.of("/var/spacewalk/gpg");

    private static final Path CUSTOMER_GPG_RING = CUSTOMER_GPG_DIR.resolve("customer-build-keys.gpg");

    private static final String ROOT_CA_FILENAME_TEMPLATE = "%s_%s_root_ca.pem";

    private static final Logger LOG = LogManager.getLogger(CertificateUtils.class);

    private CertificateUtils() {
        // Prevent instantiation
    }

    /**
     * Compute a file name for a root certificate authority of the specified server
     * @param prefix a prefix, defining the role of the server
     * @param serverFqdn the server owning the root ca
     * @return the full file path where the root ca can be store and retrieved
     */
    public static String computeRootCaFileName(String prefix, String serverFqdn) {
        return ROOT_CA_FILENAME_TEMPLATE.formatted(prefix, serverFqdn);
    }

    /**
     * Loads the local trusted root certificate and returns it as PEM data
     * @return a string representation of the root certificate, in PEM format
     */
    public static String loadLocalTrustedRoot() throws IOException {
        return loadTextFile(LOCAL_TRUSTED_ROOT);
    }

    /**
     * Loads the local GPG Key used for signing the metadata as ARMORED data.
     * @return a string representation of the GPG key
     * @throws IOException when reading the data from file fails
     */
    public static String loadGpgKey() throws IOException {
        return loadTextFile(GPG_PUBKEY);
    }

    /**
     * Load the specified file and return it Text data in a string
     * @param path the path of the file to load
     * @return a string representation of the file
     * @throws IOException when reading the data from file fails
     */
    public static String loadTextFile(Path path) throws IOException {
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
     * Saves multiple root ca certificates, then updates trusted directory
     *
     * @param filenameToRootCaCertMap maps filename to root ca certificate actual content
     * @throws JobExecutionException if there was an error
     */
    public static void saveAndUpdateCertificates(Map<String, String> filenameToRootCaCertMap) {
        if ((null == filenameToRootCaCertMap) || filenameToRootCaCertMap.isEmpty()) {
            return; // nothing to do
        }

        saveCertificates(filenameToRootCaCertMap);
        updateCertificates();
    }

    private static void saveCertificates(Map<String, String> filenameToRootCaCertMap) {
        if ((null == filenameToRootCaCertMap) || filenameToRootCaCertMap.isEmpty()) {
            return; // nothing to do
        }

        for (Map.Entry<String, String> pair : filenameToRootCaCertMap.entrySet()) {
            String fileName = pair.getKey();
            String rootCaCertContent = pair.getValue();

            if (fileName.isEmpty()) {
                LOG.warn("Skipping illegal empty certificate file name");
                continue;
            }

            try {
                if (rootCaCertContent.isEmpty()) {
                    Files.delete(getCertificateSafePath(fileName));
                    LOG.info("CA certificate file: {} successfully removed", fileName);
                }
                else {
                    Files.writeString(getCertificateSafePath(fileName), rootCaCertContent, StandardCharsets.UTF_8);
                    LOG.info("CA certificate file: {} successfully written", fileName);
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

    private static void updateCertificates() {
        try {
            //system command to check if a service to update the ca certificates is present
            executeExtCmd(new String[]{"systemctl", "is-active", "--quiet", "ca-certificates.path"});
        }
        catch (Exception e) {
            LOG.debug("ca-certificates.path service is not active, we will call 'update-ca-certificates' tool");
            executeExtCmd(new String[]{"/usr/share/rhn/certs/update-ca-cert-trust.sh"});
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

        if (!fileName.matches("[a-zA-Z0-9:._-]+")) {
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

    private static void executeExtCmd(String[] args) {
        SystemCommandThreadedExecutor ce = new SystemCommandThreadedExecutor(LOG, true);
        int exitCode = ce.execute(args);

        if (exitCode != 0) {
            String msg = ce.getLastCommandErrorMessage();
            if (msg.isBlank()) {
                msg = ce.getLastCommandOutput();
            }
            if (msg.length() > 2300) {
                msg = "... " + msg.substring(msg.length() - 2300);
            }
            throw new RhnRuntimeException("Command '%s' exited with error code %d%s".formatted(
                    Arrays.toString(args),
                    exitCode,
                    msg.isBlank() ? "" : ": " + msg)
            );
        }
    }

    /**
     * Import the GPG Key in the customer keyring
     * @param gpgKey the gpg key (armored text)
     * @throws IOException if something goes wrong
     */
    public static void importGpgKey(String gpgKey) throws IOException {
        if (StringUtils.isBlank(gpgKey)) {
            LOG.info("No GPG Key provided");
            return;
        }
        FileAttribute<Set<PosixFilePermission>> fileAttributes =
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r-----"));
        Path gpgTempFile = Files.createTempFile("susemanager-gpg-", ".tmp", fileAttributes);
        try {

            Files.writeString(gpgTempFile, gpgKey, StandardCharsets.UTF_8);
            if (!Files.exists(CUSTOMER_GPG_RING)) {
                initializeGpgKeyring();
            }
            String[] cmdAdd = {"gpg", "--no-default-keyring", "--import", "--import-options", "import-minimal",
                    "--keyring", CUSTOMER_GPG_RING.toString(), gpgTempFile.toString()};
            executeExtCmd(cmdAdd);
            executeExtCmd(new String[]{"/usr/sbin/import-suma-build-keys"});
        }
        finally {
            Files.deleteIfExists(gpgTempFile);
        }
    }

    private static void initializeGpgKeyring() {
        try {
            executeExtCmd(new String[]{"mkdir", "-m", "700", "-p", CUSTOMER_GPG_DIR.toString()});
            executeExtCmd(new String[]{"gpg", "--no-default-keyring", "--keyring", CUSTOMER_GPG_RING.toString(),
                    "--fingerprint"});
        }
        catch (Exception e) {
            LOG.error("Failed to initialize the customer gpg keyring: {}", e.getMessage());
            throw e;
        }
    }
}
