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
package com.redhat.rhn.manager.content.ubuntu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Test class for the Ubuntu USN database url configuration.
 */
public class UbuntuErrataManagerTest {

    private static final String CONFIG_KEY = "java.ubuntu_errata_db_download_url";
    private static final String DEFAULT_URL = "https://usn.ubuntu.com/usn-db/database.json";

    @AfterEach
    public void tearDown() {
        Config.clear();
    }

    @Test
    public void testDefaultErrataDbUrl() throws IOException {
        Config.get().remove(CONFIG_KEY);
        assertEquals(DEFAULT_URL, ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());
    }

    @Test
    public void testConfiguredErrataDbUrl() throws IOException {
        String mirrorUrl = "http://mirror.example.com/usn-db/database.json";
        Config.get().setString(CONFIG_KEY, mirrorUrl);
        assertEquals(mirrorUrl, ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());
    }

    @Test
    public void testEmptyErrataDbUrlFallsBackToDefault() throws IOException {
        Config.get().setString(CONFIG_KEY, "");
        assertEquals(DEFAULT_URL, ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());
    }

    @Test
    public void testNonHttpErrataDbUrlIsRejected() {
        Config.get().setString(CONFIG_KEY, "file:///srv/mirror/database.json");
        assertThrows(IOException.class, () -> ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());

        Config.get().setString(CONFIG_KEY, "/srv/mirror/database.json");
        assertThrows(IOException.class, () -> ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());
    }

    @Test
    public void testMalformedErrataDbUrlIsRejected() {
        Config.get().setString(CONFIG_KEY, "ht tp://mirror.example.com");
        assertThrows(IOException.class, () -> ConfigDefaults.get().getUbuntuErrataDbDownloadUrl());
    }
}
