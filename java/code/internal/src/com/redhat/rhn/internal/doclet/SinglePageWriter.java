/*
 * Copyright (c) 2020 SUSE LLC
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import java.util.List;
import java.util.Map;

/**
 *
 * Singlepage Writer
 */
public class SinglePageWriter extends DocWriter {

    /**
     * @param outputIn path to the output folder
     * @param templatesIn path to the single page templates folder
     * @param productIn name of the product
     * @param apiVersionIn version of the api
     * @param debugIn whether to show debugging messages
     */
    public SinglePageWriter(String outputIn, String templatesIn, String productIn,
                            String apiVersionIn, boolean debugIn) {
        super(outputIn, templatesIn, productIn, apiVersionIn, debugIn);
    }

    /**
     *
     * {@inheritDoc}
     */
    public void write(List<Handler> handlers,
            Map<String, String> serializers) throws Exception {
        //First macro-tize the serializer's docs
        renderSerializers(templates, serializers);

        //Lets do the index first
        StringBuffer buffer = new StringBuffer();

        buffer.append(generateIndex(handlers, templates));

        for (Handler handler : handlers) {
            //writeFile(output + "handlers/" + handler.getClassName() + ".html",
                    buffer.append(generateHandler(handler, templates));
        }

        writeFile(output + "handlers/apilist.html", buffer.toString());
    }


    /**
     * Generate the index from the template dir from (API_HEADER/INDEX/FOOTER_FILE) files
     * @param handlers list of the handlers
     * @param templateDir directory of the templates
     * @return a string representing the index
     * @throws Exception e
     */
    public  String generateIndex(List<Handler> handlers, String templateDir)
                throws Exception {

        String out = "";
        VelocityHelper vh = new VelocityHelper(templateDir);
        vh.addMatch("handlers", handlers);

        out += vh.renderTemplateFile(ApiDoclet.API_INDEX_FILE);

        return out;
    }
}
