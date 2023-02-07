/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.common.validator.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.validator.Constraint;
import com.redhat.rhn.common.validator.DataConverter;
import com.redhat.rhn.common.validator.Validator;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * ValidatorTest
 */
public class ValidatorTest  {

    private Validator validator;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.disableLocalizationLogging();
        validator = Validator.getInstance(TestUtils.findTestData("TestObject.xsd"));
    }

    /**
     * {@inheritDoc}
     */
    @AfterEach
    public void tearDown() {
        TestUtils.enableLocalizationLogging();
    }



    @Test
    public void testDataConverter() {
        DataConverter dc = DataConverter.getInstance();
        assertNotNull(dc.getJavaType("date"));
        assertNotNull(dc.getJavaType("string"));
        assertNotNull(dc.getJavaType("long"));
        assertNotNull(dc.getSchemaType("Date"));
        assertNotNull(dc.getSchemaType("String"));
        assertNotNull(dc.getSchemaType("Long"));


    }

    @Test
    public void testGetConstraints() {
        assertFalse(validator.getConstraints().isEmpty());
        Object constraint = validator.getConstraints().get(0);
        assertTrue(constraint instanceof Constraint);
    }

    @Test
    public void testNullValue() {
        TestObject to = new TestObject();
        assertNotNull(validator.validate("stringField", to));
    }

    @Test
    public void testStringLength() {
        TestObject to = new TestObject();
        to.setStringField("short");
        assertNull(validator.validate("stringField", to));
        to.setStringField("somethingthatistoolongandshouldfail");
        assertNotNull(validator.validate("stringField", to));
        to.setStringField("");
        assertNotNull(validator.validate("stringField", to));
        to.setStringField("    ");
        assertNotNull(validator.validate("stringField", to));
        to.setTwoCharField("it");
        assertNull(validator.validate("twoCharField", to));
    }

    @Test
    public void testASCIIString() {
        TestObject to = new TestObject();
        to.setAsciiString("shughes_login");
        assertNull(validator.validate("asciiString", to));
        to.setAsciiString("機能拡張を");
        assertNotNull(validator.validate("asciiString", to));
    }

    @Test
    public void testUserNameString() {
        TestObject to = new TestObject();

        // bad user names
        to.setUsernameString("foo&user");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("joe+page");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("joe user");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("10%users");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("joe'suser");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("`eval`");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("joe=page");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("foo#user");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("joe\"user");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("機能拡張を");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("shughes login");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString("shughes%login");
        assertNotNull(validator.validate("usernameString", to));
        to.setUsernameString(" shughes");
        assertNotNull(validator.validate("usernameString", to));

        // good user names
        to.setUsernameString("john.cusack@foobar.com");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("a$user");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("!@$^*()-_{}[]|\\:;?");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("/usr/bin");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("shughes_login");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("shughes@redhat.com");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("/shughes_login");
        assertNull(validator.validate("usernameString", to));
        to.setUsernameString("/\\/\\ark");
        assertNull(validator.validate("usernameString", to));

    }

    @Test
    public void testPosixUsername() {
        TestObject to = new TestObject();

        // valid user names
        to.setPosixString("ab");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("AB");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("09");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("aA0");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("_-.");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("a_B-0.Z");
        assertNull(validator.validate("posixString", to));
        to.setPosixString("shughes_login");
        assertNull(validator.validate("posixString", to));

        // Should fail
        to.setPosixString("-ab");
        assertNotNull(validator.validate("posixString", to));

        to.setPosixString("john.cusack@foobar.com");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("a$user");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("!@$^*()-_{}[]|\\:;?");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("/usr/bin");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("shughes@redhat.com");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("/shughes_login");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("/\\/\\ark");
        assertNotNull(validator.validate("posixString", to));

        to.setPosixString("foo&user");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("joe+page");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("joe user");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("10%users");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("joe'suser");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("`eval`");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("joe=page");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("foo#user");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("joe\"user");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("機能拡張を");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("shughes login");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString("shughes%login");
        assertNotNull(validator.validate("posixString", to));
        to.setPosixString(" shughes");
        assertNotNull(validator.validate("posixString", to));
    }

    @Test
    public void testDateField() {
        TestObject to = new TestObject();
        to.setDateField(new Date());
        assertNull(validator.validate("dateField", to));
    }

    @Test
    public void testLongField() {

        TestObject to = new TestObject();
        to.setLongField(10L);
        assertNull(validator.validate("longField", to));
        to.setLongField(100L);
        assertNotNull(validator.validate("longField", to));
        to.setThirdLongField(Long.MAX_VALUE);
        assertNull(validator.validate("thirdLongFiled", to));
        to.setThirdLongField(Long.MIN_VALUE);
        assertNull(validator.validate("thirdLongFiled", to));

        assertNotNull(validator.validate("numberString", to));
        to.setNumberString("0.5");
        assertNotNull(validator.validate("numberString", to));
        to.setNumberString(".5");
        assertNotNull(validator.validate("numberString", to));
        to.setNumberString("1");
        assertNull(validator.validate("numberString", to));
    }

    /* TODO: Implement the multi-value fields */
    @Test
    public void testMultiValueField() {
        TestObject to = new TestObject();
        to.setStringField("ZZZ");
        to.setCompoundField("something");
        assertNull(validator.validate("compoundField", to));
        to.setStringField("XXX");
        assertNull(validator.validate("compoundField", to));
        to.setStringField("INVALID");
        assertNull(validator.validate("compoundField", to));
        to.setCompoundField(null);
        assertNull(validator.validate("compoundField", to));
        to.setStringField("XXX");
        assertNotNull(validator.validate("compoundField", to));
        // Check that the length constraints work too
        to.setCompoundField("somethingmorethan20characterslong");
        assertNotNull(validator.validate("compoundField", to));
    }

    @Test
    public void testRequiredIfConstraint() {
        TestObject to = new TestObject();
        //init both to empty strings
        to.setStringField("");
        to.setSecondStringField("");

        // Make sure that when both are null/empty, everything is ok
        assertNull(validator.validate("secondStringField", to));

        // If the secondStringField is required if stringField is not null
        to.setStringField("foo");
        assertNotNull(validator.validate("secondStringField", to));

        // Set both to something and it should be valid
        to.setSecondStringField("bar");
        assertNull(validator.validate("secondStringField", to));

        // Since stringField isn't ZZZ or XXX this should
        // be OK
        assertNull(validator.validate("secondLongField", to));

        to.setStringField("ZZZ");
        // Now it should fail
        assertNotNull(validator.validate("secondLongField", to));
    }
}


