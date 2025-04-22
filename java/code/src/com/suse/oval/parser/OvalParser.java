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

package com.suse.oval.parser;

import com.redhat.rhn.domain.rhnpackage.PackageType;

import com.suse.oval.exceptions.OvalParserException;
import com.suse.oval.ovaltypes.Advisory;
import com.suse.oval.ovaltypes.AdvisoryAffectedType;
import com.suse.oval.ovaltypes.AdvisoryCveType;
import com.suse.oval.ovaltypes.AdvisoryResolutionType;
import com.suse.oval.ovaltypes.ArchType;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionClassEnum;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.EVRType;
import com.suse.oval.ovaltypes.LogicOperatorType;
import com.suse.oval.ovaltypes.MetadataType;
import com.suse.oval.ovaltypes.ObjectType;
import com.suse.oval.ovaltypes.OperationEnumeration;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.ovaltypes.StateType;
import com.suse.oval.ovaltypes.TestType;
import com.suse.oval.ovaltypes.VersionType;
import com.suse.oval.ovaltypes.linux.DpkginfoObject;
import com.suse.oval.ovaltypes.linux.DpkginfoState;
import com.suse.oval.ovaltypes.linux.DpkginfoTest;
import com.suse.oval.ovaltypes.linux.RpminfoObject;
import com.suse.oval.ovaltypes.linux.RpminfoState;
import com.suse.oval.ovaltypes.linux.RpminfoTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.evt.XMLEvent2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 * The Oval Parser is responsible for parsing OVAL(Open Vulnerability and Assessment Language) documents
 */
public class OvalParser {
    public static final int DEFINITIONS_BULK_SIZE = 500;

    private static final Logger LOG = LogManager.getLogger(OvalParser.class);
    private static final List<String> TEST_TYPES = List.of("rpminfo_test", "dpkginfo_test");
    private static final List<String> OBJECT_TYPES = List.of("rpminfo_object", "dpkginfo_object");
    private static final List<String> STATE_TYPES = List.of("rpminfo_state", "dpkginfo_state");

    /**
     * Parse the given OVAL file
     *
     * @param ovalFileURL the OVAL file to parse
     * @return the parsed OVAL encapsulated in an {@link OvalRootType} object.
     * */
    public OvalRootType parse(URL ovalFileURL) throws OvalParserException {
        File ovalFile;
        try {
            ovalFile = new File(ovalFileURL.toURI());
        }
        catch (URISyntaxException e) {
            throw new OvalParserException("Bad OVAL file path: " + ovalFileURL, e);
        }

        List<DefinitionType> allDefinitions = parseAllDefinitions(ovalFile);
        OVALResources ovalResources = parseResources(ovalFile);

        OvalRootType ovalRootType = new OvalRootType();
        ovalRootType.setDefinitions(allDefinitions);
        ovalRootType.setObjects(ovalResources.getObjects());
        ovalRootType.setStates(ovalResources.getStates());
        ovalRootType.setTests(ovalResources.getTests());

        return ovalRootType;
    }

    /**
     * Parses the given OVAL file in bulks. For every bulk parsed, it calls {@link OVALDefinitionsBulkHandler#handle}.
     *
     * @param ovalFile an XML file containing OVAL definitions to be parsed.
     * @param bulkHandler an operation to applied on every bulk of parsed OVAL definitions.
     * */
    public void parseDefinitionsInBulk(File ovalFile, OVALDefinitionsBulkHandler bulkHandler) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // Disable external entities processing as a protection measure against XXE.
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

        try {
            XMLEventReader2 reader =
                    (XMLEventReader2) xmlInputFactory.createXMLEventReader(new FileInputStream(ovalFile));
            // Disable external entities processing as a protection measure against XXE.
            xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

            List<DefinitionType> definitions = new ArrayList<>();

            while (reader.hasNext()) {
                XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

                if (nextEvent.isStartElement() &&
                        nextEvent.asStartElement().getName().getLocalPart().equals("definition")) {
                    DefinitionType definitionType = parseDefinitionType(nextEvent.asStartElement(), reader);
                    definitions.add(definitionType);

                    if (definitions.size() == DEFINITIONS_BULK_SIZE) {
                        bulkHandler.handle(definitions);
                        definitions = new ArrayList<>();
                    }
                }

                if (nextEvent.isEndElement() &&
                        nextEvent.asEndElement().getName().getLocalPart().equals("definitions")) {
                    if (!definitions.isEmpty()) {
                        bulkHandler.handle(definitions);
                    }
                    break;
                }
            }
        }
        catch (XMLStreamException | FileNotFoundException e) {
            throw new OvalParserException("Failed to parse OVAL definitions from OVAL file at: " +
                    ovalFile.getAbsolutePath(), e);
        }
    }
    /**
     * Utility method to parse all OVAL definitions at once instead of in bulks. To be used in testing.
     *
     * @param ovalFile an XML file containing OVAL definitions to be parsed.
     * @return all OVAL definitions in {@code ovalFile}
     * */
    public List<DefinitionType> parseAllDefinitions(File ovalFile) {
        List<DefinitionType> allDefinitions = new ArrayList<>();

        parseDefinitionsInBulk(ovalFile, allDefinitions::addAll);

        return allDefinitions;
    }
    /**
     * Parses the list of objects, states and tests from the given {@code ovalFile}.
     *
     * @param ovalFile an XML file containing OVAL definitions to be parsed.
     * @return an {@link OVALResources} object containing OVAL objects, states and tests.
     * */
    public OVALResources parseResources(File ovalFile) {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // Disable external entities processing as a protection measure against XXE.
        xmlInputFactory.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

        try {
            XMLEventReader2 reader =
                    (XMLEventReader2) xmlInputFactory.createXMLEventReader(new FileInputStream(ovalFile));

            OVALResources resources = new OVALResources();

            while (reader.hasNext()) {
                XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

                if (nextEvent.isStartElement()) {
                    String elementName = nextEvent.asStartElement().getName().getLocalPart();
                    switch (elementName) {
                        case "objects":
                            resources.setObjects(parseObjects(reader));
                            break;
                        case "states":
                            resources.setStates(parseStates(reader));
                            break;
                        case "tests":
                            resources.setTests(parseTests(reader));
                            break;
                        default: // Do nothing
                    }
                }
            }

            return resources;
        }
        catch (XMLStreamException | FileNotFoundException e) {
            throw new OvalParserException(
                    "Failed to parse the OVAL resources(tests, states and objects) from OVAL file at: " +
                            ovalFile.getAbsolutePath(), e);
        }
    }

    private DefinitionType parseDefinitionType(StartElement definitionElement, XMLEventReader2 reader)
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
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("metadata")) {
                    definitionType.setMetadata(parseDefinitionMetadata(reader));
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("criteria")) {
                    definitionType.setCriteria(parseDefinitionCriteria(nextEvent.asStartElement(), reader));
                }
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("definition")) {
                return definitionType;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </definition>");
    }

    private CriteriaType parseDefinitionCriteria(StartElement criteriaElement, XMLEventReader2 reader)
            throws XMLStreamException {
        CriteriaType criteriaType = new CriteriaType();

        criteriaElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();
            switch (attributeName) {
                case "comment":
                    criteriaType.setComment(attribute.getValue());
                    break;
                case "operator":
                    criteriaType.setOperator(LogicOperatorType.fromValue(attribute.getValue()));
                    break;
                case "negate":
                    criteriaType.setNegate(Boolean.valueOf(attribute.getValue()));
                    break;
                default:
                    break;
            }
        });

        List<BaseCriteria> children = new ArrayList<>();
        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();
            if (nextEvent.isStartElement()) {
                if (nextEvent.asStartElement().getName().getLocalPart().equals("criterion")) {
                    children.add(parseDefinitionCriterion(nextEvent.asStartElement()));
                }
                else if (nextEvent.asStartElement().getName().getLocalPart().equals("criteria")) {
                    children.add(parseDefinitionCriteria(nextEvent.asStartElement(), reader));
                }
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("criteria")) {
                criteriaType.setChildren(children);
                return criteriaType;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </criteria>");
    }

    private CriterionType parseDefinitionCriterion(StartElement criterionElement) {
        CriterionType criterionType = new CriterionType();

        criterionElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();
            switch (attributeName) {
                case "comment":
                    criterionType.setComment(attribute.getValue());
                    break;
                case "test_ref":
                    criterionType.setTestRef(attribute.getValue());
                    break;
                default:
                    break;
            }
        });

        return criterionType;
    }

    private MetadataType parseDefinitionMetadata(XMLEventReader2 reader) throws XMLStreamException {
        MetadataType metadataType = new MetadataType();
        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

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

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("metadata")) {
                return metadataType;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </metadata>");
    }

    private Advisory parseAdvisory(XMLEventReader2 reader) throws XMLStreamException {
        Advisory advisory = new Advisory();

        List<AdvisoryCveType> cveList = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

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

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("advisory")) {
                advisory.setCveList(cveList);
                return advisory;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </advisory>");
    }

    private AdvisoryAffectedType parseAdvisoryAffectedType(XMLEventReader2 reader) throws XMLStreamException {
        AdvisoryAffectedType advisoryAffectedType = new AdvisoryAffectedType();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement() &&
                    nextEvent.asStartElement().getName().getLocalPart().equals("resolution")) {
                advisoryAffectedType.setResolution(parseAdvisoryResolutionType(nextEvent.asStartElement(), reader));
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("affected")) {
                return advisoryAffectedType;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </affected>");
    }

    private AdvisoryResolutionType parseAdvisoryResolutionType(StartElement resolutionElement, XMLEventReader2 reader)
            throws XMLStreamException {
        AdvisoryResolutionType advisoryResolutionType = new AdvisoryResolutionType();

        Attribute stateAttribute = resolutionElement.getAttributeByName(new QName("state"));
        if (stateAttribute != null) {
            advisoryResolutionType.setState(stateAttribute.getValue());
        }

        List<String> affectedComponents = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement() &&
                    nextEvent.asStartElement().getName().getLocalPart().equals("component")) {
                affectedComponents.add(reader.getElementText());
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("resolution")) {
                advisoryResolutionType.setAffectedComponents(affectedComponents);
                return advisoryResolutionType;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </resolution>");
    }

    private AdvisoryCveType parseAdvisoryCve(XMLEventReader2 reader) throws XMLStreamException {
        AdvisoryCveType cveType = new AdvisoryCveType();

        cveType.setCve(reader.getElementText());

        return cveType;
    }

    private List<String> parseAffectedCpeList(XMLEventReader2 reader) throws XMLStreamException {
        List<String> cpes = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement() && nextEvent.asStartElement().getName().getLocalPart().equals("cpe")) {
                cpes.add(reader.getElementText());
            }

            if (nextEvent.isEndElement() &&
                    nextEvent.asEndElement().getName().getLocalPart().equals("affected_cpe_list")) {
                return cpes;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </affected_cpe_list>");
    }

    private List<TestType> parseTests(XMLEventReader2 reader) throws XMLStreamException {
        List<TestType> tests = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement()) {
                String element = nextEvent.asStartElement().getName().getLocalPart();
                if (element.equals("rpminfo_test")) {
                    tests.add(parseTestType(nextEvent.asStartElement(), reader, PackageType.RPM));
                }
                else if (element.equals("dpkginfo_test_test")) {
                    tests.add(parseTestType(nextEvent.asStartElement(), reader, PackageType.DEB));
                }
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("tests")) {
                return tests;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </tests>");
    }

    private TestType parseTestType(StartElement testElement, XMLEventReader2 reader, PackageType packageType)
            throws XMLStreamException {

        Objects.requireNonNull(packageType);
        TestType testType;
        if (packageType == PackageType.DEB) {
            testType = new DpkginfoTest();
        }
        else {
            testType = new RpminfoTest();
        }

        testElement.getAttributes().forEachRemaining(attribute -> {
            String attributeName = attribute.getName().getLocalPart();

            switch (attributeName) {
                case "id":
                    testType.setId(attribute.getValue());
                    break;
                case "comment":
                    testType.setComment(attribute.getValue());
                    break;
                default:
            }
        });

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement()) {
                processTestTypeStartElement(nextEvent, testType);
            }

            if (nextEvent.isEndElement()) {
                String element = nextEvent.asEndElement().getName().getLocalPart();
                if (TEST_TYPES.contains(element)) {
                    return testType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for test type");
    }

    private static void processTestTypeStartElement(XMLEvent2 nextEvent, TestType testType) {
        if (nextEvent.asStartElement().getName().getLocalPart().equals("object")) {
            Attribute objectRefAttribute = nextEvent.asStartElement().getAttributeByName(
                    new QName("object_ref"));
            if (objectRefAttribute != null) {
                testType.setObjectRef(objectRefAttribute.getValue());
            }
            else {
                LOG.warn("objectRef property was not found");
            }
        }
        else if (nextEvent.asStartElement().getName().getLocalPart().equals("state")) {
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

    private List<StateType> parseStates(XMLEventReader2 reader) throws XMLStreamException {
        List<StateType> states = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement()) {
                String element = nextEvent.asStartElement().getName().getLocalPart();
                if (element.equals("rpminfo_state")) {
                    states.add(parseStateType(nextEvent.asStartElement(), reader, PackageType.RPM));
                }
                else if (element.equals("dpkginfo_state")) {
                    states.add(parseStateType(nextEvent.asStartElement(), reader, PackageType.DEB));
                }
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("states")) {
                return states;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </states>");
    }

    private StateType parseStateType(StartElement rpmStateElement, XMLEventReader2 reader, PackageType packageType)
            throws XMLStreamException {
        Objects.requireNonNull(packageType);
        StateType stateType;
        if (packageType == PackageType.DEB) {
            stateType = new DpkginfoState();
        }
        else {
            stateType = new RpminfoState();
        }

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

        return processStateType(reader, stateType);
    }

    private StateType processStateType(XMLEventReader2 reader, StateType stateType) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();
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
                String element = nextEvent.asEndElement().getName().getLocalPart();
                if (STATE_TYPES.contains(element)) {
                    return stateType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for state type");
    }

    private ArchType parseArchStateEntity(StartElement archElement, XMLEventReader2 reader) throws XMLStreamException {
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

    private EVRType parseEVRStateEntity(StartElement evrElement, XMLEventReader2 reader) throws XMLStreamException {
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

    private VersionType parseVersionStateEntity(StartElement versionElement, XMLEventReader2 reader)
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

    private List<ObjectType> parseObjects(XMLEventReader2 reader) throws XMLStreamException {
        List<ObjectType> objects = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();

            if (nextEvent.isStartElement()) {
                String element = nextEvent.asStartElement().getName().getLocalPart();
                if (element.equals("rpminfo_object")) {
                    objects.add(parseObjectType(nextEvent.asStartElement(), reader, PackageType.RPM));
                }
                else if (element.equals("dpkginfo_object")) {
                    objects.add(parseObjectType(nextEvent.asStartElement(), reader, PackageType.DEB));
                }
            }

            if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equals("objects")) {
                return objects;
            }
        }

        throw new OvalParserException("Unable to find the closing tag for </objects>");
    }

    private ObjectType parseObjectType(StartElement rpmObjectElement, XMLEventReader2 reader, PackageType packageType)
            throws XMLStreamException {
        Objects.requireNonNull(packageType);
        ObjectType objectType;
        if (packageType == PackageType.DEB) {
            objectType = new DpkginfoObject();
        }
        else {
            objectType = new RpminfoObject();
        }

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
            XMLEvent2 nextEvent = (XMLEvent2) reader.nextEvent();
            if (nextEvent.isStartElement() && nextEvent.asStartElement().getName().getLocalPart().equals("name")) {
                objectType.setPackageName(reader.getElementText());
            }

            if (nextEvent.isEndElement()) {
                String element = nextEvent.asEndElement().getName().getLocalPart();
                if (OBJECT_TYPES.contains(element)) {
                    return objectType;
                }
            }
        }

        throw new OvalParserException("Unable to find the closing tag for object type");
    }

}
