/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.manager.satellite.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * ConfigureSatelliteCommandTest - test for ConfigureSatelliteCommand
 */
public class ConfigureSatelliteCommandTest extends BaseTestCaseWithUser {

    private ConfigureSatelliteCommand cmd;
    private static final String TEST_CONFIG_BOOLEAN = "test.boolean_config.config_sat_test";
    private static final String TEST_CONFIG_STRING = "test.string_config.config_sat_test";
    private static final String TEST_CONFIG_NULL = "test.null_config.config_sat_test";


    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
    }

    @Test
    public void testCreateCommand() {

        cmd = new ConfigureSatelliteCommand(user) {
            @Override
            public ValidatorError[] storeConfiguration() {
                this.clearUpdates();
                return null;
            }
        };
        assertNotNull(cmd.getUser());
        boolean origValue = Config.get().getBoolean(TEST_CONFIG_BOOLEAN);
        String testString = "somevalue" + TestUtils.randomString();
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, !origValue);
        cmd.updateString(TEST_CONFIG_STRING, testString);
        cmd.updateString(TEST_CONFIG_NULL, "");
        assertEquals(3, cmd.getKeysToBeUpdated().size());
        assertTrue(cmd.getKeysToBeUpdated().contains(TEST_CONFIG_BOOLEAN));
        assertTrue(cmd.getKeysToBeUpdated().contains(TEST_CONFIG_STRING));
        assertTrue(cmd.getKeysToBeUpdated().contains(TEST_CONFIG_NULL));

        Map optionMap = new TreeMap<>();
        for (String key : cmd.getKeysToBeUpdated()) {
            optionMap.put(key, Config.get().getString(key));
        }
        String[] cmdargs = cmd.getCommandArguments(Config.getDefaultConfigFilePath(),
                optionMap, Collections.<String>emptyList());

        assertEquals("--option=test.null_config.config_sat_test=", cmdargs[4]);
        assertEquals(9, cmdargs.length);
        assertNull(cmd.storeConfiguration());
        assertTrue(cmd.getKeysToBeUpdated().isEmpty());
        // Test setting back to the original value
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, origValue);
        assertEquals(1, cmd.getKeysToBeUpdated().size());
        assertNull(cmd.storeConfiguration());
        // Test NULL booleans
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, Boolean.TRUE);
        assertNull(cmd.storeConfiguration());
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, null);
        assertEquals(1, cmd.getKeysToBeUpdated().size());
        assertNull(cmd.storeConfiguration());

        // Now test to see if updating it to FALSE doesnt
        // indicate we need actual changes written out.
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, Boolean.FALSE);
        assertTrue(cmd.getKeysToBeUpdated().isEmpty());
        cmd.updateString(TEST_CONFIG_STRING, testString);
        assertTrue(cmd.getKeysToBeUpdated().isEmpty());

    }

    @Test
    public void testRemoveEntries() {

        cmd = new ConfigureSatelliteCommand(user) {
            @Override
            public ValidatorError[] storeConfiguration() {
                this.clearUpdates();
                return null;
            }
        };

        cmd.updateString(TEST_CONFIG_STRING, "initialvalue");
        cmd.updateBoolean(TEST_CONFIG_BOOLEAN, true);

        assertEquals(2, cmd.getKeysToBeUpdated().size());
        assertNull(cmd.storeConfiguration());
        assertEquals(0, cmd.getKeysToBeUpdated().size());

        // now remove them

        cmd.updateString(TEST_CONFIG_STRING, "somevalue");
        cmd.remove(TEST_CONFIG_BOOLEAN);
        cmd.remove("nonexistantkey");

        // nonexistantkey should not be part as it did not exist
        assertEquals(2, cmd.getKeysToBeUpdated().size());

        String[] cmdargs = cmd.getCommandArguments();
        assertEquals("--target=" + Config.getDefaultConfigFilePath(), cmdargs[2]);
        assertEquals("--option=" + TEST_CONFIG_STRING + "=somevalue", cmdargs[3]);
        assertEquals("--remove=" + TEST_CONFIG_BOOLEAN, cmdargs[4]);

        assertEquals(8, cmdargs.length);
        assertNull(cmd.storeConfiguration());
        assertEquals(0, cmd.getKeysToBeUpdated().size());
    }

    @Test
    public void testUpdateHostname() {

        cmd = new ConfigureSatelliteCommand(user) {
            @Override
            protected Executor getExecutor() {
                return new TestExecutor();
            }
        };

        cmd.updateString(ConfigDefaults.SERVER_HOSTNAME, "test.hostname.jabber");
        ValidatorError[] verrors = cmd.storeConfiguration();
        assertNull(verrors);
    }


    @Test
    public void testMountPoint() {

        cmd = new ConfigureSatelliteCommand(user) {
            @Override
            protected Executor getExecutor() {
                return new TestExecutor();
            }
        };

        String testmount = "/tmp/mount/point";
        cmd.updateString(ConfigDefaults.MOUNT_POINT, testmount);
        ValidatorError[] verrors = cmd.storeConfiguration();
        assertNull(verrors);
        assertEquals(testmount,
                Config.get().getString(ConfigDefaults.KICKSTART_MOUNT_POINT));
    }

    @Test
    public void testRoles() {

        user.removePermanentRole(RoleFactory.SAT_ADMIN);
        try {
            cmd = new ConfigureSatelliteCommand(user);
            fail("Should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException iae) {
            // noop
        }
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        cmd = new ConfigureSatelliteCommand(user);
        assertNotNull(cmd);
    }

    public class TestExecutor implements Executor {
        @Override
        public int execute(String[] args) {
            return 0;
        }

        @Override
        public String getLastCommandOutput() {
            return null;
        }

        @Override
        public String getLastCommandErrorMessage() {
            return null;
        }
    }
}
