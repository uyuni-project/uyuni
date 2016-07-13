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

    /**
     * Test the check for required fields.
     */
    public void testValidateBootstrapInputEmpty() {
        String json = "{}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 2);
        assertTrue(validationErrors.contains("Host is required."));
        assertTrue(validationErrors.contains("User is required."));
    }

    /**
     * Test minimal input (only host and user).
     */
    public void testValidateBootstrapInputMinimal() {
        String json = "{host: 'host.domain.com', user: 'root'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }

    /**
     * Test the check for non numeric port numbers.
     */
    public void testValidateBootstrapInputPortNotNumeric() {
        String json = "{host: 'host.domain.com', user: 'root', port: 'abcdef'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains("Given port is not a valid number."));
    }

    /**
     * Test the check for port numbers outside the valid range.
     */
    public void testValidateBootstrapInputPortRange() {
        String json = "{host: 'host.domain.com', user: 'root', port: '99999'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains("Given port is outside of the valid range (1-65535)."));

        json = "{host: 'host.domain.com', user: 'root', port: '-1'}";
        input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.size() == 1);
        assertTrue(validationErrors.contains("Given port is outside of the valid range (1-65535)."));
    }

    /**
     * Verify that valid port numbers validate.
     */
    public void testValidateBootstrapInputPortValid() {
        String json = "{host: 'host.domain.com', user: 'root', port: '8888'}";
        JSONBootstrapHosts input = MinionsAPI.GSON.fromJson(json, JSONBootstrapHosts.class);
        List<String> validationErrors = InputValidator.validateBootstrapInput(input);
        assertTrue(validationErrors.isEmpty());
    }
}
