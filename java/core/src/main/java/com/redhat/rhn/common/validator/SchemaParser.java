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
package com.redhat.rhn.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The {@code SchemaParser} parses an XML Schema and creates {@link Constraint} objects from it.
 */
public class SchemaParser {

    private static final Logger LOG = LogManager.getLogger(SchemaParser.class);

    private static final String SCHEMA_NAMESPACE_URI = "http://www.w3.org/1999/XMLSchema";

    private final URL schemaURL;

    private final Map<String, Constraint> constraints;

    /**
     * Default constructor
     *
     * @param schemaURLIn the <code>URL</code> of the schema to parse.
     * @throws IOException when parsing errors occur.
     */
    public SchemaParser(URL schemaURLIn) throws IOException {
        this.schemaURL = schemaURLIn;
        this.constraints = new LinkedHashMap<>();

        // Parse the schema and prepare constraints
        parseSchema();
    }

    /**
     * Retrieves all the constraints found within the document.
     *
     * @return the map of schema-defined constraints.
     */
    public Map<String, Constraint> getConstraints() {
        return constraints;
    }

    /**
     * Get the {@link Constraint} object for the specific constraint name.
     *
     * @param constraintName name of the constraint to look up.
     * @param constraintClazz the type of constraint
     * @param <T> the type of constraint
     * @return the {@link Constraint} matching the supplied name, or {@code null} if none is found.
     */
    public <T extends Constraint> T getConstraint(String constraintName, Class<T> constraintClazz) {
        Constraint o = constraints.get(constraintName);
        if (!constraintClazz.isInstance(o)) {
            return null;
        }

        return constraintClazz.cast(o);
    }

    private void parseSchema() throws IOException {
        try (InputStream inputStream = schemaURL.openStream()) {
            DocumentBuilder builder = getDocumentBuilderFactory().newDocumentBuilder();
            Document schemaDoc = builder.parse(inputStream);

            // Handle attributes
            childrenStream(schemaDoc.getDocumentElement(), SCHEMA_NAMESPACE_URI, "attribute")
                .map(attribute -> convertAttributeToConstraint(attribute))
                .flatMap(Optional::stream)
                .forEach(constraint -> {
                    // Store this constraint
                    LOG.debug("Adding: constraint name: {} datatype: {}", constraint.getIdentifier(),
                        constraint.getDataType());
                    constraints.put(constraint.getIdentifier(), constraint);
                });
        }
        catch (ParserConfigurationException | SAXException ex) {
            throw new IOException("Unable to parse schema " + schemaURL, ex);
        }
        catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private Optional<Constraint> convertAttributeToConstraint(Element attribute) {
        // Get the attribute name and create a Constraint
        String name = getElementAttribute(attribute, "name");
        if (name == null) {
            throw wrappedIOException("All schema attributes must have names.");
        }

        // Get the simpleType - if none, we are done with this attribute
        Element simpleType = getChildElement(attribute, SCHEMA_NAMESPACE_URI, "simpleType");
        if (simpleType == null) {
            return Optional.empty();
        }

        // Handle the data type
        String schemaType = getElementAttribute(simpleType, "baseType");
        if (schemaType == null) {
            throw wrappedIOException("No data type specified for constraint " + name);
        }

        return Optional.of(switch (schemaType) {
            case "long", "int" -> buildLongConstraint(name, simpleType, schemaType);
            case "double", "float" -> buildDoubleConstraint(name, simpleType, schemaType);
            case "string" -> buildStringConstraint(name, simpleType, schemaType);
            default -> buildParsedConstraint(name, schemaType);
        });
    }

    private LongConstraint buildLongConstraint(String name, Element simpleType, String schemaType) {
        LongConstraint constraint = new LongConstraint(name);
        constraint.setOptional(parseOptional(simpleType));
        constraint.setDataType(DataConverter.getInstance().getJavaType(schemaType));

        processRequiredIfConstraint(simpleType, constraint);
        // Handle ranges
        Element child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "minInclusive");
        if (child != null) {
            Long value = Long.valueOf(getElementAttribute(child, "value"));
            constraint.setMinInclusive(value);
        }
        else if (schemaType.equals("int")) {
            constraint.setMinInclusive((long) Integer.MIN_VALUE);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "maxInclusive");
        if (child != null) {
            Long value = Long.valueOf(getElementAttribute(child, "value"));
            constraint.setMaxInclusive(value);
        }
        else if (schemaType.equals("int")) {
            constraint.setMaxInclusive((long) Integer.MAX_VALUE);
        }

        return constraint;
    }

    private DoubleConstraint buildDoubleConstraint(String name, Element simpleType, String schemaType) {
        DoubleConstraint constraint = new DoubleConstraint(name);
        constraint.setOptional(parseOptional(simpleType));
        constraint.setDataType(DataConverter.getInstance().getJavaType(schemaType));

        processRequiredIfConstraint(simpleType, constraint);

        // Handle ranges
        Element child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "minInclusive");
        if (child != null) {
            Double value = Double.valueOf(getElementAttribute(child, "value"));
            constraint.setMinInclusive(value);
        }
        else if (schemaType.equals("float")) {
            Double value = (double) Float.MIN_VALUE;
            constraint.setMinInclusive(value);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "maxInclusive");
        if (child != null) {
            Double value = Double.valueOf(getElementAttribute(child, "value"));
            constraint.setMaxInclusive(value);
        }
        else if (schemaType.equals("float")) {
            Double value = (double) Float.MAX_VALUE;
            constraint.setMaxInclusive(value);
        }

        return constraint;
    }

    private StringConstraint buildStringConstraint(String name, Element simpleType, String schemaType) {
        StringConstraint constraint = new StringConstraint(name);
        constraint.setOptional(parseOptional(simpleType));
        constraint.setDataType(DataConverter.getInstance().getJavaType(schemaType));

        processRequiredIfConstraint(simpleType, constraint);

        Element child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "ascii");
        if (child != null) {
            constraint.setASCII(true);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "username");
        if (child != null) {
            constraint.setUserName(true);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "posix");
        if (child != null) {
            constraint.setPosix(true);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "maxLength");
        if (child != null) {
            Double value = Double.valueOf(getElementAttribute(child, "value"));
            constraint.setMaxLength(value);
        }
        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "minLength");
        if (child != null) {
            Double value = Double.valueOf(getElementAttribute(child, "value"));
            constraint.setMinLength(value);
        }

        child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "matchesExpression");
        if (child != null) {
            String value = getElementAttribute(child, "value");
            constraint.setRegEx(value);
        }

        return constraint;
    }

    private ParsedConstraint buildParsedConstraint(String name, String schemaType) {
        ParsedConstraint constraint = new ParsedConstraint(name);
        constraint.setDataType(DataConverter.getInstance().getJavaType(schemaType));

        return constraint;
    }

    private boolean parseOptional(Element simpleType) {
        boolean optional = false;
        Element child = getChildElement(simpleType, SCHEMA_NAMESPACE_URI, "optional");
        if (child != null) {
            String value = getElementAttribute(child, "value");
            if (value == null) {
                // <optional/> consider as true
                return true;
            }

            return Boolean.parseBoolean(value);
        }

        return optional;
    }

    private void processRequiredIfConstraint(Element simpleType, RequiredIfConstraint lc) {
        childrenStream(simpleType, SCHEMA_NAMESPACE_URI, "requiredIf")
            .forEach(requiredIf -> {
                String fieldName = getElementAttribute(requiredIf, "field");
                String fieldValue = getElementAttribute(requiredIf, "value");
                lc.addField(fieldName, fieldValue);
            });
    }

    private static DocumentBuilderFactory getDocumentBuilderFactory() throws ParserConfigurationException {
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

    private static Stream<Element> childrenStream(Element parent, String namespace, String childTagName) {
        NodeList nodeList = parent.getChildNodes();
        if (nodeList.getLength() == 0) {
            return Stream.of();
        }

        return IntStream.range(0, nodeList.getLength())
            .mapToObj(nodeList::item)
            // Extract Element only
            .filter(Element.class::isInstance)
            .map(Element.class::cast)
            // Filter by tagName and namespace
            .filter(elem -> childTagName.equals(elem.getLocalName()) && namespace.equals(elem.getNamespaceURI()));
    }

    private static Element getChildElement(Element parent, String namespace, String childTagName) {
        return childrenStream(parent, namespace, childTagName)
            .findFirst()
            .orElse(null);
    }

    private static String getElementAttribute(Element element, String attributeName) {
        return StringUtils.trimToNull(element.getAttribute(attributeName));
    }

    private static UncheckedIOException wrappedIOException(String message) {
        return new UncheckedIOException(new IOException(message));
    }

}
