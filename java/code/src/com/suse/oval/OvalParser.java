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
import com.suse.oval.ovaltypes.Advisory;
import com.suse.oval.ovaltypes.AdvisoryAffectedType;
import com.suse.oval.ovaltypes.AdvisoryCveType;
import com.suse.oval.ovaltypes.AdvisoryResolutionType;
import com.suse.oval.ovaltypes.ArchType;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.EVRType;
import com.suse.oval.ovaltypes.MetadataType;
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
                    else if (elementName.equals("definitions")) {
                        ovalRoot.setDefinitions(parseDefinitions(reader));
                    }
                }
            }

            return ovalRoot;

        }
        catch (XMLStreamException | FileNotFoundException e) {
            throw new OvalParserException("Failed to parse the given OVAL file at: " + ovalFile.getAbsolutePath(), e);
        }
    }

    private List<DefinitionType> parseDefinitions(XMLEventReader reader) throws XMLStreamException {
        List<DefinitionType> definitions = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("definition")) {
                    DefinitionType definitionType = parseDefinitionType(nextEvent.asStartElement(), reader);
                    definitions.add(definitionType);
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("definitions")) {
                    return definitions;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </definitions>");

    }

    private DefinitionType parseDefinitionType(StartElement definitionElement, XMLEventReader reader)
            throws XMLStreamException {
        DefinitionType definitionType = new DefinitionType();

        definitionElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();
            switch (attributeName) {
                case "id":
                    definitionType.setId(attribute.getValue());
                    break;
                case "class":
                    definitionType.setDefinitionClass(DefinitionClassEnum.fromValue(attribute.getValue()));
                    break;
                default:
                    break;
            }
        });

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("metadata")) {
                    definitionType.setMetadata(parseDefinitionMetadata(reader));
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("criteria")) {
                    definitionType.setCriteria(parseDefinitionCriteria(nextEvent.asStartElement(), reader));
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("definition")) {
                    return definitionType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </definition>");
    }

    private CriteriaType parseDefinitionCriteria(StartElement criteriaElement, XMLEventReader reader) {
        CriteriaType criteriaType = new CriteriaType();

        return criteriaType;
    }

    private MetadataType parseDefinitionMetadata(XMLEventReader reader) throws XMLStreamException {
        MetadataType metadataType = new MetadataType();
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("title")) {
                    metadataType.setTitle(reader.getElementText());
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("description")) {
                    metadataType.setDescription(reader.getElementText());
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("advisory")) {
                    metadataType.setAdvisory(parseAdvisory(reader));
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("metadata")) {
                    return metadataType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </metadata>");
    }

    private Advisory parseAdvisory(XMLEventReader reader) throws XMLStreamException {
        Advisory advisory = new Advisory();

        List<AdvisoryCveType> cveList = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("affected_cpe_list")) {
                    advisory.setAffectedCpeList(parseAffectedCpeList(reader));
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("cve")) {
                    cveList.add(parseAdvisoryCve(reader));
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("affected")) {
                    advisory.setAffected(parseAdvisoryAffectedType(reader));
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("advisory")) {
                    advisory.setCveList(cveList);
                    return advisory;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </advisory>");
    }

    private AdvisoryAffectedType parseAdvisoryAffectedType(XMLEventReader reader) throws XMLStreamException {
        AdvisoryAffectedType advisoryAffectedType = new AdvisoryAffectedType();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("resolution")) {
                    advisoryAffectedType.setResolution(parseAdvisoryResolutionType(nextEvent.asStartElement(), reader));
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("affected")) {
                    return advisoryAffectedType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </affected>");
    }

    private AdvisoryResolutionType parseAdvisoryResolutionType(StartElement resolutionElement, XMLEventReader reader)
            throws XMLStreamException {
        AdvisoryResolutionType advisoryResolutionType = new AdvisoryResolutionType();

        Attribute stateAttribute = resolutionElement.getAttributeByName(new QName("state"));
        if (stateAttribute != null) {
            advisoryResolutionType.setState(stateAttribute.getValue());
        }

        List<String> affectedComponents = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("component")) {
                    affectedComponents.add(reader.getElementText());
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("resolution")) {
                    advisoryResolutionType.setAffectedComponents(affectedComponents);
                    return advisoryResolutionType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </resolution>");
    }

    private AdvisoryCveType parseAdvisoryCve(XMLEventReader reader) throws XMLStreamException {
        AdvisoryCveType cveType = new AdvisoryCveType();

        cveType.setCve(reader.getElementText());

        return cveType;
    }

    private List<String> parseAffectedCpeList(XMLEventReader reader) throws XMLStreamException {
        List<String> cpes = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("cpe")) {
                    cpes.add(reader.getElementText());
                }
            }

            if (nextEvent.isEndElement()) {
                if (nextEvent.asEndElement().getName().getLocalPart().equals("affected_cpe_list")) {
                    return cpes;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </affected_cpe_list>");
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
