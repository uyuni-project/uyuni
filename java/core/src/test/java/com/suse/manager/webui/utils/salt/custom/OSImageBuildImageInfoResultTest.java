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
package com.suse.manager.webui.utils.salt.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.utils.Json;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OSImageBuildImageInfoResult}.
 */
public class OSImageBuildImageInfoResultTest {

    @Test
    public void testSpdxSbomDeserialization() {
        String json = """
                {
                  "sbom": {
                    "filename": "example.x86_64-1.0.spdx.json",
                    "filepath": "/var/lib/Kiwi/build/images.build/example.x86_64-1.0.spdx.json",
                    "hash": "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                    "size": 12345
                  }
                }
                """;

        OSImageBuildImageInfoResult result = Json.GSON.fromJson(json, OSImageBuildImageInfoResult.class);

        assertTrue(result.getSbom().isPresent());
        OSImageBuildImageInfoResult.Sbom sbom = result.getSbom().get();
        assertEquals("example.x86_64-1.0.spdx.json", sbom.getFilename());
        assertEquals("/var/lib/Kiwi/build/images.build/example.x86_64-1.0.spdx.json", sbom.getFilepath());
        assertEquals("sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                sbom.getChecksum().toString());
        assertEquals(12345L, sbom.getSize());
    }

    @Test
    public void testMissingSbomIsOptional() {
        OSImageBuildImageInfoResult result = Json.GSON.fromJson("{}", OSImageBuildImageInfoResult.class);

        assertFalse(result.getSbom().isPresent());
    }
}
