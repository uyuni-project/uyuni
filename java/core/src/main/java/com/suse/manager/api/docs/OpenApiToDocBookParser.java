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
package com.suse.manager.api.docs;

import com.suse.manager.api.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OpenApiToDocBookParser {

    private static final Logger LOGGER = LogManager.getLogger(OpenApiToDocBookParser.class);

    private static final String XML_PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n";
    private static final String DOCTYPE_CHAPTER =
        "<!DOCTYPE chapter PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" " +
        "\"http://www.docbook.org/xml/4.5/docbookx.dtd\">%n";
    private static final String DOCTYPE_BOOK =
        "<!DOCTYPE book PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" " +
        "\"http://www.docbook.org/xml/4.5/docbookx.dtd\">%n";

    private final OpenAPI openAPI;

    public OpenApiToDocBookParser(OpenAPI openAPI) {
        this.openAPI = Objects.requireNonNull(openAPI, "OpenAPI spec cannot be null");
    }

    public static void main(String[] args) {
        String outputDir = System.getProperty("apidoc.output");
        if (outputDir == null || outputDir.isEmpty()) {
            LOGGER.error("Missing or empty 'apidoc.output' system property.");
            System.exit(1);
        }

        try {
            OpenAPI spec = OpenApiConfig.processHandlers();
            new OpenApiToDocBookParser(spec).generateDocumentation(outputDir);
            LOGGER.info("DocBook documentation generated successfully in: {}", outputDir);
        }
        catch (Exception e) {
            LOGGER.error("Error generating DocBook documentation: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public void generateDocumentation(String outputDir) throws IOException {
        Path pathDir = Paths.get(outputDir);
        Files.createDirectories(pathDir);

        Map<String, HandlerDoc> handlers = collectHandlers();

        for (Map.Entry<String, HandlerDoc> entry : handlers.entrySet()) {
            writeHandlerFile(entry.getValue(), pathDir);
        }

        writeBookIndex(handlers, pathDir);
    }

    private Map<String, HandlerDoc> collectHandlers() {
        Map<String, HandlerDoc> handlers = new TreeMap<>();

        if (openAPI.getPaths() == null) {
            return handlers;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            PathItem pathItem = pathEntry.getValue();
            if (pathItem == null) {
                continue;
            }
            processOperation(handlers, pathItem.getGet(), "GET");
            processOperation(handlers, pathItem.getPost(), "POST");
        }
        return handlers;
    }

    private void processOperation(Map<String, HandlerDoc> handlers, Operation op, String httpMethod) {
        if (op == null || op.getTags() == null || op.getTags().isEmpty()) {
            return;
        }
        String tag = op.getTags().get(0);
        HandlerDoc handler = handlers.computeIfAbsent(tag,
                k -> new HandlerDoc(k, getTagDescription(k)));

        boolean securityRequired = isSecurityRequired(op);
        List<String> required = getFieldsByRequirement(op, true);
        List<String> optional = getFieldsByRequirement(op, false);

        if (!optional.isEmpty()) {
            List<String> allParams = new ArrayList<>(required);
            allParams.addAll(optional);
            handler.calls.add(buildCallDoc(httpMethod, op, allParams, securityRequired));
        }
        handler.calls.add(buildCallDoc(httpMethod, op, required, securityRequired));
    }

    private CallDoc buildCallDoc(String httpMethod, Operation op,
                                 List<String> activeParams, boolean securityRequired) {
        String name = Optional.ofNullable(op.getOperationId())
                .filter(s -> !s.isBlank())
                .orElse("");
        String description = buildDescription(op);
        List<ParamDoc> params = buildParams(op, activeParams, securityRequired);
        String returnDoc = buildReturnDoc(op, name);
        return new CallDoc(name, httpMethod, description, params, returnDoc);
    }

    private List<String> getFieldsByRequirement(Operation op, boolean requiredOnly) {
        List<String> fields = new ArrayList<>();
        if (op.getParameters() != null) {
            op.getParameters().stream()
                    .filter(p -> Objects.equals(p.getRequired(), requiredOnly))
                    .map(Parameter::getName)
                    .forEach(fields::add);
        }
        Schema<?> bodySchema = getBodySchema(op);
        if (bodySchema != null && bodySchema.getProperties() != null) {
            List<String> requiredInBody = bodySchema.getRequired() != null
                    ? bodySchema.getRequired() : List.of();
            for (String propName : bodySchema.getProperties().keySet()) {
                if (requiredInBody.contains(propName) == requiredOnly) {
                    fields.add(propName);
                }
            }
        }
        return fields;
    }

    private List<ParamDoc> buildParams(Operation op, List<String> activeParams,
                                       boolean securityRequired) {
        List<ParamDoc> params = new ArrayList<>();

        if (securityRequired) {
            params.add(new ParamDoc("string", "sessionKey",
                    "Session token, must be obtained via auth.login."));
        }

        Map<String, Schema> allProps = getAllPossibleProperties(op);
        for (Map.Entry<String, Schema> e : allProps.entrySet()) {
            String name = e.getKey();
            if (!activeParams.contains(name)) {
                continue;
            }
            Schema<?> schema = e.getValue();
            String type = formatType(schema);
            String desc = findDescription(schema);
            params.add(new ParamDoc(type, name, desc));
        }
        return params;
    }

    private Map<String, Schema> getAllPossibleProperties(Operation op) {
        Map<String, Schema> props = new LinkedHashMap<>();
        if (op.getParameters() != null) {
            for (Parameter param : op.getParameters()) {
                props.put(param.getName(), param.getSchema());
            }
        }
        Schema<?> body = getBodySchema(op);
        if (body != null && body.getProperties() != null) {
            props.putAll((Map<String, Schema>) body.getProperties());
        }
        return props;
    }

    private String buildReturnDoc(Operation op, String fallbackLabel) {
        if (op.getResponses() == null) {
            return "<listitem><para></para></listitem>";
        }
        ApiResponse resp = op.getResponses().entrySet().stream()
                .filter(e -> e.getKey().startsWith("2") || e.getKey().equals("default"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

        if (resp == null) {
            return "<listitem><para></para></listitem>";
        }

        String responseDescription = resp.getDescription();
        Schema<?> respSchema = null;
        if (resp.getContent() != null && resp.getContent().get("application/json") != null) {
            respSchema = resp.getContent().get("application/json").getSchema();
        }
        if (respSchema == null) {
            String text = responseDescription != null ? responseDescription : "";
            return String.format("<listitem><para>%s</para></listitem>", escapeXml(text));
        }

        Schema<?> schema = respSchema.get$ref() != null
                ? resolveSchema(respSchema.get$ref()) : respSchema;
        String label = (responseDescription != null && !responseDescription.isBlank())
                ? responseDescription : fallbackLabel;

        return renderReturnSchema(schema, label);
    }

    private String renderReturnSchema(Schema<?> schema, String label) {
        if (schema == null) {
            return "";
        }
        if (schema.getProperties() != null && schema.getProperties().containsKey("result")) {
            Object resultProp = schema.getProperties().get("result");
            if (resultProp instanceof Schema<?> resultSchema) {
                String innerLabel = resultSchema.get$ref() != null
                        ? extractRefName(resultSchema.get$ref()) : label;
                Schema<?> resolved = resultSchema.get$ref() != null
                        ? resolveSchema(resultSchema.get$ref()) : resultSchema;
                return renderReturnSchema(resolved, innerLabel);
            }
        }
        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            Schema<?> item = schema.getItems();
            Schema<?> resolvedItem = item.get$ref() != null
                    ? resolveSchema(item.get$ref()) : item;
            String itemLabel = item.get$ref() != null ? extractRefName(item.get$ref()) : "";
            StringBuilder sb = new StringBuilder();
            sb.append("<listitem>\n");
            sb.append("  <para>array</para>\n");
            sb.append("  <itemizedlist spacing=\"compact\">\n");
            sb.append(indentLines(renderReturnSchema(resolvedItem, itemLabel), 4));
            sb.append("\n  </itemizedlist>\n");
            sb.append("</listitem>");
            return sb.toString();
        }
        if (isSimpleType(schema)) {
            String type = "integer".equals(schema.getType()) ? "int" : schema.getType();
            return String.format("<listitem><para>%s - %s</para></listitem>",
                    escapeXml(type), escapeXml(label));
        }
        if (schema.getAdditionalProperties() instanceof Schema<?> inner) {
            Schema<?> resolvedInner = inner.get$ref() != null
                    ? resolveSchema(inner.get$ref()) : inner;
            return renderStructList(resolvedInner, label.isEmpty() ? "namespace" : label);
        }
        return renderStructList(schema, label);
    }

    private String renderStructList(Schema<?> schema, String label) {
        if (schema == null) {
            return String.format("<listitem><para>struct %s</para></listitem>", escapeXml(label));
        }
        if (schema.getProperties() == null || schema.getProperties().isEmpty()) {
            return String.format("<listitem><para>struct %s</para></listitem>", escapeXml(label));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<listitem>\n");
        sb.append("  <para>struct ").append(escapeXml(label)).append("</para>\n");
        sb.append("  <itemizedlist spacing=\"compact\">\n");
        for (Map.Entry<String, Schema> e :
                ((Map<String, Schema>) schema.getProperties()).entrySet()) {
            String name = e.getKey();
            String desc = e.getValue().getDescription();
            String body = String.format("string \"%s\"", name);
            if (desc != null && !desc.isBlank()) {
                body += " - " + desc;
            }
            sb.append("    <listitem><para>")
              .append(escapeXml(body))
              .append("</para></listitem>\n");
        }
        sb.append("  </itemizedlist>\n");
        sb.append("</listitem>");
        return sb.toString();
    }

    private void writeHandlerFile(HandlerDoc handler, Path dir) throws IOException {
        Path filePath = dir.resolve(handler.name + ".xml");
        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            w.printf(XML_PREAMBLE);
            w.printf(DOCTYPE_CHAPTER);
            w.println();
            w.printf("<chapter role=\"namespace\">%n");
            w.printf("  <title>%s</title>%n", escapeXml(handler.name));
            w.printf("  <abstract><para>%s</para></abstract>%n%n",
                    escapeXml(handler.description));
            for (CallDoc call : handler.calls) {
                writeCall(w, call);
            }
            w.printf("</chapter>%n");
        }
    }

    private void writeCall(PrintWriter w, CallDoc call) {
        w.printf("  <sect1>%n");
        w.printf("    <title><function>%s</function></title>%n", escapeXml(call.name));
        w.printf("    <subtitle>HTTP <function>%s</function></subtitle>%n", call.httpMethod);
        w.printf("    <para/>%n%n");
        w.printf("    <variablelist>%n");
        w.printf("      <varlistentry>%n");
        w.printf("        <term>Description</term>%n");
        w.printf("        <listitem>%n");
        w.printf("          <para>%s</para>%n", escapeXml(call.description));
        w.printf("        </listitem>%n");
        w.printf("      </varlistentry>%n");
        w.printf("      <varlistentry>%n");
        w.printf("        <term>Parameters</term>%n");
        w.printf("        <listitem>%n");
        w.printf("          <itemizedlist spacing=\"compact\">%n");
        if (call.params.isEmpty()) {
            w.printf("            <listitem><para>None</para></listitem>%n");
        }
        else {
            for (ParamDoc p : call.params) {
                String body = p.description.isEmpty()
                        ? String.format("%s %s", p.type, p.name)
                        : String.format("%s %s - %s", p.type, p.name, p.description);
                w.printf("            <listitem><para>%s</para></listitem>%n",
                        escapeXml(body));
            }
        }
        w.printf("          </itemizedlist>%n");
        w.printf("        </listitem>%n");
        w.printf("      </varlistentry>%n");
        w.printf("      <varlistentry>%n");
        w.printf("        <term>Return Value</term>%n");
        w.printf("        <listitem>%n");
        w.printf("          <itemizedlist spacing=\"compact\">%n");
        for (String line : call.returnDoc.split("\n", -1)) {
            if (line.isEmpty()) {
                w.println();
            }
            else {
                w.printf("            %s%n", line);
            }
        }
        w.printf("          </itemizedlist>%n");
        w.printf("        </listitem>%n");
        w.printf("      </varlistentry>%n");

        w.printf("    </variablelist>%n");
        w.printf("  </sect1>%n%n");
    }

    private void writeBookIndex(Map<String, HandlerDoc> handlers, Path dir) throws IOException {
        Path filePath = dir.resolve("book.xml");
        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            w.printf(XML_PREAMBLE);
            w.printf(DOCTYPE_BOOK);
            w.println();

            w.printf("<book lang=\"en\" xmlns:xi=\"http://www.w3.org/2001/XInclude\">%n");
            w.printf("  <title>API Documentation</title>%n");
            w.printf("  <bookinfo>%n");
            w.printf("    <productname>Uyuni</productname>%n");
            w.printf("    <abstract>%n");
            w.printf("      <para>Welcome to the Uyuni API.</para>%n");
            w.printf("    </abstract>%n");
            w.printf("  </bookinfo>%n%n");
            for (String handlerName : handlers.keySet()) {
                w.printf("  <xi:include href=\"%s.xml\"/>%n", handlerName);
            }
            w.printf("</book>%n");
        }
    }

    private String getTagDescription(String tag) {
        return Optional.ofNullable(openAPI.getTags()).orElse(List.of()).stream()
                .filter(t -> t.getName() != null && t.getName().equals(tag))
                .map(io.swagger.v3.oas.models.tags.Tag::getDescription)
                .findFirst()
                .orElse("");
    }

    private String buildDescription(Operation op) {
        String summary = Optional.ofNullable(op.getSummary()).orElse("");
        String description = op.getDescription();
        if (description != null && !description.trim().equals(summary.trim())) {
            return summary + (summary.isEmpty() ? "" : "\n") + description;
        }
        return summary;
    }

    private boolean isSecurityRequired(Operation op) {
        if (op.getSecurity() != null && !op.getSecurity().isEmpty()) {
            return true;
        }
        return openAPI.getSecurity() != null && !openAPI.getSecurity().isEmpty();
    }

    private boolean isSimpleType(Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        String type = schema.getType();
        return "string".equals(type) || "integer".equals(type)
                || "boolean".equals(type) || "number".equals(type);
    }
    private String extractRefName(String ref) {
        if (ref == null) {
            return "";
        }
        String name = ref.substring(ref.lastIndexOf("/") + 1);
        return name.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
    }

    private Schema<?> resolveSchema(String ref) {
        if (ref == null || openAPI.getComponents() == null
                || openAPI.getComponents().getSchemas() == null) {
            return null;
        }
        return openAPI.getComponents().getSchemas()
                .get(ref.substring(ref.lastIndexOf("/") + 1));
    }

    private Schema<?> getBodySchema(Operation op) {
        try {
            var mediaType = op.getRequestBody().getContent().get("application/json");
            Schema<?> schema = mediaType.getSchema();
            return schema.get$ref() != null ? resolveSchema(schema.get$ref()) : schema;
        }
        catch (Exception e) {
            return null;
        }
    }

    private String findDescription(Schema<?> s) {
        if (s == null) {
            return "";
        }
        if (s.getDescription() != null) {
            return s.getDescription();
        }
        if (s.get$ref() != null) {
            Schema<?> res = resolveSchema(s.get$ref());
            if (res != null && res.getDescription() != null) {
                return res.getDescription();
            }
        }
        if (s.getItems() != null) {
            return findDescription(s.getItems());
        }
        return "";
    }

    private String formatType(Schema<?> s) {
        if (s == null) {
            return "string";
        }
        String type = s.getType();
        if ("array".equals(type)) {
            Schema<?> items = s.getItems();
            if (items != null) {
                if (items.get$ref() != null) {
                    return "array(" + extractRefName(items.get$ref()) + ")";
                }
                String itemType = items.getType();
                if (itemType != null) {
                    return "array(" + itemType + ")";
                }
            }
            return "array";
        }
        if (s.get$ref() != null) {
            return extractRefName(s.get$ref());
        }
        return type != null ? type : "string";
    }

    private String indentLines(String s, int n) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String prefix = " ".repeat(n);
        return prefix + s.replace("\n", "\n" + prefix);
    }

    private static String escapeXml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    private static final class HandlerDoc {
        final String name;
        final String description;
        final List<CallDoc> calls = new ArrayList<>();

        HandlerDoc(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private static final class CallDoc {
        final String name;
        final String httpMethod;
        final String description;
        final List<ParamDoc> params;
        final String returnDoc;

        CallDoc(String name, String httpMethod, String description,
                List<ParamDoc> params, String returnDoc) {
            this.name = name;
            this.httpMethod = httpMethod;
            this.description = description;
            this.params = params;
            this.returnDoc = returnDoc;
        }
    }

    private static final class ParamDoc {
        final String type;
        final String name;
        final String description;

        ParamDoc(String type, String name, String description) {
            this.type = type;
            this.name = name;
            this.description = description;
        }
    }
}