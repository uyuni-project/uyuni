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
package com.redhat.rhn.common.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

class OvalFileAggregatorTest {

    @Test
    @DisplayName("Return empty string for empty aggregator")
    void returnEmptyStringForEmptyAggregator() throws IOException {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        assertEquals("", aggregator.finish(false));
    }

    @Test
    @DisplayName("Aggregate single file with generator metadata")
    void aggregateSingleFileWithGeneratorMetadata() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml"));

        String output = aggregator.finish(false);

        assertTrue(output.contains("<oval_definitions"));
        assertTrue(output.contains("Spacewalk"));
        assertTrue(output.contains("5.0"));

        Document doc = parseXml(output);
        assertEquals("oval_definitions", doc.getDocumentElement().getTagName());

        Element definitions = firstElementByTagName(doc, "definitions");
        assertEquals(1, directChildElementCount(definitions));
    }

    @Test
    @DisplayName("Handle namespaces correctly with proper URI declarations")
    void handleNamespacesCorrectlyWithProperUriDeclarations() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml"));
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-redhat-prefix.xml"));

        String output = aggregator.finish(false);

        // The root element should declare proper namespace URIs (not placeholder values).
        assertTrue(output.contains("xmlns=\"http://oval.mitre.org/XMLSchema/oval-definitions-5\""));
        assertTrue(output.contains("xmlns:oval=\"http://oval.mitre.org/XMLSchema/oval-common-5\""));
        assertTrue(output.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""));

        // Prefixed elements are correctly serialized with their namespace URIs.
        assertTrue(output.contains("<oval:timestamp>"));
        assertTrue(output.contains("<oval:product_name>"));
        assertTrue(output.contains("<redhat:reference"));

        // The schema location attribute must be present and properly computed.
        assertTrue(output.contains("xsi:schemaLocation="));
        assertTrue(output.contains("oval-common-schema.xsd"));
        assertTrue(output.contains("oval-definitions-schema.xsd"));
        assertTrue(output.contains("linux-definitions-schema.xsd"));
    }

    @Test
    @DisplayName("Ignore null file in add")
    void ignoreNullFileInAdd() throws IOException {
        OvalFileAggregator aggregator = new OvalFileAggregator();

        assertDoesNotThrow(() -> aggregator.add((File) null));
        assertEquals("", aggregator.finish(false));
    }

    @Test
    @DisplayName("Throw when adding after finish")
    void throwWhenAddingAfterFinish() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        File oval1 = testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml");

        aggregator.add(oval1);
        aggregator.finish(false);

        assertThrows(IllegalStateException.class, () -> aggregator.add(oval1));
    }

    @Test
    @DisplayName("Deduplicate overlapping IDs across files")
    void deduplicateOverlappingIdsAcrossFiles() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-2.xml"));
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-3.xml"));

        String output = aggregator.finish(false);
        Document doc = parseXml(output);

        Element definitions = firstElementByTagName(doc, "definitions");
        Element tests = firstElementByTagName(doc, "tests");
        Element objects = firstElementByTagName(doc, "objects");
        Element states = firstElementByTagName(doc, "states");

        assertEquals(1, directChildElementCount(definitions));
        assertEquals(3, directChildElementCount(tests));
        assertEquals(3, directChildElementCount(objects));
        assertEquals(2, directChildElementCount(states));
    }

    @Test
    @DisplayName("Keep first duplicate id from the same file")
    void keepFirstDuplicateIdFromTheSameFile() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-duplicate-id-same-file.xml"));

        String output = aggregator.finish(false);
        Document doc = parseXml(output);
        Element definitions = firstElementByTagName(doc, "definitions");

        assertEquals(1, directChildElementCount(definitions));
        assertTrue(output.contains("first-definition"));
        assertFalse(output.contains("second-definition"));
    }

    @Test
    @DisplayName("Return identical output when finish is called repeatedly")
    void returnIdenticalOutputWhenFinishIsCalledRepeatedly() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml"));

        String first = aggregator.finish(false);
        String second = aggregator.finish(false);

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Treat objects-only input as empty in current implementation")
    void treatObjectsOnlyInputAsEmptyInCurrentImplementation() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-objects-only.xml"));

        // Current isEmpty() logic ignores objects and therefore returns empty output.
        assertEquals("", aggregator.finish(false));
    }

    @Test
    @DisplayName("Throw on malformed XML in add(File)")
    void throwOnMalformedXmlInAddFile() {
        OvalFileAggregator aggregator = new OvalFileAggregator();

        assertThrows(IOException.class,
            () -> aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-malformed.xml")));
    }

    @Test
    @DisplayName("Add missing sections as empty containers in output")
    void addMissingSectionsAsEmptyContainersInOutput() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-missing-sections.xml"));

        String output = aggregator.finish(false);
        Document doc = parseXml(output);

        Element definitions = firstElementByTagName(doc, "definitions");
        Element tests = firstElementByTagName(doc, "tests");
        Element objects = firstElementByTagName(doc, "objects");
        Element states = firstElementByTagName(doc, "states");

        assertEquals(1, directChildElementCount(definitions));
        assertEquals(0, directChildElementCount(tests));
        assertEquals(0, directChildElementCount(objects));
        assertEquals(0, directChildElementCount(states));
    }

    @Test
    @DisplayName("Ignore section children without id attribute")
    void ignoreSectionChildrenWithoutIdAttribute() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-missing-id.xml"));

        String output = aggregator.finish(false);
        Document doc = parseXml(output);

        Element definitions = firstElementByTagName(doc, "definitions");
        Element tests = firstElementByTagName(doc, "tests");
        Element objects = firstElementByTagName(doc, "objects");
        Element states = firstElementByTagName(doc, "states");

        assertEquals(1, directChildElementCount(definitions));
        assertEquals(1, directChildElementCount(tests));
        assertEquals(1, directChildElementCount(objects));
        assertEquals(1, directChildElementCount(states));
    }

    @Test
    @DisplayName("Keep empty and whitespace ids but deduplicate repeated keys in compact output")
    void keepEmptyAndWhitespaceIdsButDeduplicateRepeatedKeysInCompactOutput() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-empty-whitespace-id.xml"));

        String output = aggregator.finish(false);

        // Current behavior keeps empty and whitespace ids as valid dedup keys.
        assertTrue(output.contains("id=\"\""));
        assertTrue(output.contains("id=\"   \""));
        assertTrue(output.contains("empty-1"));
        assertFalse(output.contains("empty-2"));
        assertTrue(output.contains("space-1"));
        assertFalse(output.contains("space-2"));

        // Compact output should not introduce separator whitespace between
        // aggregated sibling nodes/sections.
        assertFalse(output.contains("</definition>\n<definition"));
        assertFalse(output.contains("</definition>\r\n<definition"));
        assertFalse(output.contains("</definition> <definition"));
        assertFalse(output.contains("</definitions>\n<tests"));
        assertFalse(output.contains("</definitions>\r\n<tests"));
        assertFalse(output.contains("</definitions> <tests"));

        // Ensure sibling definitions and sections are serialized adjacently.
        assertTrue(output.contains("</definition><definition"));
        assertTrue(output.contains("</definitions><tests"));
    }

    @Test
    @DisplayName("Do not resolve external DTD or schema in non-validating add(File)")
    void doNotResolveExternalDtdOrSchemaInNonValidatingAddFile() throws Exception {
        OvalFileAggregator aggregator = new OvalFileAggregator();

        var ioException = assertThrows(IOException.class,
            () -> aggregator.add(testFile("/com/redhat/rhn/common/util/oval-def-external-refs.xml")));

        assertTrue(ioException.getMessage().startsWith("Unable to parse xml document"));
        var saxException = assertInstanceOf(SAXParseException.class, ioException.getCause());
        assertTrue(saxException.getMessage().contains("DOCTYPE is disallowed when the feature " +
            "\"http://apache.org/xml/features/disallow-doctype-decl\" set to true."));
    }

    @Test
    @DisplayName("Use injected date to build deterministic generator timestamp")
    void useInjectedDateForGeneratorTimestamp() throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2026, Calendar.JULY, 22, 3, 4, 5);
        Date fixedDate = calendar.getTime();

        OvalFileAggregator aggregator = new OvalFileAggregator(fixedDate);
        aggregator.add(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml"));

        String output = aggregator.finish(false);
        Document doc = parseXml(output);

        Element timestamp = firstElementByTagName(doc, "oval:timestamp");
        assertEquals("2026-07-22T03:04:05", timestamp.getTextContent());
    }

    @Test
    @DisplayName("Clone source nodes so post-add mutations do not leak")
    void cloneSourceNodesSoPostAddMutationsDoNotLeak() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        Document source;
        try (var fileStream = new FileInputStream(testFile("/com/redhat/rhn/manager/audit/oval/oval-def-1.xml"))) {
            source = factory.newDocumentBuilder().parse(new InputSource(fileStream));
        }

        OvalFileAggregator aggregator = new OvalFileAggregator();
        aggregator.add(source);

        // Mutate the source after add(); aggregated output must keep the original id.
        NodeList definitions = source.getDocumentElement().getElementsByTagName("definition");
        Element firstDefinition = (Element) definitions.item(0);
        firstDefinition.setAttribute("id", "oval:test:def:mutated");

        String output = aggregator.finish(false);

        assertTrue(output.contains("oval:org.opensuse.security:def:20222991"));
        assertFalse(output.contains("oval:test:def:mutated"));
    }

    private static File testFile(String path) throws Exception {
        return new File(TestUtils.findTestData(path).getFile());
    }

    private static Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Element firstElementByTagName(Document doc, String tagName) {
        NodeList nodes = doc.getElementsByTagName(tagName);
        return (Element) nodes.item(0);
    }

    private static int directChildElementCount(Element parent) {
        int count = 0;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }
}
