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

package com.suse.manager.webui.controllers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.webui.controllers.AnsibleController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnsibleControllerTest extends BaseTestCaseWithUser {
    @BeforeAll
    public static void beforeAll() {}

    @Override
    @BeforeEach
    public void setUp() throws Exception {}

    @Override
    @AfterEach
    public void tearDown() throws Exception {}

    private class InventoryTestCase {
        private String TestName;
        private Map<String, Map<String, Object>> Inventory;
        private Set<String> ExpectedResult;

        public InventoryTestCase(String TestName, Map<String, Map<String, Object>> Inventory, Set<String> ExpectedResult) {
            this.TestName = TestName;
            this.Inventory = Inventory;
            this.ExpectedResult = ExpectedResult;
        }


        public String getTestName() {
            return TestName;
        }

        public Map<String, Map<String, Object>> getInventory() {
            return Inventory;
        }

        public Set<String> getExpectedResult() {
            return ExpectedResult;
        }
    }
    public List<InventoryTestCase> getTestInventories() {
        return new LinkedList<>() {{
            add(new InventoryTestCase(
                    "1_empty_inventory",
                    new LinkedHashMap<>() {{
                        put("_meta", new LinkedHashMap<>() {{
                            put("hostvars", new LinkedList<String>());
                        }});
                    }},
                    new HashSet<>()
            ));

            add(new InventoryTestCase(
                    "2_inventory_with_ungrouped_hosts",
                    new LinkedHashMap<>() {{
                        put("_meta", new LinkedHashMap<>() {{
                            put("hostvars", new LinkedList<String>());
                        }});
                        put("all", new LinkedHashMap<>() {{
                            put("children", new LinkedList<String>() {{
                                add("ungrouped");
                            }});
                        }});
                        put("ungrouped", new LinkedHashMap<>() {{
                            put("hosts", new LinkedList<String>() {{
                                add("gray-fox.shadow-moses.island");
                                add("meryl.shadow-moses.island");
                                add("otacon.shadow-moses.island");
                            }});
                        }});
                    }},
                    new HashSet<>() {{
                        add("gray-fox.shadow-moses.island");
                        add("meryl.shadow-moses.island");
                        add("otacon.shadow-moses.island");
                    }}
            ));

            add(new InventoryTestCase(
                            "3_inventory_with_ungrouped_servers_grouped_servers_and_nested_groups",
                            new LinkedHashMap<>() {
                                {
                                    put("_meta", new LinkedHashMap<>() {{
                                        put("hostvars", new LinkedList<String>());
                                    }});
                                    put("all", new LinkedHashMap<>() {{
                                        put("children", new LinkedList<String>() {{
                                            add("ungrouped");
                                            add("government");
                                            add("fox-hound");
                                        }});
                                    }});
                                    put("ungrouped", new LinkedHashMap<>() {{
                                        put("hosts", new LinkedList<String>() {{
                                            add("gray-fox.shadow-moses.island");
                                            add("meryl.shadow-moses.island");
                                            add("otacon.shadow-moses.island");
                                        }});
                                    }});
                                    put("government", new LinkedHashMap<>() {{
                                        put("hosts", new LinkedList<String>() {{
                                            add("solid-snake.shadow-moses.island");
                                            add("cambell.shadow-moses.island");
                                        }});
                                    }});
                                    put("fox-hound", new LinkedHashMap<>() {{
                                        put("hosts", new LinkedList<String>() {{
                                            add("liquid-snake.shadow-moses.island");
                                            add("ocelot.shadow-moses.island");
                                        }});
                                    }});
                                    put("characters", new LinkedHashMap<>() {{
                                        put("children", new LinkedList<String>() {{
                                            add("government");
                                            add("fox-hound");
                                        }});
                                    }});
                                }
                            },
                            new HashSet<>() {{
                                add("gray-fox.shadow-moses.island");
                                add("meryl.shadow-moses.island");
                                add("otacon.shadow-moses.island");
                                add("solid-snake.shadow-moses.island");
                                add("cambell.shadow-moses.island");
                                add("liquid-snake.shadow-moses.island");
                                add("ocelot.shadow-moses.island");
                            }}
                    )
            );
        }};
    }

    @Test
    public void testParseInventoryAndGetHostnames() {
        List<InventoryTestCase> testCases = getTestInventories();
        testCases.stream().forEach((testCase) -> {
            Set<String> gotHostnames = AnsibleController.parseInventoryAndGetHostnames(testCase.getInventory());
            assertEquals(gotHostnames, testCase.getExpectedResult());
        });
    }
}
