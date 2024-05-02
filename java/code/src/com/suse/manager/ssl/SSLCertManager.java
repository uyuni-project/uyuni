/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.ssl;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.util.FileUtils;

import com.suse.manager.utils.ExecHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class helping to generate and check SSL certificates
 */
public class SSLCertManager {
    private static final Logger LOG = LogManager.getLogger(SSLCertManager.class);

    private final ExecHelper execHelper;
    private File sslBuildDir = null;

    /**
     * Default constructor
     */
    public SSLCertManager() {
        execHelper = new ExecHelper();
    }

    /**
     * Constructor to use for testing
     *
     * @param helper the exec helper
     * @param tempDirectory temporary files folder
     */
    public SSLCertManager(ExecHelper helper, File tempDirectory) {
        execHelper = helper;
        sslBuildDir = tempDirectory;
    }

    /**
     * Generate an SSL certificate using a provided CA
     *
     * @param caPair the CA certificate and it key used to sign the certificate to generate
     * @param password the CA private key password
     * @param data the data to use to create the certificate
     *
     * @return the generated certicate and its key
     * @throws SSLCertGenerationException if anything wrong happens when generating the certificate
     */
    public SSLCertPair generateCertificate(SSLCertPair caPair, String password, SSLCertData data)
            throws SSLCertGenerationException {
        // Run the ssh-ssl-tool in a temporary folder
        try {
            if (sslBuildDir == null) {
                sslBuildDir = Files.createTempDirectory("ssltool").toFile();
            }

            // Write the CA data to the temp folder
            File tempCaCertFile = new File(sslBuildDir, "ca.crt");
            FileUtils.writeStringToFile(caPair.getCertificate(), tempCaCertFile.getAbsolutePath());

            File tempCaKeyFile = new File(sslBuildDir, "ca.key");
            FileUtils.writeStringToFile(caPair.getKey(), tempCaKeyFile.getAbsolutePath());

            List<String> command = new ArrayList<>();
            command.addAll(List.of("rhn-ssl-tool", "--gen-server", "-q", "--no-rpm"));
            command.addAll(List.of("-d", sslBuildDir.getAbsolutePath()));
            command.addAll(List.of("--ca-cert", tempCaCertFile.getName()));
            command.addAll(List.of("--ca-key", tempCaKeyFile.getName()));
            command.addAll(data.getRhnSslToolParams());

            execHelper.exec(command, password);

            // Read the cert and key files
            File serverFolder = new File(sslBuildDir, data.getMachineName());
            File serverCertFile = new File(serverFolder, "server.crt");
            File serverKeyFile = new File(serverFolder, "server.key");
            return new SSLCertPair(FileUtils.readStringFromFile(serverCertFile.getAbsolutePath(), true),
                    FileUtils.readStringFromFile(serverKeyFile.getAbsolutePath(), true));
        }
        catch (RhnRuntimeException | IOException err) {
            String msg = err.getMessage();
            if (msg.startsWith(ExecHelper.TOOL_FAILED_MSG)) {
                msg = Pattern.compile("\\A.*ERROR: ", Pattern.DOTALL).matcher(
                        msg.substring(ExecHelper.TOOL_FAILED_MSG.length()).replaceAll("(?m)^\n", "")
                ).replaceAll("").strip();
            }
            throw new SSLCertGenerationException(msg);
        }
        finally {
            if (sslBuildDir != null) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(sslBuildDir);
                }
                catch (IOException err) {
                    LOG.error("Failed to remove temporary folder", err);
                }
            }
        }
    }

    /**
     * Extract the CN and alternate DNS names from an SSL certificate.
     *
     * @param certificate the certificate to read
     * @return the set of subject names
     */
    public Set<String> getNamesFromSslCert(String certificate) {
        String output = execHelper.exec(List.of("openssl", "x509", "-noout", "-subject", "-ext", "subjectAltName"),
                certificate);
        return Arrays.stream(output.split("\n"))
                .map(line -> {
                    if (line.startsWith("subject=")) {
                        for (String part : line.split(",")) {
                            String trimmed = part.trim();
                            if (trimmed.startsWith("CN = ")) {
                                return Set.of(trimmed.substring("CN =".length()).trim());
                            }
                        }
                    }
                    if (line.contains("DNS:")) {
                        return Arrays.stream(line.split(","))
                                .filter(dns -> dns.contains("DNS:"))
                                .map(dns -> dns.split(":")[1].trim())
                                .collect(Collectors.toSet());
                    }
                    return null;
                })
                .filter(name -> name != null)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
