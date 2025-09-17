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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * Base doc Writer
 */
@SuppressWarnings("java:S106")
public abstract class DocWriter {


    protected VelocityHelper serializerRenderer;
    protected String output;
    protected String templates;
    protected String product;
    protected String apiVersion;
    private final boolean debug;


    /**
     * Constructor
     * @param outputIn path to the output folder
     * @param templatesIn path to the HTML templates folder
     * @param productIn name of the product
     * @param apiVersionIn version of the api
     * @param debugIn whether to show debugging messages
     *
     */
    protected DocWriter(String outputIn, String templatesIn, String productIn, String apiVersionIn, boolean debugIn) {
        output = outputIn;
        templates = templatesIn;
        product = productIn;
        apiVersion = apiVersionIn;
        debug = debugIn;
    }

    protected void writeFile(String filePath, String contents) throws IOException {
        try (
                FileWriter fileWrite = new FileWriter(filePath);
                BufferedWriter bw = new BufferedWriter(fileWrite)
        ) {
            bw.write(contents);
        }
    }

    protected String readFile(String filePath) throws IOException {
        StringBuilder toReturn = new StringBuilder();
        try (
                FileReader input = new FileReader(filePath);
                BufferedReader bufRead = new BufferedReader(input)
        ) {
            String line = bufRead.readLine();

            while (line != null) {
                toReturn.append(line).append("\n");
                line = bufRead.readLine();
            }
        }
        return toReturn.toString();
   }

    /**
     * Generate the index from the template dir from (API_HEADER/INDEX/FOOTER_FILE) files
     * @param handlers list of the handlers
     * @param templateDir directory of the templates
     * @return a string representing the index
     */
    public String generateIndex(List<Handler> handlers, String templateDir) {
        VelocityHelper vh = new VelocityHelper(templateDir);
        vh.addMatch("handlers", handlers);
        vh.addMatch("productName", product);
        vh.addMatch("apiVersion", apiVersion);
        String out = vh.renderTemplateFile(ApiDoclet.API_HEADER_FILE);
        out += vh.renderTemplateFile(ApiDoclet.API_INDEX_FILE);
        out += vh.renderTemplateFile(ApiDoclet.API_FOOTER_FILE);
        return out;
    }

    /**
     * generate a templated handler from teh template dir and the file (API_HANDLER_FILE)
     * @param handler the handler in question
     * @param templateDir the directory of templates
     * @return a string that is the templated handler
     * @throws IOException e
     */
    public String generateHandler(Handler handler, String templateDir) throws IOException {
        for (ApiCall call : handler.getCalls()) {
            log(String.format("Generating handler call %s.%s", handler.getName(), call.getName()));
            call.setReturnDoc(renderMacro(templateDir, call.getReturnDoc(),
                    call.getName()));
            List<String> params = new ArrayList<>();

            for (String param : call.getParams()) {
                params.add(renderMacro(templateDir, param,
                        "param:" + param));
            }
            call.setParams(params);

        }

        VelocityHelper vh = new VelocityHelper(templateDir);
        vh.addMatch("handler", handler);
        String out = vh.renderTemplateFile(ApiDoclet.API_HANDLER_FILE);

        //Now we render the serializers
        out = serializerRenderer.renderTemplate(out);

        return out;
    }

    /**
     * Renders the input agains the api macros file in a given directory.
     * @param templateDir The directory of the macros.txt file
     * @param input the input to macrotize
     * @param description a description to use in case something goes wrong
     * @return the macrotized input
     * @throws IOException e
     */
    public String renderMacro(String templateDir, String input, String description) throws IOException {
        log("Rendering macro: " + input + ", " + description);
        VelocityHelper macros = new VelocityHelper(templateDir);
        String macro = readFile(templateDir + ApiDoclet.API_MACROS_FILE);

        String productMacro = "#macro(product)" + product + "#end\n";

        try {
            return macros.renderTemplate(macro + productMacro + input + " \n ");
        }
        catch (Exception e) {
            String errrorFile = "/tmp/apidoc_error.txt";
            System.out.println("ATTN!!!!!!!!!!");
            System.out.println("There was an error macro-tizing " + description);
            System.out.println("We have dumped the full text to "  + errrorFile + ".");
            System.out.println("Please lookup the appropriate line number printed " +
                    "in the following traceback and reference it with the written file.");
            writeFile(errrorFile,  macro + input + "\n");
            throw e;
        }

    }

    /**
     * render the serializers
     * @param templateDir the template directory for this writer
     * @param serializers Map of serializers
     * @throws IOException e
     */
    public void renderSerializers(String templateDir, Map<String, String> serializers) throws IOException {

        serializerRenderer = new VelocityHelper(templateDir);

        // macrotize the serializers
        for (Map.Entry<String, String> serializer : serializers.entrySet()) {
            String name = serializer.getKey();
            log("Rendering serializer macro: " + name);
            String temp = renderMacro(templateDir, serializer.getValue(), name);
            serializers.put(name, temp);
            serializerRenderer.addMatch(name, serializer.getValue());
        }

        VelocityHelper tempRenderer = new VelocityHelper();
        for (Map.Entry<String, String> serializer : serializers.entrySet()) {
            String name = serializer.getKey();
            log("Rendering serializer: " + name);
            String temp = serializerRenderer.renderTemplate(serializer.getValue());
            serializers.put(name, temp);
            tempRenderer.addMatch(name, temp);
        }
        serializerRenderer = tempRenderer;
    }


    /**
     * write the specified writer
     * @param handlers list of handlers
     * @param serializers a list of serializers to write with
     * @throws IOException e
     */
    public abstract void write(List<Handler> handlers,
            Map<String, String> serializers) throws IOException;


    protected void log(String msg) {
        if (debug) {
            System.err.println(msg);
        }
    }
}
