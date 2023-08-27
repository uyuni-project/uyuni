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
import com.suse.oval.ovaltypes.OvalRootType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The Oval Parser is responsible for parsing OVAL(Open Vulnerability and Assessment Language) documents
 */
public class OvalParser {

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

    public OvalRootType parse(URL url) {
        try {
            return parse(new File(url.toURI()));
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
