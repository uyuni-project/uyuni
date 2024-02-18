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

package com.suse.oval.config.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OsFamily;
import com.suse.oval.config.OVALConfig;
import com.suse.oval.config.OVALConfigLoader;
import com.suse.oval.config.OVALDistributionSourceInfo;
import com.suse.oval.config.OVALSourceInfo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import wiremock.com.google.common.io.Files;

public class OVALConfigTest {

    private static OVALConfig config;


    @BeforeAll
    public static void setUp() throws IOException, ClassNotFoundException {
        File tempDir = Files.createTempDir();
        File configFile = tempDir.toPath().resolve("oval.config.json").toFile();
        FileUtils.copyURLToFile(TestUtils.findTestData("oval.config.json"),
                configFile);

        config = new OVALConfigLoader(configFile.getAbsolutePath()).load();
    }

    @Test
    public void testAllSources() throws IOException {
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
        }).collect(Collectors.toList());

        assertFalse(allSources.isEmpty());

        for (String sourceURL : allSources) {
            HttpURLConnection huc = (HttpURLConnection) new URL(sourceURL).openConnection();

            huc.setRequestMethod("HEAD");

            int responseCode = huc.getResponseCode();

            assertEquals(HttpURLConnection.HTTP_OK, responseCode, () -> "Can't connect to URL: " + sourceURL);
        }
    }

    public void testOsFamilies() {
        // TODO: Test that all os families in the config file are in OsFamily enum
    }
}
