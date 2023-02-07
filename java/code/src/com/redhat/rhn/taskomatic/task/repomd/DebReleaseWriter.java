/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.repomd;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates the Release file for Debian repos.
 */
public class DebReleaseWriter {

    private static Logger log = LogManager.getLogger(DebReleaseWriter.class);
    private String filenameRelease = "";
    private String pathPrefix;
    private Channel channel;

    public static final DateTimeFormatter RFC822_DATE_FORMAT;
    static {
        RFC822_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'z", Locale.US)
                .withZone(ZoneId.of("UTC"));
    }

    /**
     * @param channelIn the channel
     * @param pathPrefixIn the path prefix
     */
    public DebReleaseWriter(Channel channelIn, String pathPrefixIn) {
        log.debug("DebReleaseWriter created");
        this.channel = channelIn;
        this.pathPrefix = pathPrefixIn;
        this.filenameRelease = pathPrefix + "Release";
    }

    /**
     * Generates the Release file.
     */
    public void generateRelease() {
        File f = new File(filenameRelease);
        if (f.exists()) {
            f.delete();
        }
        try (PrintWriter writer = new PrintWriter(filenameRelease, StandardCharsets.UTF_8)) {
            writer.println("Archive: " + channel.getLabel());
            writer.println("Label: " + channel.getLabel());
            writer.println("Suite: " + channel.getLabel());
            writer.println("Architectures: " + toArchString(channel.getChannelArch()));
            writer.println("Date: " + RFC822_DATE_FORMAT.format(ZonedDateTime.now()));
            writer.println("Description: " + Optional.ofNullable(channel.getDescription()).orElse(""));

            List<File> metadataFiles = Arrays.asList("Packages", "Packages.gz")
                    .stream().map(name -> new File(pathPrefix + name))
                    .collect(Collectors.toList());

            writer.println("MD5Sum:");
            metadataFiles.forEach(file -> appendSum(writer, DigestUtils::md5Hex, file));

            writer.println("SHA1:");
            metadataFiles.forEach(file -> appendSum(writer, DigestUtils::sha1Hex, file));

            writer.println("SHA256:");
            metadataFiles.forEach(file -> appendSum(writer, DigestUtils::sha256Hex, file));
        }
        catch (IOException e) {
            log.error("Could not generate Release file for channel {}", channel.getLabel(), e);
        }
    }

    private String toArchString(ChannelArch channelArch) {
        return (String)channelArch.getCompatiblePackageArches().stream().
               map(a -> ((PackageArch)a).getLabel().replaceAll("-deb", "")).
               filter(a -> !("all".equals(a) || "src".equals(a))).
               sorted().collect(Collectors.joining(" "));
    }

    @FunctionalInterface
    private interface ChecksumFunction {
        String apply(InputStream input) throws IOException;
    }

    private void appendSum(PrintWriter writer, ChecksumFunction checksum, File file) {
        try (FileInputStream pkgIn = new FileInputStream(file)) {
            writer.println(" " + checksum.apply(pkgIn) + " " + file.length() + " " + file.getName());
        }
        catch (IOException e) {
            log.error("Could not compute checksum for {}", file.getName());
        }
    }
}
