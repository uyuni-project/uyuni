/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers.download;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for extracting package information from download URL paths.
 * Handles both RPM and Debian package filename formats.
 */
public class PackagePathParser {
    private static final Logger LOG = LogManager.getLogger(PackagePathParser.class);

    // RPM pattern: name-version-release.arch.rpm
    private static final Pattern RPM_PATTERN = Pattern.compile("^(.+?)-([^-]+)-([^-]+)\\.([^.]+)\\.(rpm|drpm)$");

    // Debian pattern: name_version-release.arch.deb
    private static final Pattern DEBIAN_PATTERN = Pattern.compile("^(.+?)_([^_]+)\\.([^.]+)\\.(deb|udeb)$");

    // URL path indices
    private static final int STANDARD_PATH_PARTS = 9;
    private static final int HUBSYNC_PATH_PARTS = 10;
    private static final int STANDARD_GET_PACKAGE_IDX = 5;
    private static final int HUBSYNC_GET_PACKAGE_IDX = 6;

    /**
     * Parse a download URL path to extract package information.
     * @param urlPath the URL path
     * @return builder with parsed package info
     */
    public static PackageInfo parse(String urlPath) {
        String filename = FilenameUtils.getName(urlPath);
        String extension = FilenameUtils.getExtension(filename).toLowerCase();

        PackageInfo pkgInfo;
        if ("deb".equals(extension) || "udeb".equals(extension)) {
            pkgInfo = parseDebian(filename);
        }
        else if ("rpm".equals(extension) || "drpm".equals(extension)) {
            pkgInfo = parseRpm(filename);
        }
        else {
            throw new IllegalArgumentException("Unsupported package type: " + extension);
        }

        // Extract orgId and checksum from path if present
        String[] parts = urlPath.split("/");
        if (parts.length == STANDARD_PATH_PARTS && "getPackage".equals(parts[STANDARD_GET_PACKAGE_IDX])) {
            setPathMetadata(pkgInfo, parts[6], parts[7]);
        }
        else if (parts.length == HUBSYNC_PATH_PARTS && "getPackage".equals(parts[HUBSYNC_GET_PACKAGE_IDX])) {
            setPathMetadata(pkgInfo, parts[7], parts[8]);
        }

        return pkgInfo;
    }

    private static PackageInfo parseRpm(String filename) {
        Matcher matcher = RPM_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid RPM filename format: " + filename);
        }
        return new PackageInfo(
            matcher.group(1),   // name
            null,               // epoch
            matcher.group(2),   // version
            matcher.group(3),   // release
            matcher.group(4)    // arch
        );
    }

    private static PackageInfo parseDebian(String filename) {
        Matcher matcher = DEBIAN_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Debian filename format: " + filename);
        }

        String name = matcher.group(1);
        String versionRelease = matcher.group(2);
        String arch = matcher.group(3);

        PackageEvr evr = PackageEvr.parseDebian(versionRelease);
        return new PackageInfo(
            name,               // name
            evr.getEpoch(),     // epoch
            evr.getVersion(),   // version
            evr.getRelease(),   // release
            arch                // arch
        );
    }

    private static void setPathMetadata(PackageInfo pkgInfo, String orgIdStr, String checksum) {
        if (!"NULL".equalsIgnoreCase(orgIdStr)) {
            try {
                pkgInfo.setOrgId(Long.parseLong(orgIdStr));
            }
            catch (NumberFormatException e) {
                // Invalid orgId, leave empty
                LOG.debug("Invalid orgId in path: {}", orgIdStr, e);
            }
        }
        pkgInfo.setChecksum(checksum);
    }

    private PackagePathParser() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
