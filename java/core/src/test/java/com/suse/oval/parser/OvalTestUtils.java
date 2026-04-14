/*
 * Copyright (c) 2026 SUSE LLC
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

import com.suse.oval.exceptions.OvalParserException;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.OvalRootType;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for OVAL parsing to be used in tests.
 */
public class OvalTestUtils {

    private static final OvalParser PARSER = new OvalParser();

    private OvalTestUtils() {
    }

    /**
     * Parse the given OVAL file
     *
     * @param ovalFileURL the OVAL file to parse
     * @return the parsed OVAL encapsulated in an {@link OvalRootType} object.
     * */
    public static OvalRootType parse(URL ovalFileURL) throws OvalParserException {
        File ovalFile;
        try {
            ovalFile = new File(ovalFileURL.toURI());
        }
        catch (URISyntaxException e) {
            throw new OvalParserException("Bad OVAL file path: " + ovalFileURL, e);
        }

        List<DefinitionType> allDefinitions = parseAllDefinitions(ovalFile);
        OVALResources ovalResources = PARSER.parseResources(ovalFile);

        OvalRootType ovalRootType = new OvalRootType();
        ovalRootType.setDefinitions(allDefinitions);
        ovalRootType.setObjects(ovalResources.getObjects());
        ovalRootType.setStates(ovalResources.getStates());
        ovalRootType.setTests(ovalResources.getTests());

        return ovalRootType;
    }

    /**
     * Utility method to parse all OVAL definitions at once instead of in bulks. To be used in testing.
     *
     * @param ovalFile an XML file containing OVAL definitions to be parsed.
     * @return all OVAL definitions in {@code ovalFile}
     * */
    public static List<DefinitionType> parseAllDefinitions(File ovalFile) {
        List<DefinitionType> allDefinitions = new ArrayList<>();

        PARSER.parseDefinitionsInBulk(ovalFile, allDefinitions::addAll);

        return allDefinitions;
    }
}
