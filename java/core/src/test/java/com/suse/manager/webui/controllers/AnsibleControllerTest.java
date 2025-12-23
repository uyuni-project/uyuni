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

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.manager.system.AnsibleManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnsibleControllerTest extends BaseTestCaseWithUser {
    /**
     * Class used for test `testParseInventoryAndGetHostnames`
     */
    private class InventoryTestCase {
        private String testName;
        private Map<String, Map<String, Object>> inventory;
        private Set<String> expectedResult;

        InventoryTestCase(String testNameIn, Map<String, Map<String, Object>> inventoryIn,
                                 Set<String> expectedResultIn) {
            testName = testNameIn;
            inventory = inventoryIn;
            expectedResult = expectedResultIn;
        }

        public String getTestName() {
            return testName;
        }

        public Map<String, Map<String, Object>> getInventory() {
            return inventory;
        }

        public Set<String> getExpectedResult() {
            return expectedResult;
        }
    }

    /**
     *
     * @return some testing Ansible inventories to test `AnsibleController.parseInventoryAndGetHostnames`
     */
    private List<InventoryTestCase> getTestInventories() {
        return List.of(
            new InventoryTestCase(
                "1_empty_inventory",
                Map.of("_meta", Map.of(
                        "hostvars", List.of())
                    ),
                Set.of()
            ),
            new InventoryTestCase(
                "2_inventory_with_ungrouped_hosts",
                Map.of(
                    "_meta", Map.of(
                        "hostvars", List.of()),
                    "all", Map.of(
                        "children", List.of(
                            "ungrouped"
                        )
                    ),
                    "ungrouped", Map.of(
                        "hosts", List.of(
                                "gray-fox.shadow-moses.island",
                                "meryl.shadow-moses.island",
                                "otacon.shadow-moses.island"
                        )
                    )
                ),
                Set.of(
                    "gray-fox.shadow-moses.island",
                    "meryl.shadow-moses.island",
                    "otacon.shadow-moses.island"
                )
            ),
            new InventoryTestCase(
                "3_inventory_with_ungrouped_servers_grouped_servers_and_nested_groups",
                Map.of(
                    "_meta", Map.of("hostvars", List.of()),
                    "all", Map.of(
                        "children", List.of(
                                "ungrouped",
                                "government",
                                "fox-hound"
                        )
                    ),
                    "ungrouped", Map.of(
                        "hosts", List.of(
                            "gray-fox.shadow-moses.island",
                            "meryl.shadow-moses.island",
                            "otacon.shadow-moses.island"
                        )
                    ),
                    "government", Map.of(
                        "hosts", List.of(
                            "solid-snake.shadow-moses.island",
                            "cambell.shadow-moses.island"
                        )
                    ),
                    "fox-hound", Map.of(
                        "hosts", List.of(
                                "liquid-snake.shadow-moses.island",
                                "ocelot.shadow-moses.island"
                        )
                    ),
                    "characters", Map.of(
                        "children", List.of(
                            "government",
                            "fox-hound"
                        )
                    )
                ),
                Set.of(
                    "gray-fox.shadow-moses.island",
                    "meryl.shadow-moses.island",
                    "otacon.shadow-moses.island",
                    "solid-snake.shadow-moses.island",
                    "cambell.shadow-moses.island",
                    "liquid-snake.shadow-moses.island",
                    "ocelot.shadow-moses.island"
                )
            )
        );
    }

    /**
     * Tests `AnsibleController.parseInventoryAndGetHostnames`
     */
    @Test
    public void testParseInventoryAndGetHostnames() {
        List<InventoryTestCase> testCases = getTestInventories();
        testCases.stream().forEach((testCase) -> {
            Set<String> gotHostnames = AnsibleManager.parseInventoryAndGetHostnames(testCase.getInventory());
            assertEquals(gotHostnames, testCase.getExpectedResult());
        });
    }
}
