/*
 * Copyright (c) 2025 SUSE LLC
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
package com.suse.manager.webui.utils.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.redhat.rhn.domain.access.Namespace;

import com.suse.manager.webui.utils.AccessControlNamespaceTreeHelper;
import com.suse.manager.webui.utils.gson.NamespaceNodeJson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.List;

/**
 * Tests for {@link AccessControlNamespaceTreeHelper}.
 */
public class AccessControlNamespaceTreeHelperTest {

    private AccessControlNamespaceTreeHelper treeHelper;
    private long idCounter;

    @BeforeEach
    public void setUp() {
        treeHelper = new AccessControlNamespaceTreeHelper();
        idCounter = 1L;
    }

    @TestFactory
    @DisplayName("Namespace Tree Hierarchy Verification for Web Nodes")
    Collection<DynamicTest> testWebHierarchy() {
        // The tree structure being tested:
        // admin
        //   └─ config (RW)
        // home
        //   ├─ account (branch)
        //   │  ├─ address (RW)
        //   │  ├─ email (W)
        //   │  ├─ myorg.config_channels (RW)
        //   │  └─ myorg.trust (R)
        //   ├─ notifications (RW, leaf)
        //   └─ notifications (branch)
        //      └─ retry (W)
        // systems
        //   ├─ ssm (R)
        //   └─ groups (branch)
        //      ├─ details (R)
        //      └─ details.delete (W)
        List<Namespace> namespaces = List.of(
            // admin
            createNamespace("admin.config", Namespace.AccessMode.R),
            createNamespace("admin.config", Namespace.AccessMode.W),

            // home
            createNamespace("home.account.address", Namespace.AccessMode.R),
            createNamespace("home.account.address", Namespace.AccessMode.W),
            createNamespace("home.account.email", Namespace.AccessMode.W),
            createNamespace("home.account.myorg.config_channels", Namespace.AccessMode.W),
            createNamespace("home.account.myorg.config_channels", Namespace.AccessMode.R),
            createNamespace("home.account.myorg.trust", Namespace.AccessMode.R),
            createNamespace("home.notifications", Namespace.AccessMode.W),
            createNamespace("home.notifications", Namespace.AccessMode.R),
            createNamespace("home.notifications.retry", Namespace.AccessMode.W),

            // systems
            createNamespace("systems.groups.details.delete", Namespace.AccessMode.W),
            createNamespace("systems.groups.details", Namespace.AccessMode.R),
            createNamespace("systems.ssm", Namespace.AccessMode.R)
        );

        Collection<NamespaceNodeJson> tree = treeHelper.buildTree(namespaces);
        return List.of(
            dynamicTest("should build tree with correct number of root nodes", () -> {
                assertEquals(3, tree.size(), "Expected three root nodes: admin, home, systems");
            }),

            dynamicTest("should validate the 'admin' hierarchy", () -> {
                NamespaceNodeJson admin = findNode(tree, "admin");
                assertNotNull(admin);
                testAdminWebNode(admin);
            }),

            dynamicTest("should validate the 'home' hierarchy", () -> {
                NamespaceNodeJson home = findNode(tree, "home");
                assertNotNull(home);
                testHomeWebNode(home);
            }),

            dynamicTest("should validate the 'systems' hierarchy", () -> {
                NamespaceNodeJson systems = findNode(tree, "systems");
                assertNotNull(systems);
                testSystemsWebNode(systems);
            })
        );
    }

    private void testAdminWebNode(NamespaceNodeJson adminNode) {
        assertAll("Verify 'admin' node structure",
            () -> assertEquals(1, adminNode.getChildren().size()),
            () -> {
                var configNode = findNode(adminNode.getChildren(), "admin.config");
                assertNotNull(configNode);
                assertEquals("RW", configNode.getAccessMode());
            }
        );
    }

    private void testHomeWebNode(NamespaceNodeJson homeNode) {
        var children = homeNode.getChildren();
        var accountNode = findNode(children, "account");
        var notificationsGroupNode = findNode(children, "notifications");
        var notificationsLeafNode = findNode(children, "home.notifications");

        assertAll("Verify 'home' node and its direct children",
            () -> assertEquals(3, children.size()),
            () -> {
                assertNotNull(accountNode);
                testHomeAccountWebNode(accountNode);
            },
            () -> {
                assertNotNull(notificationsLeafNode);
                assertEquals("RW", notificationsLeafNode.getAccessMode());
            },
            () -> {
                assertNotNull(notificationsGroupNode);
                assertEquals(1, notificationsGroupNode.getChildren().size());
                var retryNode = findNode(notificationsGroupNode.getChildren(), "home.notifications.retry");
                assertNotNull(retryNode);
                assertEquals("W", retryNode.getAccessMode());
            }
        );
    }

    private void testHomeAccountWebNode(NamespaceNodeJson accountNode) {
        var children = accountNode.getChildren();
        assertAll("Verify 'account' node children",
            () -> assertEquals(4, children.size()),
            () -> {
                var node = findNode(children, "home.account.address");
                assertNotNull(node);
                assertEquals("RW", node.getAccessMode());
            },
            () -> {
                var node = findNode(children, "home.account.email");
                assertNotNull(node);
                assertEquals("W", node.getAccessMode());
            },
            () -> {
                var node = findNode(children, "home.account.myorg.config_channels");
                assertNotNull(node);
                assertEquals("RW", node.getAccessMode());
            },
            () -> {
                var node = findNode(children, "home.account.myorg.trust");
                assertNotNull(node);
                assertEquals("R", node.getAccessMode());
            }
        );
    }

    private void testSystemsWebNode(NamespaceNodeJson systemsNode) {
        var children = systemsNode.getChildren();
        var groupsNode = findNode(children, "groups");
        var ssmNode = findNode(children, "systems.ssm");

        assertAll("Verify 'systems' node and its direct children",
            () -> assertEquals(2, children.size()),
            () -> {
                assertNotNull(groupsNode);
                testSystemsGroupsWebNode(groupsNode);
            },
            () -> {
                assertNotNull(ssmNode);
                assertEquals("R", ssmNode.getAccessMode());
            }
        );
    }

    private void testSystemsGroupsWebNode(NamespaceNodeJson groupsNode) {
        var children = groupsNode.getChildren();
        assertAll("Verify 'groups' node children",
            () -> assertEquals(2, children.size()),
            () -> {
                var detailsNode = findNode(children, "systems.groups.details");
                assertNotNull(detailsNode);
                assertEquals("R", detailsNode.getAccessMode());
            },
            () -> {
                var deleteNode = findNode(children, "systems.groups.details.delete");
                assertNotNull(deleteNode);
                assertEquals("W", deleteNode.getAccessMode());
            }
        );
    }

    @TestFactory
    @DisplayName("Namespace Tree Hierarchy Verification for API Nodes")
    Collection<DynamicTest> testApiHierarchy() {
        // The tree structure being tested (API prefix is stripped from root):
        // channel
        //   └─ list (R)
        // channel.org
        //   └─ list (R)
        // kickstart.profile.system
        //   └─ list_keys (R)
        // system
        //   ├─ list (R)
        //   └─ list_module_streams (R)
        // user.external
        //   ├─ get_use_org_unit (R)
        //   └─ set_external_group_system_groups (W)
        List<Namespace> namespaces = List.of(
            createNamespace("api.system.list", Namespace.AccessMode.R),
            createNamespace("api.system.list_module_streams", Namespace.AccessMode.R),
            createNamespace("api.channel.list", Namespace.AccessMode.R),
            createNamespace("api.channel.org.list", Namespace.AccessMode.R),
            createNamespace("api.user.external.set_external_group_system_groups", Namespace.AccessMode.W),
            createNamespace("api.user.external.get_use_org_unit", Namespace.AccessMode.R),
            createNamespace("api.kickstart.profile.system.list_keys", Namespace.AccessMode.R)
        );

        Collection<NamespaceNodeJson> tree = treeHelper.buildTree(namespaces);

        return List.of(
            dynamicTest("should build tree with correct number of root nodes", () -> assertEquals(5,
                tree.size(),
                "Expected five root nodes: channel, channel.org, kickstart.profile.system, system, user.external")),

            dynamicTest("should validate the 'channel' hierarchy", () -> {
                NamespaceNodeJson channel = findNode(tree, "channel");
                assertNotNull(channel);
                testApiChannelNode(channel);
            }),

            dynamicTest("should validate the 'channel.org' hierarchy", () -> {
                NamespaceNodeJson channelOrg = findNode(tree, "channel.org");
                assertNotNull(channelOrg);
                testApiChannelOrgNode(channelOrg);
            }),

            dynamicTest("should validate the 'kickstart.profile.system' hierarchy", () -> {
                NamespaceNodeJson kickstart = findNode(tree, "kickstart.profile.system");
                assertNotNull(kickstart);
                testApiKickstartNode(kickstart);
            }),

            dynamicTest("should validate the 'system' hierarchy", () -> {
                NamespaceNodeJson system = findNode(tree, "system");
                assertNotNull(system);
                testApiSystemNode(system);
            }),

            dynamicTest("should validate the 'user.external' hierarchy", () -> {
                NamespaceNodeJson userExternal = findNode(tree, "user.external");
                assertNotNull(userExternal);
                testApiUserExternalNode(userExternal);
            })
        );
    }

    private void testApiChannelNode(NamespaceNodeJson channelNode) {
        assertAll("Verify 'channel' node structure",
            () -> assertEquals(1, channelNode.getChildren().size()),
            () -> {
                var listNode = findNode(channelNode.getChildren(), "api.channel.list");
                assertNotNull(listNode);
                assertEquals("R", listNode.getAccessMode());
            }
        );
    }

    private void testApiChannelOrgNode(NamespaceNodeJson channelOrgNode) {
        assertAll("Verify 'channel.org' node structure",
            () -> assertEquals(1, channelOrgNode.getChildren().size()),
            () -> {
                var listNode = findNode(channelOrgNode.getChildren(), "api.channel.org.list");
                assertNotNull(listNode);
                assertEquals("R", listNode.getAccessMode());
            }
        );
    }

    private void testApiKickstartNode(NamespaceNodeJson kickstartNode) {
        assertAll("Verify 'kickstart.profile.system' node structure",
            () -> assertEquals(1, kickstartNode.getChildren().size()),
            () -> {
                var listKeysNode = findNode(kickstartNode.getChildren(), "api.kickstart.profile.system.list_keys");
                assertNotNull(listKeysNode);
                assertEquals("R", listKeysNode.getAccessMode());
            }
        );
    }

    private void testApiSystemNode(NamespaceNodeJson systemNode) {
        var children = systemNode.getChildren();
        assertAll("Verify 'system' node children",
            () -> assertEquals(2, children.size()),
            () -> {
                var node = findNode(children, "api.system.list");
                assertNotNull(node);
                assertEquals("R", node.getAccessMode());
            },
            () -> {
                var node = findNode(children, "api.system.list_module_streams");
                assertNotNull(node);
                assertEquals("R", node.getAccessMode());
            }
        );
    }

    private void testApiUserExternalNode(NamespaceNodeJson userExternalNode) {
        var children = userExternalNode.getChildren();
        assertAll("Verify 'user.external' node children",
            () -> assertEquals(2, children.size()),
            () -> {
                var node = findNode(children, "api.user.external.set_external_group_system_groups");
                assertNotNull(node);
                assertEquals("W", node.getAccessMode());
            },
            () -> {
                var node = findNode(children, "api.user.external.get_use_org_unit");
                assertNotNull(node);
                assertEquals("R", node.getAccessMode());
            }
        );
    }

    @TestFactory
    @DisplayName("Namespace Tree Hierarchy Verification for Mixed Nodes")
    Collection<DynamicTest> testMixedScenario() {
        // The tree structure being tested:
        // home
        //   ├─ overview (R, leaf)
        //   ├─ notifications (R, leaf)
        //   └─ notifications (branch)
        //      └─ retry (W)
        // system (from api.system.*)
        //   ├─ test (RW)
        //   └─ details (R)
        List<Namespace> namespaces = List.of(
            createNamespace("home.overview", Namespace.AccessMode.R),
            createNamespace("home.notifications", Namespace.AccessMode.R),
            createNamespace("home.notifications.retry", Namespace.AccessMode.W),
            createNamespace("api.system.test", Namespace.AccessMode.R),
            createNamespace("api.system.test", Namespace.AccessMode.W),
            createNamespace("api.system.details", Namespace.AccessMode.R)
        );

        Collection<NamespaceNodeJson> tree = treeHelper.buildTree(namespaces);

        return List.of(
            dynamicTest("should build tree with correct number of root nodes", () -> assertEquals(2, tree.size())),

            dynamicTest("should validate the 'home' hierarchy", () -> {
                NamespaceNodeJson home = findNode(tree, "home");
                assertNotNull(home);
                testMixedHomeNode(home);
            }),

            dynamicTest("should validate the 'system' hierarchy", () -> {
                NamespaceNodeJson system = findNode(tree, "system");
                assertNotNull(system);
                testMixedSystemNode(system);
            })
        );
    }

    private void testMixedHomeNode(NamespaceNodeJson homeNode) {
        var children = homeNode.getChildren();
        var notificationsGroupNode = findNode(children, "notifications");
        var notificationsLeafNode = findNode(children, "home.notifications");
        var overviewLeafNode = findNode(children, "home.overview");

        assertAll("Verify 'home' node and its direct children",
            () -> assertEquals(3, children.size(),
            "'home' node should have 3 direct children: overview, notifications (leaf), and notifications (branch)"),
            () -> {
                assertNotNull(overviewLeafNode);
                assertEquals("R", overviewLeafNode.getAccessMode());
            },
            () -> {
                assertNotNull(notificationsLeafNode);
                assertEquals("R", notificationsLeafNode.getAccessMode());
            },
            () -> {
                assertNotNull(notificationsGroupNode);
                assertEquals(1, notificationsGroupNode.getChildren().size());
                var retryNode = findNode(notificationsGroupNode.getChildren(), "home.notifications.retry");
                assertNotNull(retryNode);
                assertEquals("W", retryNode.getAccessMode());
            }
        );
    }

    private void testMixedSystemNode(NamespaceNodeJson systemNode) {
        var children = systemNode.getChildren();
        assertAll("Verify 'system' node children",
            () -> assertEquals(2, children.size()),
            () -> {
                var testNode = findNode(children, "api.system.test");
                assertNotNull(testNode);
                assertEquals("RW", testNode.getAccessMode());
            },
            () -> {
                var detailsNode = findNode(children, "api.system.details");
                assertNotNull(detailsNode);
                assertEquals("R", detailsNode.getAccessMode());
            }
        );
    }

    /**
     * Helper to create a mock Namespace object.
     */
    private Namespace createNamespace(String namespace, Namespace.AccessMode accessMode) {
        Namespace ns = new Namespace();
        ns.setId(idCounter++);
        ns.setNamespace(namespace);
        ns.setAccessMode(accessMode);
        ns.setDescription("Description for " + namespace);
        return ns;
    }

    /**
     * Helper to find a node in a collection by its exact namespace string.
     *
     * @param nodes     The collection of nodes to search.
     * @param namespace The namespace to find.
     * @return The found node, or null if not found.
     */
    private NamespaceNodeJson findNode(Collection<NamespaceNodeJson> nodes, String namespace) {
        return nodes.stream()
            .filter(n -> namespace.equals(n.getNamespace()))
            .findFirst()
            .orElse(null);
    }
}
