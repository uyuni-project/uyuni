/**
 * Copyright (c) 2020 SUSE LLC
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
 * AsciidocWriter
 */
public class AsciidocWriter extends DocWriter {

    private static final String[] OTHER_FILES = {"faqs", "scripts"};

    /**
     * @param outputIn path to the output folder
     * @param templatesIn path to the HTML templates folder
     * @param productIn name of the product
     * @param debugIn whether to show debugging messages
     */
    public AsciidocWriter(String outputIn, String templatesIn, String productIn, boolean debugIn) {
        super(outputIn, templatesIn, productIn, debugIn);
    }

    /**
     *
     * {@inheritDoc}
     */
    public void write(List<Handler> handlers, Map<String, String> serializers) throws Exception {

        //First macro-tize the serializer's docs
        renderSerializers(templates, serializers);

        //Lets do the index first
        StringBuffer buffer = new StringBuffer();

        buffer.append(generateIndex(handlers, templates));

        for (Handler handler : handlers) {
            //writeFile(JSP_OUTPUT + "handlers/" + handler.getClassName() + ".html",
                    buffer.append(generateHandler(handler, templates));
        }

        writeFile(output + "handlers/apilist.adoc", buffer.toString());

        VelocityHelper vh = new VelocityHelper(templates);
        vh.addMatch("productName", product);
        for (String file : OTHER_FILES) {
            String content = vh.renderTemplateFile(file + ".txt");
            writeFile(output + file + ".adoc", content);
        }
    }
}
