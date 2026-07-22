/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

class SchemaParserTest {

    @Test
    @DisplayName("Parse TestObject schema and expose all named constraints")
    void parseTestObjectSchemaAndExposeAllNamedConstraints() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("TestObject.xsd"));

        Map<String, Constraint> constraints = parser.getConstraints();

        assertEquals(12, constraints.size());
        assertNotNull(parser.getConstraint("longField"));
        assertNotNull(parser.getConstraint("stringField"));
        assertNotNull(parser.getConstraint("compoundField"));
        // Non existing constraint
        assertNull(parser.getConstraint("doesNotExist"));
    }

    @Test
    @DisplayName("Parse long and string bounds from schema")
    void parseLongAndStringBoundsFromSchema() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("TestObject.xsd"));

        var longField = (LongConstraint) parser.getConstraint("longField");
        assertNotNull(longField);
        assertEquals(Long.valueOf(0L), longField.getMinInclusive());
        assertEquals(Long.valueOf(20L), longField.getMaxInclusive());
        assertEquals(Boolean.FALSE, longField.getOptional());
        assertEquals(DataConverter.getInstance().getJavaType("long"), longField.getDataType());

        var thirdLongField = (LongConstraint) parser.getConstraint("thirdLongField");
        assertNotNull(thirdLongField);
        assertEquals(Long.valueOf(Long.MIN_VALUE), thirdLongField.getMinInclusive());
        assertEquals(Long.valueOf(Long.MAX_VALUE), thirdLongField.getMaxInclusive());

        var stringField = (StringConstraint) parser.getConstraint("stringField");
        assertNotNull(stringField);
        assertEquals(Double.valueOf(0), stringField.getMinLength());
        assertEquals(Double.valueOf(20), stringField.getMaxLength());
        assertEquals(Boolean.FALSE, stringField.getOptional());
        assertEquals(DataConverter.getInstance().getJavaType("string"), stringField.getDataType());
    }

    @Test
    @DisplayName("Parse ascii username and posix flags")
    void parseAsciiUsernameAndPosixFlags() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("TestObject.xsd"));

        var asciiConstraint = (StringConstraint) parser.getConstraint("asciiString");
        assertNotNull(asciiConstraint);
        assertTrue(asciiConstraint.getASCII());

        var usernameConstraint = (StringConstraint) parser.getConstraint("usernameString");
        assertNotNull(usernameConstraint);
        assertTrue(usernameConstraint.getUserName());

        var posixConstraint = (StringConstraint) parser.getConstraint("posixString");
        assertNotNull(posixConstraint);
        assertTrue(posixConstraint.getPosix());
    }

    @Test
    @DisplayName("Parse requiredIf rules and apply them")
    void parseRequiredIfRulesAndApplyThem() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("TestObject.xsd"));
        TestObject testObject = new TestObject();

        var compoundField = (RequiredIfConstraint) parser.getConstraint("compoundField");
        testObject.setStringField("ZZZ");
        assertTrue(compoundField.isRequired(null, testObject));
        testObject.setStringField("XXX");
        assertTrue(compoundField.isRequired(null, testObject));
        testObject.setStringField("NO_MATCH");
        assertFalse(compoundField.isRequired(null, testObject));

        var secondStringField = (RequiredIfConstraint) parser.getConstraint("secondStringField");
        testObject.setStringField("");
        assertFalse(secondStringField.isRequired(null, testObject));
        testObject.setStringField("foo");
        assertTrue(secondStringField.isRequired(null, testObject));
    }

    @Test
    @DisplayName("Parse optional and regex attributes")
    void parseOptionalAndRegexAttributes() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("SchemaParser_optional_regex.xsd"));

        var regexField = (StringConstraint) parser.getConstraint("regexField");
        assertEquals(Boolean.TRUE, regexField.getOptional());
        assertEquals("^[A-Z]{3}$", regexField.getRegEx());

        var implicitOptional = (StringConstraint) parser.getConstraint("implicitOptional");
        assertEquals(Boolean.TRUE, implicitOptional.getOptional());
    }

    @Test
    @DisplayName("Parse unsupported base type as ParsedConstraint")
    void parseUnsupportedBaseTypeAsParsedConstraint() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("TestObject.xsd"));

        Constraint dateField = parser.getConstraint("dateField");
        assertNotNull(dateField);
        assertTrue(dateField instanceof ParsedConstraint);
        assertEquals("dateField", dateField.getIdentifier());
        assertEquals(DataConverter.getInstance().getJavaType("date"), dateField.getDataType());

        ParsedConstraint parsedDateField = (ParsedConstraint) dateField;
        assertEquals(Boolean.FALSE, parsedDateField.getOptional());
    }

    @Test
    @DisplayName("Use default min and max for long int double and float when bounds are not defined")
    void useDefaultMinAndMaxForLongIntDoubleAndFloatWhenBoundsAreNotDefined() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("SchemaParser_int_float_defaults.xsd"));

        var longConstraint = (LongConstraint) parser.getConstraint("longDefaultsField");
        assertNotNull(longConstraint);
        assertEquals(Long.valueOf(Long.MIN_VALUE), longConstraint.getMinInclusive());
        assertEquals(Long.valueOf(Long.MAX_VALUE), longConstraint.getMaxInclusive());

        var intConstraint = (LongConstraint) parser.getConstraint("intDefaultsField");
        assertNotNull(intConstraint);
        assertEquals(Long.valueOf(Integer.MIN_VALUE), intConstraint.getMinInclusive());
        assertEquals(Long.valueOf(Integer.MAX_VALUE), intConstraint.getMaxInclusive());

        var doubleConstraint = (DoubleConstraint) parser.getConstraint("doubleDefaultsField");
        assertNotNull(doubleConstraint);
        assertEquals(Double.valueOf(Double.MIN_VALUE), doubleConstraint.getMinInclusive());
        assertEquals(Double.valueOf(Double.MAX_VALUE), doubleConstraint.getMaxInclusive());

        var floatConstraint = (DoubleConstraint) parser.getConstraint("floatDefaultsField");
        assertNotNull(floatConstraint);
        assertEquals(Double.valueOf((double) Float.MIN_VALUE), floatConstraint.getMinInclusive());
        assertEquals(Double.valueOf((double) Float.MAX_VALUE), floatConstraint.getMaxInclusive());
    }

    @Test
    @DisplayName("Ignore attributes without simpleType and parse explicit floating bounds")
    void ignoreAttributeWithoutSimpleTypeAndParseExplicitFloatingBounds() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("SchemaParser_additional_coverage.xsd"));

        assertNull(parser.getConstraint("ignoredNoSimpleType"));

        var doubleBounds = (DoubleConstraint) parser.getConstraint("doubleExplicitBoundsField");
        assertNotNull(doubleBounds);
        assertEquals(Double.valueOf(-1.5), doubleBounds.getMinInclusive());
        assertEquals(Double.valueOf(42.25), doubleBounds.getMaxInclusive());

        var floatBounds = (DoubleConstraint) parser.getConstraint("floatExplicitBoundsField");
        assertNotNull(floatBounds);
        assertEquals(Double.valueOf(1.25), floatBounds.getMinInclusive());
        assertEquals(Double.valueOf(99.5), floatBounds.getMaxInclusive());
    }

    @Test
    @DisplayName("Parse explicit optional false and treat illegal optional value as false")
    void parseExplicitOptionalFalseAndIllegalOptionalValue() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("SchemaParser_additional_coverage.xsd"));

        var explicitFalse = (StringConstraint) parser.getConstraint("explicitOptionalFalse");
        assertNotNull(explicitFalse);
        assertEquals(Boolean.FALSE, explicitFalse.getOptional());

        var illegalOptionalValue = (StringConstraint) parser.getConstraint("illegalOptionalValue");
        assertNotNull(illegalOptionalValue);
        assertEquals(Boolean.FALSE, illegalOptionalValue.getOptional());
    }

    @Test
    @DisplayName("Throw when minInclusive is not a valid number")
    void throwWhenMinInclusiveIsNotValidNumber() throws Exception {
        URL schemaUrl = TestUtils.findTestData("SchemaParser_invalid_min_number.xsd");
        assertThrows(NumberFormatException.class, () -> new SchemaParser(schemaUrl));
    }

    @Test
    @DisplayName("Throw when maxInclusive is not a valid number")
    void throwWhenMaxInclusiveIsNotValidNumber() throws Exception {
        URL schemaUrl = TestUtils.findTestData("SchemaParser_invalid_max_number.xsd");
        assertThrows(NumberFormatException.class, () -> new SchemaParser(schemaUrl));
    }

    @Test
    @DisplayName("Throw when attribute name is missing")
    void throwWhenAttributeNameIsMissing() throws Exception {
        URL schemaUrl = TestUtils.findTestData("SchemaParser_missing_attribute_name.xsd");
        IOException ex = assertThrows(IOException.class, () -> new SchemaParser(schemaUrl));
        assertTrue(ex.getMessage().contains("must have names"));
    }

    @Test
    @DisplayName("Throw when baseType is missing")
    void throwWhenBaseTypeIsMissing() throws Exception {
        URL schemaUrl = TestUtils.findTestData("SchemaParser_missing_base_type.xsd");
        IOException ex = assertThrows(IOException.class, () -> new SchemaParser(schemaUrl));
        assertTrue(ex.getMessage().contains("No data type specified"));
    }

    @Test
    @DisplayName("Throw when schema XML is malformed")
    void throwWhenSchemaXmlIsMalformed() throws Exception {
        URL schemaUrl = TestUtils.findTestData("SchemaParser_malformed.xsd");
        assertThrows(IOException.class, () -> new SchemaParser(schemaUrl));
    }

    @Test
    @DisplayName("Ignore attributes in non-schema namespace")
    void ignoreAttributesInNonSchemaNamespace() throws Exception {
        SchemaParser parser = new SchemaParser(TestUtils.findTestData("SchemaParser_wrong_namespace.xsd"));
        assertTrue(parser.getConstraints().isEmpty());
    }
}
