/*
 * Copyright (c) 2024 SUSE LLC
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Writes Go code from API docs.
 */
public class GoWriter extends DocWriter {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Constructor
     *
     * @param outputFolder where to generate the files
     * @param templateFolder the folder containing the templates
     * @param debug turn debug mode on or off
     */
    public GoWriter(String outputFolder, String templateFolder, boolean debug) {
        super(outputFolder, templateFolder, "", "", debug);

    }

    @Override
    public void write(List<Handler> handlers, Map<String, String> serializers) throws IOException {
        // First write the serializers into types
        renderSerializers(templates, serializers);

        for (Handler handler : handlers) {
            String modulePath = handler.getName().replace(".", "/");
            String[] segments = modulePath.split("/");
            String moduleName = segments[segments.length - 1];

            String folder = String.join(File.separator, output, "mgrctl", "cmd", modulePath);
            Files.createDirectories(Path.of(folder));

            String sharedFolder = String.join(File.separator, output, "shared", "api", modulePath);
            Files.createDirectories(Path.of(sharedFolder));

            Map<String, List<ApiCall>> calls = handler.getCalls().stream()
                    .filter(call -> !call.isDeprecated())   // Do not wrap the deprecated calls
                    .collect(Collectors.groupingBy(ApiCall::getName));

            for (Map.Entry<String, List<ApiCall>> entry : calls.entrySet()) {
                generateCallCmd(modulePath, sharedFolder, moduleName, entry.getValue(), folder);
            }
            // Generate the mgrctl/cmd/<module>/<module>.go file
            generateHandlerCommand(moduleName, handler, calls.keySet(), folder);
        }
    }

    private void generateHandlerCommand(String moduleName, Handler handler, Set<String> calls,
                                        String folder) throws IOException {
        VelocityHelper vh = new VelocityHelper(templates);

        vh.addMatch("package_name", moduleName);
        vh.addMatch("package_doc", handler.getDesc());
        vh.addMatch("calls", calls);
        String out = vh.renderTemplateFile("module_cmd.txt");
        writeFile(String.join(File.separator, folder, moduleName + ".go"), out);
    }

    private String renderJsonMacro(String input) throws IOException {
        log("Rendering Json macro: " + input);
        String macro = readFile(templates + "json_macros.txt");

        try {
            VelocityHelper helper = new VelocityHelper();
            return helper.renderTemplate(macro + input + " \n ");
        }
        catch (Exception e) {
            String errorFile = "/tmp/apidoc_error.txt";
            System.out.println("ATTN!!!!!!!!!!");
            System.out.println("There was an error");
            System.out.println("We have dumped the full text to "  + errorFile + ".");
            System.out.println("Please lookup the appropriate line number printed " +
                    "in the following traceback and reference it with the written file.");
            writeFile(errorFile,  macro + input + "\n");
            throw e;
        }
    }

    private void generateCallCmd(String modulePath, String sharedFolder, String moduleName, List<ApiCall> calls,
                                 String folder ) throws IOException {
        VelocityHelper vh = new VelocityHelper(templates);

        vh.addMatch("package_name", moduleName);
        vh.addMatch("package_path", modulePath);

        String callName = calls.get(0).getName();
        vh.addMatch("call_name", calls.get(0).getName());

        log("Generate call for " + modulePath + "." + callName);

        // Merge all parameters of the possible calls
        List<String> params = calls.stream()
                .flatMap(call -> call.getParams().stream())
                .filter(param -> !param.contains("#session_key()")) // API code in mgrctl takes care of the key
                .distinct()
                .collect(Collectors.toList());
        List<ApiParam> apiParams = new ArrayList<>();
        for (String param : params) {
            String jsonParam = renderJsonMacro(param);
            log("Parsing Json: " + jsonParam);
            try {
                apiParams.add(GSON.fromJson(jsonParam, ApiParam.class));
            }
            catch (Exception e) {
                log("Unhandled parameter template");
            }
        }
        vh.addMatch("call_params", apiParams);

        List<String> returns = calls.stream()
                .map(call -> call.getReturnDoc())
                .distinct()
                .collect(Collectors.toList());
        if (returns.size() > 1) {
            log("Multiple calls with the same name have different results, picking the first !!");
        }

        String returnType = null;
        if (!returns.isEmpty()) {
            returnType = returns.get(0);
            if (returnType.startsWith("$") && returnType.endsWith("Serializer")) {
                returnType = returnType.replace("$", "").replace("Serializer", "");
            }
        }

        vh.addMatch("call_return", returns.isEmpty() ? null : returnType);
        vh.addMatch("call_desc", calls.get(0).getDoc());
        vh.addMatch("call_method", calls.get(0).isReadOnly() ? "Get" : "Post");

        // Generate the mgrctl/cmd/<module>/<function>.go file
        String out = vh.renderTemplateFile("call_cmd.txt");
        writeFile(String.join(File.separator, folder, callName + ".go"), out);

        // Generate the shared/api/<module>/<function>.go file
        String outShared = vh.renderTemplateFile("shared.txt");
        writeFile(String.join(File.separator, sharedFolder, callName + ".go"), outShared);

    }

    @Override
    public void renderSerializers(String templateDir, Map<String, String> serializers) throws IOException {
        String typesFolder = String.join(File.separator, output, "shared", "api", "types");
        Files.createDirectories(Path.of(typesFolder));
        serializerRenderer = new VelocityHelper(templateDir);

        Map<String, String> serializersMacros = new HashMap<>();

        VelocityHelper helper = new VelocityHelper();

        // Replace all the serializer variables with their content
        for (Map.Entry<String, String> rawSerializer : serializers.entrySet()) {
            helper.addMatch(rawSerializer.getKey(), rawSerializer.getValue());
        }

        Pattern namePattern = Pattern.compile("#struct_begin\\(\"([^\"]+)\"\\)");

        for (Map.Entry<String, String> serializer : serializers.entrySet()) {
            String name = serializer.getKey().replace("Serializer", "");
            String completeTemplate = helper.renderTemplate(serializer.getValue());
            Matcher m = namePattern.matcher(completeTemplate);
            if (m.find()) {
                completeTemplate = m.replaceFirst("#struct_begin(\"" + name + "\")");

                // Don't process non-structs
                serializersMacros.put(name, completeTemplate);
            }
        }

        // Actually run the macros to generate the Go type files.
        for (Map.Entry<String, String> serializer : serializersMacros.entrySet()) {
            String name = serializer.getKey();
            log("Rendering serializer macro: " + name);
            helper.addMatch("stacked_mapstructures", new Stack<String>());

            String temp = renderMacro(helper, templateDir, serializer.getValue(), name);
            String fileName = name.toLowerCase() + ".go";
            writeFile(String.join(File.separator, typesFolder, fileName), temp);
            writeFile(String.join(File.separator, typesFolder, fileName + ".tpl"), serializer.getValue());
        }
    }

}
