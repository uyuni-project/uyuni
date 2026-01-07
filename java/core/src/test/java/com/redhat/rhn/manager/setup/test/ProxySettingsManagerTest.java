/*
 * Copyright (c) 2014--2025 SUSE LLC
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
package com.redhat.rhn.manager.setup.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;
import com.redhat.rhn.manager.satellite.ProxySettingsConfigureSatelliteCommand;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.ProxySettingsManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Tests for {@link ProxySettingsManager}.
 */
public class ProxySettingsManagerTest extends RhnBaseTestCase {

    private static final String TEST_HOSTNAME = "proxy.foobar.com";
    private static final String TEST_USERNAME = "foobaruser";
    private static final String TEST_PASSWD = "foobarpasswd";

    private ProxySettingsDto proxySettingsDto;
    private User satAdmin;

    @BeforeEach
    public void setupTest() {
        satAdmin = UserTestUtils.createUser(TestStatics.TEST_SAT_USER, "satOrg");
        satAdmin.addPermanentRole(RoleFactory.SAT_ADMIN);
        UserFactory.save(satAdmin);

        proxySettingsDto = new ProxySettingsDto();
        proxySettingsDto.setHostname(TEST_HOSTNAME);
        proxySettingsDto.setUsername(TEST_USERNAME);
        proxySettingsDto.setPassword(TEST_PASSWD);

        Config.get().remove(ConfigDefaults.HTTP_PROXY);
        Config.get().remove(ConfigDefaults.HTTP_PROXY_USERNAME);
        Config.get().remove(ConfigDefaults.HTTP_PROXY_PASSWORD);

    }

    static class MockProxySettingsConfigureCommand extends ProxySettingsConfigureSatelliteCommand {
        MockProxySettingsConfigureCommand(User userIn) {
            super(userIn);
        }
        public String getFirstEnvironmentVar() {
            if ((null == environmentVars) || (0 == environmentVars.length)) {
                return "";
            }
            return environmentVars[0];
        }
    }

    /**
     * Tests getProxySettings().
     * @throws Exception if something goes wrong
     */
    @Test
    public void testGetProxySettings() {
        setProxySettings(proxySettingsDto);
        ProxySettingsDto proxySettingsResult = ProxySettingsManager.getProxySettings();
        assertEquals(proxySettingsDto.getHostname(), proxySettingsResult.getHostname());
        assertEquals(proxySettingsDto.getUsername(), proxySettingsResult.getUsername());
        assertNull(proxySettingsResult.getPassword());
    }

    /**
     * Sets the proxy settings.
     *
     * @param proxy the new proxy settings
     */
    public static void setProxySettings(ProxySettingsDto proxy) {
        Config.get().setString(ConfigDefaults.HTTP_PROXY, proxy.getHostname());
        Config.get().setString(ConfigDefaults.HTTP_PROXY_USERNAME, proxy.getUsername());
        Config.get().setString(ConfigDefaults.HTTP_PROXY_PASSWORD, proxy.getPassword());
    }


    @Test
    public void testConfigureSatelliteCommandArguments() {
        ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(satAdmin);
        configCommand.updateString(ConfigDefaults.HTTP_PROXY, proxySettingsDto.getHostname());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_USERNAME, proxySettingsDto.getUsername());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_PASSWORD, proxySettingsDto.getPassword());

        String[] cmdArgs = configCommand.getCommandArguments();
        assertEquals(9, cmdArgs.length);
        assertEquals("/usr/bin/sudo", cmdArgs[0]);
        assertEquals("/usr/bin/rhn-config-satellite.pl", cmdArgs[1]);
        assertTrue(cmdArgs[2].startsWith("--target="));
        assertEquals("--option=server.satellite.http_proxy=%s".formatted(TEST_HOSTNAME), cmdArgs[3]);
        assertEquals("--option=server.satellite.http_proxy_username=%s".formatted(TEST_USERNAME), cmdArgs[4]);
        assertEquals("--option=server.satellite.http_proxy_password=%s".formatted(TEST_PASSWD), cmdArgs[5]);
        assertEquals("2>&1", cmdArgs[6]);
        assertEquals(">", cmdArgs[7]);
        assertEquals("/dev/null", cmdArgs[8]);
    }

    @Test
    @Disabled("Doesn't test anything useful and takes 5 minutes when running tests outside of a container")
    public void testProxySettingsManagerStoreProxySettings() {
        HttpServletRequest request = new RhnMockHttpServletRequest();
        ProxySettingsDto proxySettingsDtoToStore = new ProxySettingsDto();
        proxySettingsDtoToStore.setHostname(TEST_HOSTNAME + TestUtils.randomString());
        proxySettingsDtoToStore.setUsername(TEST_USERNAME + TestUtils.randomString());
        proxySettingsDtoToStore.setPassword(TEST_PASSWD + TestUtils.randomString());

        ValidatorError[] errors = ProxySettingsManager.storeProxySettings(proxySettingsDtoToStore, satAdmin, request);
        assertEquals(1, errors.length);
        assertEquals("config.storeconfig.error", errors[0].getKey());
        // error: sudo: a terminal is required to read the password; either use the -S option to read
        // from standard input or configure an askpass helper
    }

    @Test
    public void testProxySettingsConfigureSatelliteCommandArguments() {
        MockProxySettingsConfigureCommand configCommand = new MockProxySettingsConfigureCommand(satAdmin);
        configCommand.updateString(ConfigDefaults.HTTP_PROXY, proxySettingsDto.getHostname());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_USERNAME, proxySettingsDto.getUsername());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_PASSWORD, proxySettingsDto.getPassword());

        String[] cmdArgs = configCommand.getCommandArguments();
        assertEquals(10, cmdArgs.length);
        assertEquals("/usr/bin/sudo", cmdArgs[0]);
        assertEquals("-E", cmdArgs[1]);
        assertEquals("/usr/bin/rhn-config-satellite.pl", cmdArgs[2]);
        assertTrue(cmdArgs[3].startsWith("--target="));
        assertEquals("--option=server.satellite.http_proxy=%s".formatted(TEST_HOSTNAME), cmdArgs[4]);
        assertEquals("--option=server.satellite.http_proxy_username=%s".formatted(TEST_USERNAME), cmdArgs[5]);
        assertEquals("--option=server.satellite.http_proxy_password=PWD_PLACEHOLDER", cmdArgs[6]);
        assertEquals("2>&1", cmdArgs[7]);
        assertEquals(">", cmdArgs[8]);
        assertEquals("/dev/null", cmdArgs[9]);

        assertEquals("UYUNICFG_PWD_PLACEHOLDER=%s".formatted(TEST_PASSWD), configCommand.getFirstEnvironmentVar());
    }

    @Test
    public void testProxySettingsConfigureSatelliteCommandArgumentsPartialArguments() {
        MockProxySettingsConfigureCommand configCommand = new MockProxySettingsConfigureCommand(satAdmin);
        configCommand.updateString(ConfigDefaults.HTTP_PROXY, "");
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_USERNAME, proxySettingsDto.getUsername());

        String[] cmdArgs = configCommand.getCommandArguments();
        assertEquals(8, cmdArgs.length);
        assertEquals("/usr/bin/sudo", cmdArgs[0]);
        assertEquals("/usr/bin/rhn-config-satellite.pl", cmdArgs[1]);
        assertTrue(cmdArgs[2].startsWith("--target="));
        assertEquals("--option=server.satellite.http_proxy=", cmdArgs[3]);
        assertEquals("--option=server.satellite.http_proxy_username=%s".formatted(TEST_USERNAME), cmdArgs[4]);
        assertEquals("2>&1", cmdArgs[5]);
        assertEquals(">", cmdArgs[6]);
        assertEquals("/dev/null", cmdArgs[7]);
    }

    @Test
    public void testProxySettingsConfigureSatelliteCommandArgumentsOnlyPasswd() {
        MockProxySettingsConfigureCommand configCommand = new MockProxySettingsConfigureCommand(satAdmin);
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_PASSWORD, proxySettingsDto.getPassword());

        String[] cmdArgs = configCommand.getCommandArguments();
        assertEquals(8, cmdArgs.length);
        assertEquals("/usr/bin/sudo", cmdArgs[0]);
        assertEquals("-E", cmdArgs[1]);
        assertEquals("/usr/bin/rhn-config-satellite.pl", cmdArgs[2]);
        assertTrue(cmdArgs[3].startsWith("--target="));
        assertEquals("--option=server.satellite.http_proxy_password=PWD_PLACEHOLDER", cmdArgs[4]);
        assertEquals("2>&1", cmdArgs[5]);
        assertEquals(">", cmdArgs[6]);
        assertEquals("/dev/null", cmdArgs[7]);

        assertEquals("UYUNICFG_PWD_PLACEHOLDER=%s".formatted(TEST_PASSWD), configCommand.getFirstEnvironmentVar());
    }

    @Test
    public void testProxySettingsConfigureSatelliteCommandArgumentsOnlyEmptyPasswd() {
        MockProxySettingsConfigureCommand configCommand = new MockProxySettingsConfigureCommand(satAdmin);

        proxySettingsDto.setPassword("");
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_PASSWORD, proxySettingsDto.getPassword());

        String[] cmdArgs = configCommand.getCommandArguments();
        assertEquals(8, cmdArgs.length);
        assertEquals("/usr/bin/sudo", cmdArgs[0]);
        assertEquals("-E", cmdArgs[1]);
        assertEquals("/usr/bin/rhn-config-satellite.pl", cmdArgs[2]);
        assertTrue(cmdArgs[3].startsWith("--target="));
        assertEquals("--option=server.satellite.http_proxy_password=PWD_PLACEHOLDER", cmdArgs[4]);
        assertEquals("2>&1", cmdArgs[5]);
        assertEquals(">", cmdArgs[6]);
        assertEquals("/dev/null", cmdArgs[7]);

        assertEquals("UYUNICFG_PWD_PLACEHOLDER=", configCommand.getFirstEnvironmentVar());
    }
}
