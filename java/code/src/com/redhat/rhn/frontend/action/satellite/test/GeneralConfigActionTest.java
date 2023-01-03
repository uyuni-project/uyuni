/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.satellite.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.action.satellite.GeneralConfigAction;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;

import org.apache.struts.action.DynaActionForm;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * GeneralConfigActionTest
 */
public class GeneralConfigActionTest extends RhnPostMockStrutsTestCase {
    private static final String TEST_CONFIG_BOOLEAN = ConfigDefaults.DISCONNECTED;

    @Test
    public void testTestValue() {

        assertTrue(GeneralConfigAction.getAllowedConfigs().contains(TEST_CONFIG_BOOLEAN));
    }

    @Test
    public void testNonSubmit() {
        user.getOrg().addRole(RoleFactory.SAT_ADMIN);
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        setRequestPathInfo("/admin/config/GeneralConfig");
        Map<String, String> originalConfigValues = new HashMap<>();
        for (String config : GeneralConfigAction.getAllowedConfigs()) {
            String value = Config.get().getString(config);
            if (value != null) {
                originalConfigValues.put(config, value);
                Config.get().setString(config, "1");
            }
        }
        actionPerform();
        DynaActionForm af = (DynaActionForm) getActionForm();
        for (String configName : GeneralConfigAction.getAllowedConfigs()) {
            String config = GeneralConfigAction.translateFormPropertyName(configName);
            String configValue = Config.get().getString(config);
            Object formValue = af.get(config);
            if (configValue != null) {
                assertNotNull(formValue);
                Config.get().setString(config, originalConfigValues.get(config));
            }
        }
    }

    @Test
    public void testSubmit() {
        user.getOrg().addRole(RoleFactory.SAT_ADMIN);
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        setRequestPathInfo("/admin/config/GeneralConfig");
        Config.get().setString("web.com.redhat.rhn.frontend." +
                "action.satellite.GeneralConfigAction.command",
                TestConfigureSatelliteCommand.class.getName());
        addRequestParameter(RhnAction.SUBMITTED, Boolean.TRUE.toString());

        boolean origValue = Config.get().getBoolean(TEST_CONFIG_BOOLEAN);
        boolean changedValue = !origValue;

        addRequestParameter(
                GeneralConfigAction.translateFormPropertyName(
                        TEST_CONFIG_BOOLEAN), Boolean.toString(changedValue));
        actionPerform();
        assertEquals(origValue, Config.get().getBoolean(TEST_CONFIG_BOOLEAN));
        Config.get().setBoolean(TEST_CONFIG_BOOLEAN, Boolean.toString(origValue));
        verifyForward("failure");

        addRequestParameter(
                GeneralConfigAction.translateFormPropertyName("traceback_mail"),
                "testuser@redhat.com");
        addRequestParameter(
                GeneralConfigAction.translateFormPropertyName("java.hostname"),
                "testbox");

        actionPerform();

        assertEquals("testuser@redhat.com", Config.get().getString("traceback_mail"));
        assertEquals("testbox", Config.get().getString("java.hostname"));

        verifyActionMessages(new String[] {"config.restartrequired"});


        Config.get().setBoolean(TEST_CONFIG_BOOLEAN, Boolean.toString(origValue));
    }

}

