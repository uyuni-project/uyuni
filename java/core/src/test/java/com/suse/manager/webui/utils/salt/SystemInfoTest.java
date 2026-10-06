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
package com.suse.manager.webui.utils.salt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.utils.Json;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SystemInfoTest {

    @Test
    public void testTransactionalGrainBooleanValues() {
        assertEquals(Boolean.TRUE, systemInfoWithTransactional(true).getTransactional().orElseThrow());
        assertEquals(Boolean.FALSE, systemInfoWithTransactional(false).getTransactional().orElseThrow());
        assertEquals(Boolean.TRUE, systemInfoWithTransactional(true, "grains.item")
                .getTransactional().orElseThrow());
        assertEquals(Boolean.FALSE, systemInfoWithTransactional(false, "grains.item")
                .getTransactional().orElseThrow());
    }

    @Test
    public void testTransactionalGrainInvalidOrAbsentValuesAreIgnored() {
        assertTrue(systemInfoWithTransactional("true").getTransactional().isEmpty());
        assertTrue(systemInfoWithTransactional(1).getTransactional().isEmpty());
        assertTrue(systemInfoWithTransactional(null).getTransactional().isEmpty());
        // grains.item answers with an empty string when the grain is not defined on the minion
        assertTrue(systemInfoWithTransactional("", "grains.item").getTransactional().isEmpty());
    }

    @Test
    public void testTransactionalGrainAbsentStateIsIgnored() {
        SystemInfo systemInfo = Json.GSON.fromJson("{}", SystemInfo.class);

        assertTrue(systemInfo.getTransactional().isEmpty());
    }

    private SystemInfo systemInfoWithTransactional(Object value) {
        return systemInfoWithTransactional(value, "grains.items");
    }

    private SystemInfo systemInfoWithTransactional(Object value, String function) {
        Map<String, Object> grains = new HashMap<>();
        if (value != null) {
            grains.put("transactional", value);
        }
        Map<String, Object> stateResult = Map.of(
                "changes", Map.of("ret", grains),
                "result", true);
        Map<String, Object> response = Map.of(
                "module_|-grains_update_|-" + function + "_|-run", stateResult);
        return Json.GSON.fromJson(Json.GSON.toJson(response), SystemInfo.class);
    }
}
