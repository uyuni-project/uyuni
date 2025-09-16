/*
 * Copyright (c) 2016 SUSE LLC
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.BootstrapHostsJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for the InputValidator.
 */
public class InputValidatorTest  {

    private static final String HOST_ERROR_MESSAGE = "Invalid host name.";
    private static final String USER_ERROR_MESSAGE = "Non-valid user. Allowed characters" +
            " are: letters, numbers, '.', '\\', '-' and '_'";
    private static final String PORT_ERROR_MESSAGE = "Port must be a number within range" +
            " 1-65535.";

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final CloudPaygManager paygManager = new TestCloudPaygManagerBuilder().build();
    private final AttestationManager attMgr = new AttestationManager();

    /**
     * Test the check for required fields.
     */
    @Test
    public void testValidateBootstrapInputUserEmpty() {
        String json = "{user: ''}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(2, validationErrors.size());
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
        assertTrue(validationErrors.contains(USER_ERROR_MESSAGE));
    }

    /**
     * Test the check for user with letters and numbers.
     */
    @Test
    public void testValidateBootstrapInputUserLettersNumbers() {
        String json = "{user: 'Admin1', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with dot.
     */
    @Test
    public void testValidateBootstrapInputUserDot() {
        String json = "{user: 'my.admin', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with backslash.
     */
    @Test
    public void testValidateBootstrapInputUserBackslash() {
        String json = "{user: 'domain\\\\admin', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with dash.
     */
    @Test
    public void testValidateBootstrapInputUserDash() {
        String json = "{user: 'my-admin', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with underscore.
     */
    @Test
    public void testValidateBootstrapInputUserUnderscore() {
        String json = "{user: 'my_admin', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with invalid character.
     */
    @Test
    public void testValidateBootstrapInputUserInvalid() {
        String json = "{user: '$(execme)', host: 'host.domain.com'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains(USER_ERROR_MESSAGE));
    }

    /**
     * Test the check for host with invalid character.
     */
    @Test
    public void testValidateBootstrapInputHostInvalid() {
        String json = "{user: 'toor', host: '`execme`'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
    }

    /**
     * Test the check for host as an IPv4.
     */
    @Test
    public void testValidateBootstrapInputHostIPv4() {
        String json = "{user: 'toor', host: '192.168.1.1'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for host as an IPv6.
     */
    @Test
    public void testValidateBootstrapInputHostIPv6() {
        String json = "{user: 'toor', host: '[2001:0db8:0000:0000:0000:0000:1428:57ab]'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for required fields, user is "root" per default.
     */
    @Test
    public void testValidateBootstrapInputDefaultUser() {
        String json = "{}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
    }

    /**
     * Test minimal input (only host and user).
     */
    @Test
    public void testValidateBootstrapInputMinimal() {
        String json = "{host: 'host.domain.com', user: 'root'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for empty port numbers.
     */
    @Test
    public void testValidateBootstrapInputPortEmpty() {
        String json = "{host: 'host.domain.com', user: 'root', port: ''}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for port numbers outside the valid range.
     */
    @Test
    public void testValidateBootstrapInputPortRange() {
        String json = "{host: 'host.domain.com', user: 'root', port: '99999'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains(PORT_ERROR_MESSAGE));

        json = "{host: 'host.domain.com', user: 'root', port: '-1'}";
        input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr).createBootstrapParams(input);
        validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertEquals(1, validationErrors.size());
        assertTrue(validationErrors.contains(PORT_ERROR_MESSAGE));
    }

    /**
     * Verify that valid port numbers validate.
     */
    @Test
    public void testValidateBootstrapInputPortValid() {
        String json = "{host: 'host.domain.com', user: 'root', port: '8888'}";
        BootstrapHostsJson input = MinionsAPI.GSON.fromJson(json, BootstrapHostsJson.class);
        BootstrapParameters params = new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attMgr)
                .createBootstrapParams(input);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(params);
        assertTrue(validationErrors.isEmpty());
    }
}
