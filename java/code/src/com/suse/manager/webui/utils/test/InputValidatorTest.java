/**
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

import com.suse.manager.webui.controllers.MinionsAPI;
import com.suse.manager.webui.utils.InputValidator;
import com.suse.manager.webui.utils.gson.JSONBootstrapHosts;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for the InputValidator.
 */
public class InputValidatorTest extends TestCase {

    private static final String HOST_ERROR_MESSAGE = "Invalid host name.";
    private static final String USER_ERROR_MESSAGE = "Non-valid user. Allowed characters" +
            " are: letters, numbers, '.', '\\' and '/'";
    private static final String PORT_ERROR_MESSAGE = "Port must be a number within range" +
            " 1-65535.";

    /**
     * Test the check for required fields.
     */
    public void testValidateBootstrapInputUserEmpty() {
        String json = "{user: ''}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 2);
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
        assertTrue(validationErrors.contains(USER_ERROR_MESSAGE));
    }

    /**
     * Test the check for user with backslash.
     */
    public void testValidateBootstrapInputUserBackslash() {
        String json = "{user: 'domain\\\\admin', host: 'host.domain.com'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for user with invalid character.
     */
    public void testValidateBootstrapInputUserInvalid() {
        String json = "{user: '$(execme)', host: 'host.domain.com'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(USER_ERROR_MESSAGE));
    }

    /**
     * Test the check for host with invalid character.
     */
    public void testValidateBootstrapInputHostInvalid() {
        String json = "{user: 'toor', host: '`execme`'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
    }

    /**
     * Test the check for host as an IPv4.
     */
    public void testValidateBootstrapInputHostIPv4() {
        String json = "{user: 'toor', host: '192.168.1.1'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for host as an IPv6.
     */
    public void testValidateBootstrapInputHostIPv6() {
        String json = "{user: 'toor', host: '[2001:0db8:0000:0000:0000:0000:1428:57ab]'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for required fields, user is "root" per default.
     */
    public void testValidateBootstrapInputDefaultUser() {
        String json = "{}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(HOST_ERROR_MESSAGE));
    }

    /**
     * Test minimal input (only host and user).
     */
    public void testValidateBootstrapInputMinimal() {
        String json = "{host: 'host.domain.com', user: 'root'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for empty port numbers.
     */
    public void testValidateBootstrapInputPortEmpty() {
        String json = "{host: 'host.domain.com', user: 'root', port: ''}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for non numeric port numbers.
     */
    public void testValidateBootstrapInputPortNotNumeric() {
        String json = "{host: 'host.domain.com', user: 'root', port: 'abcdef'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(PORT_ERROR_MESSAGE));
    }

    /**
     * Test the check for port numbers outside the valid range.
     */
    public void testValidateBootstrapInputPortRange() {
        String json = "{host: 'host.domain.com', user: 'root', port: '99999'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(PORT_ERROR_MESSAGE));

        json = "{host: 'host.domain.com', user: 'root', port: '-1'}";
        input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains(PORT_ERROR_MESSAGE));
    }

    /**
     * Verify that valid port numbers validate.
     */
    public void testValidateBootstrapInputPortValid() {
        String json = "{host: 'host.domain.com', user: 'root', port: '8888'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.INSTANCE.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }
}
