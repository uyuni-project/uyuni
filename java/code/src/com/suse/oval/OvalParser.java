/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval;

import com.suse.oval.exceptions.OvalParserException;
import com.suse.oval.ovaltypes.ArchType;
import com.suse.oval.ovaltypes.EVRType;
import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.OperationEnumeration;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;
import com.suse.oval.ovaltypes.VersionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * The Oval Parser is responsible for parsing OVAL(Open Vulnerability and Assessment Language) documents
 */
public class OvalParser {
    private static final Logger LOG = LogManager.getLogger(OvalParser.class);

    /**
     * Parse the given OVAL file
     *
     * @param ovalFile the OVAL file to parse
     * @return the parsed OVAL encapulated in a {@link OvalRootType} object=
     * */
    public OvalRootType parse(File ovalFile) throws OvalParserException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(OvalRootType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (OvalRootType) unmarshaller.unmarshal(ovalFile);
        }
        catch (JAXBException e) {
            throw new OvalParserException("Failed to parse the given OVAL file at: " + ovalFile.getAbsolutePath(), e);
        }
    }

    /**
     * Parse the given OVAL file from a URL
     *
     * @param url the URL to get the OVAL file from
     * @return the parsed OVAL encapsulated in a {@link OvalRootType} object
     * */
    public OvalRootType parse(URL url) {
        try {
            return parse(new File(url.toURI()));
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public OvalRootType parseStax(File ovalFile) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(ovalFile));

            OvalRootType ovalRoot = new OvalRootType();

            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();

                if (nextEvent.isStartElement()) {
                    String elementName = nextEvent.asStartElement().getName().getLocalPart();
                    if (elementName.equals("objects")) {
                        ovalRoot.setObjects(parseObjects(reader));
                    }
                    else if (elementName.equals("states")) {
                        ovalRoot.setStates(parseStates(reader));
                    }
                    else if (elementName.equals("tests")) {
                        ovalRoot.setTests(parseTests(reader));
                    }
                }
            }

            return ovalRoot;

        }
        catch (XMLStreamException | FileNotFoundException e) {
            throw new OvalParserException("Failed to parse the given OVAL file at: " + ovalFile.getAbsolutePath(), e);
        }
    }

    private List<TestType> parseTests(XMLEventReader reader) throws XMLStreamException {
        List<TestType> tests = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("rpminfo_test")) {
                    TestType testType = parseTestType(nextEvent.asStartElement(), reader);
                    tests.add(testType);
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("tests")) {
                    return tests;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </tests>");
    }

    private TestType parseTestType(StartElement testElement, XMLEventReader reader) throws XMLStreamException {
        TestType testType = new TestType();

        testElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            switch (attributeName) {
                case "id":
                    testType.setId(attribute.getValue());
                    break;
                case "comment":
                    testType.setComment(attribute.getValue());
                    break;
            }
        });

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("object")) {
                    Attribute objectRefAttribute = nextEvent.asStartElement().getAttributeByName(
                            new QName("object_ref"));
                    if (objectRefAttribute != null) {
                        testType.setObjectRef(objectRefAttribute.getValue());
                    } else {
                        LOG.warn("objectRef property was not found");
                    }
                } else if (nextEvent.asStartElement().getName().getLocalPart().equals("state")) {
                    Attribute stateRefAttribute = nextEvent.asStartElement().getAttributeByName(
                            new QName("state_ref"));
                    if (stateRefAttribute != null) {
                        testType.setStateRef(stateRefAttribute.getValue());
                    }
                    else {
                        LOG.warn("stateRef property was not found");
                    }
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("rpminfo_test")) {
                    return testType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </rpminfo_test>");
    }

    private List<StateType> parseStates(XMLEventReader reader) throws XMLStreamException {
        List<StateType> states = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("rpminfo_state")) {
                    StateType stateType = parseStateType(nextEvent.asStartElement(), reader);
                    states.add(stateType);
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("states")) {
                    return states;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </states>");
    }

    private StateType parseStateType(StartElement rpmStateElement, XMLEventReader reader) throws XMLStreamException {
        StateType stateType = new StateType();

        rpmStateElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            switch (attributeName) {
                case "id":
                    stateType.setId(attribute.getValue());
                    break;
                case "comment":
                    stateType.setComment(attribute.getValue());
                    break;
                default:
                    break;
            }
        });

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                String elementName = nextEvent.asStartElement().getName().getLocalPart();
                if (elementName.equals("arch")) {
                    stateType.setPackageArch(parseArchStateEntity(nextEvent.asStartElement(), reader));
                }
                else if (elementName.equals("evr")) {
                    stateType.setPackageEVR(parseEVRStateEntity(nextEvent.asStartElement(), reader));
                }
                else if (elementName.equals("version")) {
                    stateType.setPackageVersion(parseVersionStateEntity(nextEvent.asStartElement(), reader));
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("rpminfo_state")) {
                    return stateType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </rpminfo_state>");
    }

    private ArchType parseArchStateEntity(StartElement archElement, XMLEventReader reader) throws XMLStreamException {
        ArchType archType = new ArchType();

        archElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            if (attributeName.equals("operation")) {
                archType.setOperation(OperationEnumeration.fromValue(attribute.getValue()));
            }
        });

        archType.setValue(reader.getElementText());

        return archType;
    }

    private EVRType parseEVRStateEntity(StartElement evrElement, XMLEventReader reader) throws XMLStreamException {
        EVRType evrType = new EVRType();

        evrElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            if (attributeName.equals("operation")) {
                evrType.setOperation(OperationEnumeration.fromValue(attribute.getValue()));
            }
        });

        evrType.setValue(reader.getElementText());

        return evrType;
    }

    private VersionType parseVersionStateEntity(StartElement versionElement, XMLEventReader reader)
            throws XMLStreamException {
        VersionType versionType = new VersionType();

        versionElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            if (attributeName.equals("operation")) {
                versionType.setOperation(OperationEnumeration.fromValue(attribute.getValue()));
            }
        });

        versionType.setValue(reader.getElementText());

        return versionType;
    }

    private List<ObjectType> parseObjects(XMLEventReader reader) throws XMLStreamException {
        List<ObjectType> objects = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("rpminfo_object")) {
                    ObjectType objectType = parseObjectType(nextEvent.asStartElement(), reader);
                    objects.add(objectType);
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("objects")) {
                    return objects;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </objects>");
    }

    private ObjectType parseObjectType(StartElement rpmObjectElement, XMLEventReader reader) throws XMLStreamException {
        ObjectType objectType = new ObjectType();

        rpmObjectElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            switch (attributeName) {
                case "id":
                    objectType.setId(attribute.getValue());
                    break;
                case "comment":
                    objectType.setComment(attribute.getValue());
                    break;
                default:
                    break;
            }
        });


        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("name")) {
                    objectType.setPackageName(reader.getElementText());
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("rpminfo_object")) {
                    return objectType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </rpminfo_object>");
    }

}
