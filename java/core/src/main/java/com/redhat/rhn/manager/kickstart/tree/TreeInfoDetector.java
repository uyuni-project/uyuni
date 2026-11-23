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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.domain.kickstart.KickstartInstallType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Detects the distribution of an installation tree from its .treeinfo file.
 * The product map is the same as the one of the uyuni-tools mgradm distribution copy command.
 */
public final class TreeInfoDetector {

    private static final Logger LOG = LogManager.getLogger(TreeInfoDetector.class);

    private static final String TREEINFO = ".treeinfo";
    private static final String SLES = "suse linux enterprise";
    private static final String RHEL = "red hat enterprise linux";
    private static final String X86_64 = "x86_64";
    private static final String AARCH64 = "aarch64";
    private static final String SLES15 = "sles15generic";
    private static final String SLES12 = "sles12generic";

    /**
     * Distribution data needed to register a tree.
     * @param treeLabel the default tree label
     * @param installType the kickstart install type label
     * @param channelLabel the base channel label
     */
    public record DetectedDistro(String treeLabel, String installType, String channelLabel) { }

    private record ProductKey(String name, String version, String arch) { }

    private static final Map<ProductKey, DetectedDistro> PRODUCT_MAP = Map.ofEntries(
            product(SLES, "15 sp4", X86_64, "SLES15SP4", SLES15, "sle-product-sles15-sp4-pool-x86_64"),
            product(SLES, "15 sp5", X86_64, "SLES15SP5", SLES15, "sle-product-sles15-sp5-pool-x86_64"),
            product(SLES, "15 sp6", X86_64, "SLES15SP6", SLES15, "sle-product-sles15-sp6-pool-x86_64"),
            product(SLES, "15 sp6", AARCH64, "SLES15SP6", SLES15, "sle-product-sles15-sp6-pool-aarch64"),
            product(SLES, "15 sp7", X86_64, "SLES15SP7", SLES15, "sle-product-sles15-sp7-pool-x86_64"),
            product(SLES, "15 sp7", AARCH64, "SLES15SP7", SLES15, "sle-product-sles15-sp7-pool-aarch64"),
            product(SLES, "12 sp5", X86_64, "SLES12SP5", SLES12, "sles12-sp5-pool-x86_64"),
            product(RHEL, "7", X86_64, "RHEL7", KickstartInstallType.RHEL_7, "rhel7-pool-x86_64"),
            product(RHEL, "8", X86_64, "RHEL8", KickstartInstallType.RHEL_8, "rhel8-pool-x86_64"),
            product(RHEL, "9", X86_64, "RHEL9", KickstartInstallType.RHEL_9, "rhel9-pool-x86_64")
    );

    private TreeInfoDetector() {
    }

    private static Map.Entry<ProductKey, DetectedDistro> product(String name, String version, String arch,
                                                                 String treeLabel, String installType,
                                                                 String channelLabel) {
        return Map.entry(new ProductKey(name, version, arch), new DetectedDistro(treeLabel, installType, channelLabel));
    }

    /**
     * Detect the distribution of an installation tree.
     * @param tree the installation tree directory
     * @return the detected distribution, or empty if there is no readable .treeinfo or the product is unknown
     */
    public static Optional<DetectedDistro> detect(Path tree) {
        Path treeinfo = tree.resolve(TREEINFO);
        if (!Files.isRegularFile(treeinfo)) {
            LOG.info("No {} found in {}", TREEINFO, tree);
            return Optional.empty();
        }

        Map<String, String> values;
        try {
            values = parseIni(Files.readAllLines(treeinfo, StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            LOG.warn("Unable to read {}", treeinfo, e);
            return Optional.empty();
        }

        String name = values.getOrDefault("release.name", "");
        String version = values.getOrDefault("release.version", "");
        String arch = values.getOrDefault("general.arch", "");
        LOG.debug("Detected distribution {}, version {}, arch {}", name, version, arch);

        DetectedDistro distro = PRODUCT_MAP.get(new ProductKey(name.toLowerCase(Locale.ROOT),
                version.toLowerCase(Locale.ROOT), arch));
        if (distro == null) {
            LOG.info("Distribution {} {} {} from {} is not in the product map", name, version, arch, treeinfo);
        }
        return Optional.ofNullable(distro);
    }

    /**
     * Parse INI content into a map of "section.key" to value.
     */
    private static Map<String, String> parseIni(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        String section = "";
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim().toLowerCase(Locale.ROOT);
                continue;
            }
            int separator = line.indexOf('=');
            if (separator > 0) {
                String key = line.substring(0, separator).trim().toLowerCase(Locale.ROOT);
                values.put(section + "." + key, line.substring(separator + 1).trim());
            }
        }
        return values;
    }
}
