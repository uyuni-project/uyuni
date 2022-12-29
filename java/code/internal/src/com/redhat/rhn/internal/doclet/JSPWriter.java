/*
 * Copyright (c) 2020 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.internal.doclet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * JSP Writer
 */
public class JSPWriter extends DocWriter {

    private static final String[] OTHER_FILES = {"faqs", "scripts"};

    /**
     * @param outputIn path to the output folder
     * @param templatesIn path to the JSP templates folder
     * @param productIn name of the product
     * @param apiVersionIn version of the api
     * @param debugIn whether to show debugging messages
     */
    public JSPWriter(String outputIn, String templatesIn, String productIn, String apiVersionIn, boolean debugIn) {
        super(outputIn, templatesIn, productIn, apiVersionIn, debugIn);
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void write(List<Handler> handlers, Map<String, String> serializers) throws IOException {

        // Create the handlers folder
        String handlersDir = output + "/handlers/";
        File folder = new File(handlersDir);
        folder.mkdirs();

        //First macro-tize the serializer's docs
        renderSerializers(templates, serializers);

        //Lets do the index first
        writeFile(output + "index.jsp", generateIndex(handlers, templates));

        for (Handler handler : handlers) {

            writeFile(handlersDir + handler.getClassName() + ".jsp",
                    generateHandler(handler, templates));
        }

        for (String file : OTHER_FILES) {
            writeFile(output + file + ".jsp", readFile(templates + file + ".txt"));
        }
    }
}
