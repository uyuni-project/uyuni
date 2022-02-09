/*
 * Copyright (c) 2021 SUSE LLC
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

package com.suse.manager.admin.validator.test;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.admin.PaygAdminFields;
import com.suse.manager.admin.validator.PaygAdminValidator;

import java.util.stream.Stream;

import junit.framework.TestCase;

public class PaygAdminValidatorTest extends TestCase {
    private static final String DESCRITPION = "description";
    private static final String HOST = "my.host";
    private static final Integer PORT = 22;
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String KEY = "key";
    private static final String KEY_PASSWORD = "key password";

    private static final String BASTION_HOST = "my.bastion.host";
    private static final Integer BASTION_PORT = 23;
    private static final String BASTION_USERNAME = "bastion_username";
    private static final String BASTION_PASSWORD = "bastion_password";
    private static final String BASTION_KEY = "bastion key";
    private static final String BASTION_KEY_PASSWORD = "bastion_key_pass";

    public void testDescriptionErrors() {
        Stream.of(null, "", TestUtils.randomString(256))
                .forEach(description -> {
                    try {
                        PaygAdminValidator.validatePaygData(description,
                                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors().get(PaygAdminFields.description.name()).size());
                    }
                });
    }


    public void testDescriptionSuccess() {
        Stream.of(TestUtils.randomString(1),
                TestUtils.randomString(255))
               .forEach(description -> {
                   PaygAdminValidator.validatePaygData(
                           description,
                           HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                           BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                           BASTION_KEY, BASTION_KEY_PASSWORD);
                   assertTrue(true);
                });

    }

    public void testHostError() {
        Stream.of(null, "", TestUtils.randomString(256))
                .forEach(host -> {
                    try {
                        PaygAdminValidator.validatePaygData(DESCRITPION,
                                host,
                                PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors().get(PaygAdminFields.host.name()).size());
                    }
                });

    }

    public void testHostSuccess() {
        Stream.of(TestUtils.randomString(1),
                        TestUtils.randomString(255))
                .forEach(host -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            host, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                            BASTION_KEY, BASTION_KEY_PASSWORD);
                    assertTrue(true);
                });
    }

    public void testPortError() {
        Stream.of(0, -1)
                .forEach(port -> {
                    try {
                        PaygAdminValidator.validatePaygData(DESCRITPION,
                                HOST,
                                port, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors()
                                .get(PaygAdminFields.port.name()).size());
                    }
                });
    }

    public void testPortSuccess() {
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, 22, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, null, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        assertTrue(true);
    }

    public void testUsernameError() {
        Stream.of(null, "", TestUtils.randomString(33))
                .forEach(username -> {
                    try {
                        PaygAdminValidator.validatePaygData(DESCRITPION,
                                HOST, PORT, username, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors()
                                .get(PaygAdminFields.username.name()).size());
                    }
                });

    }

    public void testUsernameSuccess() {
        Stream.of(TestUtils.randomString(1),
                        TestUtils.randomString(32))
                .forEach(username -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            HOST, PORT, username, PASSWORD, KEY, KEY_PASSWORD,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                            BASTION_KEY, BASTION_KEY_PASSWORD);
                    assertTrue(true);
                });
    }

    public void testPasswordError() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, TestUtils.randomString(33), KEY, KEY_PASSWORD,
                    BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                    BASTION_KEY, BASTION_KEY_PASSWORD);
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.password.name()).size());
        }

    }

    public void testPassswordSuccess() {
        Stream.of(null, TestUtils.randomString(1),
                        TestUtils.randomString(32))
                .forEach(password -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            HOST, PORT, USERNAME, password, KEY, KEY_PASSWORD,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                            BASTION_KEY, BASTION_KEY_PASSWORD);
                    assertTrue(true);
                });
    }

    public void testKeyPasswordError() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, PASSWORD, KEY, TestUtils.randomString(33),
                    BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                    BASTION_KEY, BASTION_KEY_PASSWORD);
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.key_password.name()).size());
        }

    }

    public void testKeyPassswordSuccess() {
        Stream.of(null, TestUtils.randomString(1),
                        TestUtils.randomString(32))
                .forEach(keyPassword -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            HOST, PORT, USERNAME, PASSWORD, KEY, keyPassword,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                            BASTION_KEY, BASTION_KEY_PASSWORD);
                    assertTrue(true);
                });
    }


    public void testBastionHostError1() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                    TestUtils.randomString(256), BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                    BASTION_KEY, BASTION_KEY_PASSWORD);
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.bastion_host.name()).size());
        }
    }

    public void testBastionHostError2() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                    null, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                    BASTION_KEY, BASTION_KEY_PASSWORD);
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.bastion_host.name()).size());
        }
    }

    public void testBastionHostSuccess1() {
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                TestUtils.randomString(1), BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                TestUtils.randomString(255), BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                null, null, null, null,
                null, null);
        assertTrue(true);
    }

    public void testBastionPortError() {
        Stream.of(0, -1)
                .forEach(port -> {
                    try {
                        PaygAdminValidator.validatePaygData(DESCRITPION,
                                HOST,
                                PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, port, BASTION_USERNAME, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors()
                                .get(PaygAdminFields.bastion_port.name()).size());
                    }
                });
    }

    public void testBastionPortSuccess() {
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, 22, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, null, BASTION_USERNAME, BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        assertTrue(true);
    }

    public void testBastionUsernameError() {
        Stream.of(null, "", TestUtils.randomString(33))
                .forEach(username -> {
                    try {
                        PaygAdminValidator.validatePaygData(DESCRITPION,
                                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                                BASTION_HOST, BASTION_PORT, username, BASTION_PASSWORD,
                                BASTION_KEY, BASTION_KEY_PASSWORD);
                        fail("ValidatorException should be thrown");

                    }
                    catch (ValidatorException e) {
                        assertEquals(1, e.getResult().getFieldErrors().size());
                        assertEquals(1, e.getResult().getFieldErrors()
                                .get(PaygAdminFields.bastion_username.name()).size());
                    }
                });

    }

    public void testBastionUsernameSuccess() {
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, BASTION_PORT,
                TestUtils.randomString(1), BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                BASTION_HOST, BASTION_PORT,
                TestUtils.randomString(32), BASTION_PASSWORD,
                BASTION_KEY, BASTION_KEY_PASSWORD);
        PaygAdminValidator.validatePaygData(
                DESCRITPION,
                HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                null, null,
                null, null, null, null);
        assertTrue(true);
    }

    public void testBastionPasswordError() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                    BASTION_HOST, BASTION_PORT, BASTION_USERNAME,
                    TestUtils.randomString(33),
                    BASTION_KEY, BASTION_KEY_PASSWORD);
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.bastion_password.name()).size());
        }

    }

    public void testBastionPassswordSuccess() {
        Stream.of(null, TestUtils.randomString(1),
                        TestUtils.randomString(32))
                .forEach(password -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, password,
                            BASTION_KEY, BASTION_KEY_PASSWORD);
                    assertTrue(true);
                });
    }

    public void testBastionKeyPasswordError() {
        try {
            PaygAdminValidator.validatePaygData(DESCRITPION,
                    HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                    BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                    BASTION_KEY, TestUtils.randomString(33));
            fail("ValidatorException should be thrown");

        }
        catch (ValidatorException e) {
            assertEquals(1, e.getResult().getFieldErrors().size());
            assertEquals(1, e.getResult().getFieldErrors()
                    .get(PaygAdminFields.bastion_key_password.name()).size());
        }

    }

    public void testBastionKeyPassswordSuccess() {
        Stream.of(null, TestUtils.randomString(1),
                        TestUtils.randomString(32))
                .forEach(keyPassword -> {
                    PaygAdminValidator.validatePaygData(
                            DESCRITPION,
                            HOST, PORT, USERNAME, PASSWORD, KEY, KEY_PASSWORD,
                            BASTION_HOST, BASTION_PORT, BASTION_USERNAME, BASTION_PASSWORD,
                            BASTION_KEY, keyPassword);
                    assertTrue(true);
                });
    }
}
