/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.ovaldownloader;

import com.suse.oval.OsFamily;
import com.suse.oval.config.OVALConfig;
import com.suse.oval.config.OVALSourceInfo;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
/**
 * The OVAL Downloader is responsible for finding OVAL data online, downloading them, and caching them for easy
 * access. Each supported distribution maintains an online server containing their OVAL data organized into XML files,
 * known as an OVAL repository. The links in these repositories can be adjusted by editing the {@code oval.config.json}
 * file. By default, {@code oval.config.json} directs to the official repositories supplied by the distribution
 * maintainer to ensure the integrity and consistency of the consumed OVAL data.
 * */
public class OVALDownloader {
    /**
     * The path where downloaded OVAL files will be stored.
     * */
    private static final String DOWNLOAD_PATH = "/var/log/rhn/ovals/";
    /**
     * A configuration object that corresponds to {@code oval.config.json}
     * */
    private final OVALConfig config;

    /**
     * Default constructor
     *
     * @param configIn A configuration object that corresponds to {@code oval.config.json}
     * */
    public OVALDownloader(OVALConfig configIn) {
        this.config = configIn;
    }

    /**
     * Download the OVAL data that correspond to the given {@code osFamily} and {@code osVersion} and cache
     * them for future access.
     *
     * @param osFamily the family of the Operating system (OS) to download OVAL data for.
     * @param osVersion the version of the Operating system (OS) to download OVAL data for.
     * @return {@link OVALDownloadResult}
     * */
    public OVALDownloadResult download(OsFamily osFamily, String osVersion) throws IOException {
        Optional<OVALSourceInfo> sourceInfoOpt = config.lookupSourceInfo(osFamily, osVersion);
        if (sourceInfoOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "OVAL sources for '%s' '%s' is not configured. Please ensure OVAL is configured " +
                                    "correctly oval.config.json",
                            osFamily, osVersion));
        }

        return doDownload(sourceInfoOpt.get());
    }

    /**
     * A helper method for download OVAL data.
     * */
    private OVALDownloadResult doDownload(OVALSourceInfo sourceInfo)
            throws IOException {
        OVALDownloadResult result = new OVALDownloadResult();

        String vulnerabilityInfoSource = sourceInfo.getVulnerabilitiesInfoSource();
        String patchInfoSource = sourceInfo.getPatchInfoSource();

        if (StringUtils.isNotBlank(vulnerabilityInfoSource)) {
            File vulnerabilityFile = downloadOVALFile(vulnerabilityInfoSource);
            result.setVulnerabilityFile(vulnerabilityFile);
        }

        if (StringUtils.isNotBlank(patchInfoSource)) {
            File patchFile = downloadOVALFile(patchInfoSource);
            result.setPatchFile(patchFile);
        }

        return result;
    }

    private File downloadOVALFile(String vulnerabilityInfoSource) throws IOException {
        URL vulnerabilityInfoURL = new URL(vulnerabilityInfoSource);
        String vulnerabilityInfoOVALFilename = FilenameUtils.getName(vulnerabilityInfoURL.getPath());
        File vulnerabilityFile =
                new File(DOWNLOAD_PATH + vulnerabilityInfoOVALFilename);
        // Start downloading
        FileUtils.copyURLToFile(vulnerabilityInfoURL, vulnerabilityFile, 10_000, 10_000);

        return decompressIfNeeded(vulnerabilityFile);
    }

    private File decompressIfNeeded(File file) {
        OVALCompressionMethod compressionMethod = getCompressionMethod(file.getName());
        File uncompressedOVALFile;
        if (compressionMethod == OVALCompressionMethod.BZIP2) {
            uncompressedOVALFile = new File(file.getPath().replace(compressionMethod.extension(), ""));
            decompressBzip2(file, uncompressedOVALFile);
        }
        else if (compressionMethod == OVALCompressionMethod.GZIP) {
            uncompressedOVALFile = new File(file.getPath().replace(compressionMethod.extension(), ""));
            decompressGzip(file, uncompressedOVALFile);
        }
        else if (compressionMethod == OVALCompressionMethod.NOT_COMPRESSED) {
            uncompressedOVALFile = file;
        }
        else {
            throw new IllegalStateException("Unable to decompress file: " + file.getPath());
        }

        return uncompressedOVALFile;
    }

    private OVALCompressionMethod getCompressionMethod(String filename) {
        Objects.requireNonNull(filename);

        if (filename.endsWith(".bz2")) {
            return OVALCompressionMethod.BZIP2;
        }
        else if (filename.endsWith("gz")) {
            return OVALCompressionMethod.GZIP;
        }
        else if (filename.endsWith(".xml")) {
            return OVALCompressionMethod.NOT_COMPRESSED;
        }
        else {
            throw new IllegalStateException("OVAL file compressed with an unknown compression method");
        }
    }

    /**
     * Decompress the GZIP {@code archive} into {@code target} file.
     */
    private static void decompressGzip(File archive, File target) {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(archive))) {
            FileUtils.copyToFile(gis, target);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to decompress GZIP archive", e);
        }
    }

    /**
     * Decompress the BZIP2 {@code archive} into {@code target} file.
     */
    private static void decompressBzip2(File archive, File target) {
        try (InputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(archive))) {
            FileUtils.copyToFile(inputStream, target);
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to decompress BZIP2 archive", e);
        }
    }
}
