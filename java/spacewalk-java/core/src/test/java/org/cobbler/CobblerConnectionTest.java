/*
 * Copyright (c) 2022 SUSE LLC
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
package org.cobbler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CobblerConnectionTest {
    @Test
    public void testCobblerConnectionUrl() {
        // Arrange
        String expectedResult = "http://localhost/cobbler_api";

        // Act
        CobblerConnection result = new CobblerConnection("http://localhost");

        // Assert
        Assertions.assertEquals(result.getUrl(), expectedResult);
    }

    @Test
    public void testLogin() {
        // Arrange
        String username = "test";
        String password = "test";
        String expectedResult = "MyFakeToken";
        CobblerConnection connection = new CobblerConnection("http://localhost", new MockXmlRpcClient());

        // Act
        String result = connection.login(username, password);

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testInvokeMethod() {
        // Arrange
        CobblerConnection connection = new CobblerConnection("http://localhost", new MockXmlRpcClient());

        // Act
        Object result = connection.invokeMethod("test_invoke_method", "argument");

        // Assert
        Assertions.assertEquals("Test passed", result);
    }

    @Test
    public void testInvokeTokenMethod() {
        // Arrange
        CobblerConnection connection = new CobblerConnection("http://localhost", new MockXmlRpcClient());
        connection.setToken("my_test_token");

        // Act
        boolean result = (boolean) connection.invokeTokenMethod("test_invoke_token_method", "argument");

        // Assert
        // The Mock API client will throw a RunTimeException if the token is not attached to the method call.
        Assertions.assertTrue(result);
    }

    @Test
    public void testUrl() {
        // Arrange
        String expectedResult = "http://localhost/cobbler_api";
        CobblerConnection connection = new CobblerConnection("http://localhost");

        // Act
        String result = connection.getUrl();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testVersion() {
        // Arrange
        Double expectedResult = 2.2;
        CobblerConnection connection = new CobblerConnection("http://localhost", new MockXmlRpcClient());

        // Act
        Double result = connection.getVersion();

        // Assert
        Assertions.assertEquals(expectedResult, result);
    }
}
