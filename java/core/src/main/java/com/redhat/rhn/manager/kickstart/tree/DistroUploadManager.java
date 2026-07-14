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

package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.manager.satellite.SystemCommandThreadedExecutor;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Stores uploaded autoinstallation distribution media.
 */
public class DistroUploadManager {

    private static final Logger LOG = LogManager.getLogger(DistroUploadManager.class);

    private static final int MAX_COMMAND_OUTPUT_LENGTH = 2300;

    /** Web root used by the existing mgradm distro copy workflow. */
    public static final Path DEFAULT_DISTRIBUTIONS_PATH = Paths.get("/srv/www/distributions");

    private final Path distributionsPath;
    private final IsoExtractor isoExtractor;

    /**
     * Extracts uploaded ISO media into an installation tree.
     */
    @FunctionalInterface
    public interface IsoExtractor {
        /**
         * Extract ISO media into a directory.
         * @param isoPath the uploaded ISO path
         * @param destinationPath the destination directory
         * @throws IOException if the ISO cannot be extracted
         */
        void extract(Path isoPath, Path destinationPath) throws IOException;
    }

    /**
     * Create a manager using the default distribution web root.
     */
    public DistroUploadManager() {
        this(DEFAULT_DISTRIBUTIONS_PATH);
    }

    /**
     * Create a manager using a custom distribution web root.
     * @param distributionsPathIn the directory where distribution media is stored
     */
    public DistroUploadManager(Path distributionsPathIn) {
        this(distributionsPathIn, new XorrisoIsoExtractor());
    }

    /**
     * Create a manager using a custom distribution web root and ISO extractor.
     * @param distributionsPathIn the directory where distribution trees are stored
     * @param isoExtractorIn the extractor used to unpack ISO media
     */
    public DistroUploadManager(Path distributionsPathIn, IsoExtractor isoExtractorIn) {
        this.distributionsPath = Objects.requireNonNull(distributionsPathIn).toAbsolutePath().normalize();
        this.isoExtractor = Objects.requireNonNull(isoExtractorIn);
    }

    /**
     * Store a distribution upload as an extracted installation tree.
     * @param filename the distribution name or original ISO file name
     * @param distroStream the uploaded content
     * @return the destination path
     * @throws IOException if the upload cannot be stored
     */
    public Path uploadDistro(String filename, InputStream distroStream) throws IOException {
        String safeDistroName = getSafeDistroName(filename);

        Files.createDirectories(distributionsPath);
        if (!Files.isDirectory(distributionsPath)) {
            throw new IOException("Distribution upload path is not a directory: " + distributionsPath);
        }

        Path destination = distributionsPath.resolve(safeDistroName).normalize();
        if (!destination.startsWith(distributionsPath)) {
            throw new IOException("Invalid distribution upload filename: " + filename);
        }
        if (Files.exists(destination)) {
            throw new FileAlreadyExistsException(destination.toString());
        }

        Path tempIso = Files.createTempFile(distributionsPath, getTempPrefix(safeDistroName), ".iso");
        Path tempDirectory = Files.createTempDirectory(distributionsPath, getTempPrefix(safeDistroName));
        try {
            try (InputStream stream = distroStream) {
                Files.copy(stream, tempIso, StandardCopyOption.REPLACE_EXISTING);
            }
            isoExtractor.extract(tempIso, tempDirectory);
            moveIntoPlace(tempDirectory, destination);
            return destination;
        }
        catch (IOException | RuntimeException e) {
            deleteDirectoryIfExists(tempDirectory);
            throw e;
        }
        finally {
            deleteFileIfExists(tempIso);
        }
    }

    private static void moveIntoPlace(Path tempDirectory, Path destination) throws IOException {
        try {
            Files.move(tempDirectory, destination, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            Files.move(tempDirectory, destination);
        }
    }

    private static String getSafeDistroName(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("Distribution upload filename is required");
        }

        String trimmedFilename = filename.trim();
        if (trimmedFilename.contains("/") || trimmedFilename.contains("\\")) {
            throw new IOException("Distribution upload filename must not contain path segments: " + filename);
        }

        String safeFilename = Paths.get(trimmedFilename).getFileName().toString().trim();
        if (safeFilename.toLowerCase(Locale.ROOT).endsWith(".iso")) {
            safeFilename = safeFilename.substring(0, safeFilename.length() - ".iso".length()).trim();
        }
        if (safeFilename.isEmpty() || ".".equals(safeFilename) || "..".equals(safeFilename)) {
            throw new IOException("Invalid distribution upload filename: " + filename);
        }
        if (!safeFilename.equals(trimmedFilename) && !(safeFilename + ".iso").equalsIgnoreCase(trimmedFilename)) {
            throw new IOException("Distribution upload filename must not contain path segments: " + filename);
        }
        return safeFilename;
    }

    private static String getTempPrefix(String distroName) {
        String prefix = distroName + ".";
        return prefix.length() >= 3 ? prefix : "distro." + prefix;
    }

    private static void deleteDirectoryIfExists(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try {
            FileUtils.deleteDirectory(directory.toFile());
        }
        catch (IOException e) {
            LOG.warn("Unable to delete temporary distribution extraction directory {}", directory, e);
        }
    }

    private static void deleteFileIfExists(Path file) {
        try {
            Files.deleteIfExists(file);
        }
        catch (IOException e) {
            LOG.warn("Unable to delete temporary distribution upload file {}", file, e);
        }
    }

    private static class XorrisoIsoExtractor implements IsoExtractor {
        @Override
        public void extract(Path isoPath, Path destinationPath) throws IOException {
            executeExtCmd(new String[] {
                    "xorriso", "-osirrox", "on", "-indev", isoPath.toString(), "-extract", "/",
                    destinationPath.toString()
            });
        }
    }

    private static void executeExtCmd(String[] args) throws IOException {
        SystemCommandThreadedExecutor commandExecutor = new SystemCommandThreadedExecutor(LOG, true);
        int exitCode;
        try {
            exitCode = commandExecutor.execute(args);
        }
        catch (RhnRuntimeException e) {
            throw new IOException("Unable to execute command " + Arrays.asList(args), e);
        }

        if (exitCode != 0) {
            String message = commandExecutor.getLastCommandErrorMessage();
            if (message == null || message.isBlank()) {
                message = commandExecutor.getLastCommandOutput();
            }
            message = truncateCommandOutput(message);
            throw new IOException("Command '" + Arrays.asList(args) + "' exited with error code " + exitCode +
                    (message.isBlank() ? "" : ": " + message));
        }
    }

    private static String truncateCommandOutput(String message) {
        if (message == null) {
            return "";
        }
        if (message.length() > MAX_COMMAND_OUTPUT_LENGTH) {
            return "... " + message.substring(message.length() - MAX_COMMAND_OUTPUT_LENGTH);
        }
        return message;
    }
}
