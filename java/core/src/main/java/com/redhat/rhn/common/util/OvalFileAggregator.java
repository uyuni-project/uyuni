/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Builds a single OVAL XML file out of individual OVAL files
 *
 */
public final class OvalFileAggregator {

    private static final Logger LOGGER = LogManager.getLogger(OvalFileAggregator.class);

    private static final String XMLNS = XMLConstants.XMLNS_ATTRIBUTE;

    private static final String XMLNS_NS = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

    private static final String XSI_NS = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;

    private static final String OVAL_DEFINITION_NS = "http://oval.mitre.org/XMLSchema/oval-definitions-5";

    private static final String OVAL_COMMON_NS = "http://oval.mitre.org/XMLSchema/oval-common-5";

    private static final Set<LocationMapping> SCHEMA_LOCATION_MAPPING = Set.of(
        new LocationMapping(
            "http://oval.mitre.org/XMLSchema/oval-definitions-5#",
            "-definitions-schema.xsd"
        ),
        new LocationMapping(
            "http://oval.mitre.org/XMLSchema/oval-system-characteristics-5#",
            "-system-characteristics-schema.xsd"
        )
    );

    private static final ThreadLocal<DocumentBuilderFactory> DOCUMENT_BUILDER_FACTORY = ThreadLocal.withInitial(
        OvalFileAggregator::getDocumentBuilderFactory);

    private final Document aggregate;

    private final Map<String, Element> defs;
    private final Map<String, Element> tests;
    private final Map<String, Element> objects;
    private final Map<String, Element> states;

    private boolean isFinished;

    /**
     * No-arg constructor
     */
    public OvalFileAggregator() {
        this(null);
    }

    /**
     * Constructor for unit tests
     * @param timestamp
     */
    OvalFileAggregator(Date timestamp) {
        try {
            aggregate = DOCUMENT_BUILDER_FACTORY.get().newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Unable to create the aggregated document", ex);
        }

        defs = new LinkedHashMap<>();
        tests = new LinkedHashMap<>();
        objects = new LinkedHashMap<>();
        states = new LinkedHashMap<>();

        reset(timestamp);
    }

    /**
     * Adds a OVAL file to the aggregate
     * @param f file to add
     * @throws IOException file IO failed
     */
    public void add(File f) throws IOException {
        if (f == null) {
            return;
        }

        try (InputStream fileStream = new FileInputStream(f)) {
            var documentBuilder = DOCUMENT_BUILDER_FACTORY.get().newDocumentBuilder();
            add(documentBuilder.parse(new InputSource(fileStream)));
        }
        catch (SAXException ex) {
            throw new IOException("Unable to parse xml document " + f.getAbsolutePath(), ex);
        }
        catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Unable to create DocumentBuilder", ex);
        }
    }

    /**
     * Adds a parsed OVAL file to the aggregate
     * @param doc parsed OVAL file
     */
    public void add(Document doc) {
        if (isFinished) {
            throw new IllegalStateException("Cannot add document: aggregation already finished");
        }

        storeDefinitions(doc);
        storeTests(doc);
        storeObjects(doc);
        storeStates(doc);
    }

    /**
     * Finalizes processing and builds the aggregated document
     * @param prettyPrint pretty print XML or not
     * @return XML in string form
     * @throws IOException document output failed
     */
    public String finish(boolean prettyPrint) throws IOException {
        if (!isFinished && !isEmpty()) {
            buildDocument();
            isFinished = true;
        }
        if (isEmpty()) {
            return "";
        }

        try (StringWriter writer = new StringWriter()) {
            var transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");

            removeWhitespaces();

            if (prettyPrint) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            }
            else {
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
            }

            transformer.transform(new DOMSource(aggregate), new StreamResult(writer));
            return writer.toString();
        }
        catch (TransformerException ex) {
            throw new IOException("Unable to convert aggregate document to xml", ex);
        }
    }

    private void buildDocument() {
        Element defsElement = aggregate.createElement("definitions");
        attachChildren(defsElement, defs);

        Element testsElement = aggregate.createElement("tests");
        attachChildren(testsElement, tests);

        Element objectsElement = aggregate.createElement("objects");
        attachChildren(objectsElement, objects);

        Element statesElement = aggregate.createElement("states");
        attachChildren(statesElement, states);

        Element root = aggregate.getDocumentElement();
        root.appendChild(defsElement);
        root.appendChild(testsElement);
        root.appendChild(objectsElement);
        root.appendChild(statesElement);

        // Computing the schemaLocation attribute
        root.setAttributeNS(XSI_NS, "xsi:schemaLocation", computeSchemaLocation(root));
    }

    private boolean isEmpty() {
        return defs.isEmpty() && tests.isEmpty() && states.isEmpty();
    }

    private void attachChildren(Element parent, Map<String, Element> children) {
        children.values().forEach(parent::appendChild);
    }

    private void storeStates(Document doc) {
        storeChildren("states", doc, states);
    }

    private void storeObjects(Document doc) {
        storeChildren("objects", doc, objects);
    }

    private void storeTests(Document doc) {
        storeChildren("tests", doc, tests);
    }

    private void storeDefinitions(Document doc) {
        storeChildren("definitions", doc, defs);
    }

    private void storeChildren(String childTagName, Document doc, Map<String, Element> container) {
        elementStream(doc.getDocumentElement())
            .filter(element -> childTagName.equals(element.getTagName()))
            .flatMap(element -> elementStream(element))
            .forEach(element -> {
                String key = element.hasAttribute("id") ? element.getAttribute("id") : null;
                if (key == null) {
                    return;
                }

                // Import the node into the 'aggregate' document.
                container.computeIfAbsent(key, k -> (Element) aggregate.importNode(element, true));
            });
    }

    private void reset(Date timestampDate) {
        Element root = aggregate.createElementNS(OVAL_DEFINITION_NS, "oval_definitions");
        root.setAttribute(XMLNS, OVAL_DEFINITION_NS);
        root.setAttributeNS(XMLNS_NS, "xmlns:oval", OVAL_COMMON_NS);
        root.setAttributeNS(XMLNS_NS, "xmlns:xsi", XSI_NS);

        Element generator = aggregate.createElementNS(OVAL_DEFINITION_NS, "generator");

        Element prodName = aggregate.createElementNS(OVAL_COMMON_NS, "oval:product_name");
        prodName.setTextContent("Spacewalk");

        Element schemaVersion = aggregate.createElementNS(OVAL_COMMON_NS, "oval:schema_version");
        schemaVersion.setTextContent("5.0");

        Element timestamp = aggregate.createElementNS(OVAL_COMMON_NS, "oval:timestamp");
        Instant instant = timestampDate != null ? timestampDate.toInstant() : Instant.now();
        timestamp.setTextContent(
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Creating the DOM tree
        generator.appendChild(prodName);
        generator.appendChild(schemaVersion);
        generator.appendChild(timestamp);

        root.appendChild(generator);

        aggregate.appendChild(root);
    }

    private void removeWhitespaces() {
        try {
            XPathExpression allTextNodes = XPathFactory.newInstance().newXPath().compile("//text()");
            NodeList nodes = (NodeList) allTextNodes.evaluate(aggregate, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node textNode = nodes.item(i);
                String text = textNode.getNodeValue();

                if (text.trim().isEmpty()) {
                    // Remove nodes that are purely whitespace (fixes inter-tag formatting)
                    textNode.getParentNode().removeChild(textNode);
                }
                else {
                    // Normalize nodes containing actual text (for JDOM backwards compatibility)
                    textNode.setNodeValue(text.trim().replaceAll("\\s+", " "));
                }
            }
        }
        catch (XPathExpressionException ex) {
            // Should never happen
            LOGGER.debug("Unable to remove whitespaces from aggregated xml", ex);
        }
    }

    private static String computeSchemaLocation(Element root) {
        Set<String> activeNamespaces = new HashSet<>();
        collectNamespaces(root, activeNamespaces);

        StringBuilder schemaLocationBuilder = new StringBuilder();
        schemaLocationBuilder.append("http://oval.mitre.org/XMLSchema/oval-common-5 oval-common-schema.xsd");
        schemaLocationBuilder.append(" http://oval.mitre.org/XMLSchema/oval-definitions-5 oval-definitions-schema.xsd");

        for (String ns : activeNamespaces) {
            SCHEMA_LOCATION_MAPPING.stream()
                .filter(mapping -> mapping.matches(ns))
                .findFirst()
                .ifPresent(mapping -> schemaLocationBuilder.append(" " + ns + " " + mapping.schemaLocation(ns)));
        }

        return schemaLocationBuilder.toString();
    }

    private static void collectNamespaces(Node node, Set<String> namespaces) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            String ns = node.getNamespaceURI();
            if (StringUtils.isNotBlank(ns)) {
                namespaces.add(ns);
            }
        }

        // Recursively check all children
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            collectNamespaces(children.item(i), namespaces);
        }
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            return factory;
        }
        catch (ParserConfigurationException ex) {
            throw new IllegalStateException("Unable to build DocumentBuilderFactory", ex);
        }
    }

    private static Stream<Element> elementStream(Node parent) {
        NodeList nodeList = parent.getChildNodes();
        if (nodeList.getLength() == 0) {
            return Stream.of();
        }

        return IntStream.range(0, nodeList.getLength())
            .mapToObj(nodeList::item)
            .filter(Element.class::isInstance)
            .map(Element.class::cast);
    }

    private record LocationMapping(String namespacePrefix, String locationSuffix) {

        public boolean matches(String namespace) {
            return namespace != null && namespace.startsWith(namespacePrefix);
        }

        public String schemaLocation(String namespace) {
            String family = namespace.substring(namespacePrefix.length());
            return family + locationSuffix;
        }
    }
}
