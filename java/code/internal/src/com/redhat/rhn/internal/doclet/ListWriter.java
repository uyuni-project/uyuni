/**
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.VariableElement;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * Writes a  list of api calls that are used
 */
public class ListWriter extends DocWriter {

    private DocletEnvironment docEnv;
    private String output;

    /**
     * Constructor
     *
     * @param docEnvIn doclet environement
     * @param outputFolderIn folder where the apilist.txt file will be written
     */
    public ListWriter(DocletEnvironment docEnvIn, String outputFolderIn) {
        docEnv = docEnvIn;
        output = outputFolderIn;
    }
/**
     *
     * {@inheritDoc}
     */
    public void write(List<Handler> handlers,
            Map<String, String> serializers) throws Exception {

        FileWriter fstream = new FileWriter(output + "/apilist.txt");
        BufferedWriter out = new BufferedWriter(fstream);

        for (Handler handler : handlers) {
            for (ApiCall call : handler.getCalls()) {
                out.write(handler.getName() + "." + call.getName() + " " +
                        call.getMethod().getParameters().size() + " ");

                for (VariableElement param : call.getMethod().getParameters()) {
                    out.write(docEnv.getTypeUtils().asElement(param.asType()).getSimpleName().toString() + " ");
                }
                out.write("\n");

            }
        }
        out.close();
    }

}
