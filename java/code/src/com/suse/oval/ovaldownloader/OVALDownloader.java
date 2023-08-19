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

public class OVALDownloader {
    private static final String DOWNLOAD_PATH = "/var/log/rhn/ovals/";
    private final OVALConfig config;

    public OVALDownloader(OVALConfig config) {
        this.config = config;
    }

    public OVALDownloadResult download(OsFamily osFamily, String osVersion) throws IOException {
        Optional<OVALSourceInfo> sourceInfoOpt = config.lookupSourceInfo(osFamily, osVersion);
        if (sourceInfoOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "OVAL sources for '%s' '%s' is not configured. Please ensure OVAL is configured correctly oval.config.json",
                            osFamily, osVersion));
        }

        return doDownload(sourceInfoOpt.get());
    }

    public OVALDownloadResult doDownload(OVALSourceInfo sourceInfo)
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
        } else if (compressionMethod == OVALCompressionMethod.GZIP) {
            uncompressedOVALFile = new File(file.getPath().replace(compressionMethod.extension(), ""));
            decompressGzip(file, uncompressedOVALFile);
        } else if (compressionMethod == OVALCompressionMethod.NOT_COMPRESSED) {
            uncompressedOVALFile = file;
        } else {
            throw new IllegalStateException("Unable to decompress file: " + file.getPath());
        }

        return uncompressedOVALFile;
    }

    public OVALCompressionMethod getCompressionMethod(String filename) {
        Objects.requireNonNull(filename);

        if (filename.endsWith(".bz2")) {
            return OVALCompressionMethod.BZIP2;
        } else if (filename.endsWith("gz")) {
            return OVALCompressionMethod.GZIP;
        } else if (filename.endsWith(".xml")) {
            return OVALCompressionMethod.NOT_COMPRESSED;
        } else {
            throw new IllegalStateException("OVAL file compressed with an unknown compression method");
        }
    }

    /**
     * Decompress the GZIP {@code archive} into {@code target} file.
     */
    private static void decompressGzip(File archive, File target) {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(archive))) {
            FileUtils.copyToFile(gis, target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress GZIP archive", e);
        }
    }

    /**
     * Decompress the BZIP2 {@code archive} into {@code target} file.
     */
    private static void decompressBzip2(File archive, File target) {
        try (InputStream inputStream = new BZip2CompressorInputStream(new FileInputStream(archive))) {
            FileUtils.copyToFile(inputStream, target);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress BZIP2 archive", e);
        }
    }
}
