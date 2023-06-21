package com.suse.oval;

import com.suse.oval.exceptions.OvalParserException;
import com.suse.oval.ovaltypes.OvalRootType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * The Oval Parser is responsible for parsing OVAL(Open Vulnerability and Assessment Language) documents
 */
public class OvalParser {

    public OvalRootType parse(File ovalFile) throws OvalParserException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(OvalRootType.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (OvalRootType) unmarshaller.unmarshal(ovalFile);
        } catch (JAXBException e) {
            throw new OvalParserException("Failed to parse the given OVAL file at: " + ovalFile.getAbsolutePath(), e);
        }
    }

}
