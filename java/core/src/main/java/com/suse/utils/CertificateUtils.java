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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class CertificateUtils {

    public static final Path CERTS_PATH = Path.of("/etc/pki/trust/anchors/");

    private static final Path LOCAL_TRUSTED_ROOT = CERTS_PATH.resolve("LOCAL-RHN-ORG-TRUSTED-SSL-CERT");

    private static final Path GPG_PUBKEY = Path.of("/srv/susemanager/salt/gpg/mgr-gpg-pub.key");

    private static final Path CUSTOMER_GPG_DIR = Path.of("/var/spacewalk/gpg");

    private static final Path CUSTOMER_GPG_RING = CUSTOMER_GPG_DIR.resolve("customer-build-keys.gpg");

    private static final Path PUBRING_DIR = Path.of("/var/lib/spacewalk/gpgdir");

    private static final Path PUBRING = PUBRING_DIR.resolve("pubring.gpg");

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
        executeExtCmdAndGetOutput(args);
    }

    private static String executeExtCmdAndGetOutput(String[] args) {
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
            throw new RhnRuntimeException("Command '%s' exited with error code %d%s"
                    .formatted(Arrays.toString(args), exitCode,
                            msg.isBlank() ? "" : ": " + msg));
        }

        return ce.getLastCommandOutput() == null ? "" : ce.getLastCommandOutput();
    }

    /**
     * Import the GPG Key in the keyring
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
            String[] cmdAdd = {"gpg", "--homedir", CUSTOMER_GPG_DIR.toString(), "--no-default-keyring",
                    "--import", "--import-options", "import-minimal",
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
            if (!Files.isDirectory(CUSTOMER_GPG_DIR)) {
                FileAttribute<Set<PosixFilePermission>> dirAttrs =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                Files.createDirectories(CUSTOMER_GPG_DIR, dirAttrs);
            }
            executeExtCmd(new String[]{"gpg", "--homedir", CUSTOMER_GPG_DIR.toString(), "--no-default-keyring",
                    "--keyring", CUSTOMER_GPG_RING.toString(), "--fingerprint"});
        }
        catch (IOException e) {
            LOG.error("Failed to create the customer gpg directory {}: {}", CUSTOMER_GPG_DIR, e.getMessage());
            throw new RhnRuntimeException("Failed to create customer gpg directory " + CUSTOMER_GPG_DIR, e);
        }
        catch (RhnRuntimeException e) {
            LOG.error("Failed to initialize the customer gpg keyring: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Remove a GPG key identified by its fingerprint
     * @param fingerprint the hex representation of the fingerprint
     * @return true on success
     */
    public static boolean removeGpgKey(String fingerprint) {
        if (StringUtils.isBlank(fingerprint)) {
            LOG.info("No GPG fingerprint provided");
            return false;
        }

        if (!Files.exists(CUSTOMER_GPG_RING)) {
            LOG.info("No customer GPG keyring present at {}", CUSTOMER_GPG_RING);
            return false;
        }

        try {
            String[] cmdAdd = {"gpg", "--homedir", CUSTOMER_GPG_DIR.toString(), "--batch", "--yes",
                    "--no-default-keyring", "--keyring", CUSTOMER_GPG_RING.toString(),
                    "--delete-keys", fingerprint};
            executeExtCmd(cmdAdd);
        }
        catch (RhnRuntimeException e) {
            LOG.warn("Failed to remove GPG key {} from keyring: {}", fingerprint, e.getMessage());
            return false;
        }

        if (Files.exists(PUBRING)) {
            try {
                executeExtCmd(new String[]{"gpg", "--homedir", PUBRING_DIR.toString(), "--batch", "--yes",
                        "--no-default-keyring", "--keyring", PUBRING.toString(),
                        "--delete-keys", fingerprint});
            }
            catch (RhnRuntimeException e) {
                LOG.warn("Failed to remove GPG key {} from pubring: {}", fingerprint, e.getMessage());
            }
        }

        return true;
    }

    public static class GpgKeyListing {

        /**
         * Creates a new empty listing
         */
        public GpgKeyListing() {
            keyType = 0;
            keySize = 0;
            fingerprint = "<unknown>";
            names = new ArrayList<>();
        }

        /**
         * Creates a new listing with starting values
         * @param keyTypeIn GPG type number
         * @param keySizeIn Size of the key in bits
         * @param fingerprintIn Fingerprint of the key
         * @param namesIn Names associated with the key
         */
        public GpgKeyListing(int keyTypeIn, int keySizeIn, String fingerprintIn,
                List<String> namesIn) {
            keyType = keyTypeIn;
            keySize = keySizeIn;
            fingerprint = fingerprintIn;
            names = namesIn;
        }

        private int keyType;

        /**
         * @return GPG type number
         */
        public int getKeyType() {
            return keyType;
        }

        private int keySize;

        /**
         * @return Size of the key in bits
         */
        public int getKeySize() {
            return keySize;
        }

        private String fingerprint;

        /**
         * @return Fingerprint of the key
         */
        public String getFingerprint() {
            return fingerprint;
        }

        private List<String> names;

        /**
         * @return Names associated with the key
         */
        public List<String> getNames() {
            return names;
        }

    }

    /**
     * Get all keys stored in the keyring
     * @return decoded list of all found keys
     */
    public static List<GpgKeyListing> getGpgKeys() {
        if (!Files.exists(CUSTOMER_GPG_RING)) {
            return new ArrayList<>();
        }

        String output = executeExtCmdAndGetOutput(
                new String[] {"gpg", "--homedir", CUSTOMER_GPG_DIR.toString(), "--batch", "--with-colons",
                        "--no-default-keyring", "--keyring", CUSTOMER_GPG_RING.toString(), "--list-keys"});
        return parseGpgColonListing(output);
    }

    /**
     * Parse all keys from a keyring dump
     * @param output output generated by gpg --with-colons
     * @return decoded list of all found keys
     */
    public static List<GpgKeyListing> parseGpgColonListing(String output) {
        List<GpgKeyListing> keys = new ArrayList<>();
        if (StringUtils.isBlank(output)) {
            return keys;
        }

        GpgKeyListing currentKey = null;
        boolean pendingFingerprint = false;

        for (String line : output.split("\\R")) {
            String[] fields = line.split(":", -1);
            String recordType = fields[0];

            if ("pub".equals(recordType)) {
                currentKey = new GpgKeyListing();
                currentKey.keySize = fields.length > 2 ? Integer.parseInt(fields[2]) : 0;
                currentKey.keyType = fields.length > 3 ? Integer.parseInt(fields[3]) : 0;
                keys.add(currentKey);
                pendingFingerprint = true;
            }
            else if ("fpr".equals(recordType) && pendingFingerprint) {
                currentKey.fingerprint = fields.length > 9 ? fields[9] : "";
                pendingFingerprint = false;
            }
            else if ("uid".equals(recordType) && currentKey != null) {
                currentKey.names.add(fields.length > 9 ? fields[9] : "");
            }
        }

        return keys;
    }
}
