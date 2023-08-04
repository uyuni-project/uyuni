package com.suse.oval.ovaldownloader;

import com.suse.oval.OsFamily;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class OVALDownloader {
    private static final String DOWNLOAD_PATH = "/var/log/rhn/ovals/";

    public File download(OsFamily osFamily, String osVersion) throws IOException {
        OVALStreamInfo streamInfo;
        switch (osFamily) {
            case openSUSE_LEAP:
                streamInfo = new OpenSUSELeapOVALStreamInfo(osVersion);
                break;
            case openSUSE:
                streamInfo = new OpenSUSEOVALStreamInfo(osVersion);
                break;
            case REDHAT_ENTERPRISE_LINUX:
                streamInfo = new RedHatOVALStreamInfo(osVersion);
                break;
            case DEBIAN:
                streamInfo = new DebianOVALStreamInfo(osVersion);
                break;
            case UBUNTU:
                streamInfo = new UbuntuOVALStreamInfo(osVersion);
                break;
            default:
                throw new NotImplementedException(String.format("Cannot download '%s' OVALs", osFamily));
        }

        if (!streamInfo.isValidVersion(osVersion)) {
            throw new IllegalArgumentException(
                    String.format("Cannot download OVAL for '%s' version '%s'", osFamily, osVersion));
        }

        return doDownload(streamInfo);
    }

    public File doDownload(OVALStreamInfo streamInfo) throws IOException {
        URL remoteOVALFileURL = new URL(streamInfo.remoteFileUrl());
        File localOVALFile = new File(DOWNLOAD_PATH + streamInfo.localFileName() +
                streamInfo.getCompressionMethod().extension());

        // Start downloading
        FileUtils.copyURLToFile(remoteOVALFileURL, localOVALFile, 5000, 5000);

        OVALCompressionMethod compressionMethod = streamInfo.getCompressionMethod();
        if (compressionMethod == OVALCompressionMethod.GZIP) {
            File uncompressedOVALFile = new File(DOWNLOAD_PATH + streamInfo.localFileName() + ".xml");
            decompressGzip(localOVALFile, uncompressedOVALFile);
            localOVALFile = uncompressedOVALFile;
        } else if (compressionMethod == OVALCompressionMethod.BZIP2) {
            File uncompressedOVALFile = new File(DOWNLOAD_PATH + streamInfo.localFileName() + ".xml");
            decompressBzip2(localOVALFile, uncompressedOVALFile);
            localOVALFile = uncompressedOVALFile;
        }

        return localOVALFile;
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
