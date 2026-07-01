/*
 * Copyright (c) 2024--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.oval.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OsFamily;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class OVALConfigTest {

    private static OVALConfig config;

    private static Path tempDir;

    @BeforeAll
    public static void setUp() throws IOException, ClassNotFoundException {
        tempDir = Files.createTempDirectory("oval-config-test");

        File configFile = tempDir.resolve("oval.config.json").toFile();
        FileUtils.copyURLToFile(TestUtils.findTestData("oval.config.json"),
                configFile);

        config = new OVALConfigLoader(configFile.getAbsolutePath()).load();
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Delete test files
        try (Stream<Path> fileStream = Files.walk(tempDir)) {
            fileStream
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    public void testAllSources() {
        Map<OsFamily, OVALDistributionSourceInfo> sources = config.getSources();

        List<String> allSources = sources.keySet().stream().flatMap(osFamily -> {
            OVALDistributionSourceInfo ovalDistributionSourceInfo = sources.get(osFamily);
            return ovalDistributionSourceInfo.getContent().keySet().stream()
                    .flatMap(version -> {
                        Optional<OVALSourceInfo> ovalSourceInfoOpt =
                                ovalDistributionSourceInfo.getVersionSourceInfo(version);

                        OVALSourceInfo ovalSourceInfo = ovalSourceInfoOpt.get();

                        String ovalVulnerabilitiesUrl = ovalSourceInfo.getVulnerabilitiesInfoSource();
                        String ovalPatchesUrl = ovalSourceInfo.getPatchInfoSource();

                        List<String> urls = new ArrayList<>();
                        if (StringUtils.isNotBlank(ovalVulnerabilitiesUrl)) {
                            urls.add(ovalVulnerabilitiesUrl);
                        }

                        if (StringUtils.isNotBlank(ovalPatchesUrl)) {
                            urls.add(ovalPatchesUrl);
                        }

                        return urls.stream();
                    });
        }).toList();

        assertFalse(allSources.isEmpty());
    }

    public void testOsFamilies() {
        // TODO: Test that all os families in the config file are in OsFamily enum
    }
}
