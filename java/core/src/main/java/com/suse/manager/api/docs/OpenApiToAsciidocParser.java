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
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class OpenApiToAsciidocParser {

    private static final Logger LOGGER = LogManager.getLogger(OpenApiToAsciidocParser.class);
    private final OpenAPI openAPI;

    public OpenApiToAsciidocParser(OpenAPI openAPI) {
        this.openAPI = Objects.requireNonNull(openAPI, "OpenAPI spec cannot be null");
    }

    public static void main(String[] args) {
        String outputDir = System.getProperty("apidoc.output");
        if (outputDir == null || outputDir.isEmpty()) {
            LOGGER.error("Missing or empty 'apidoc.output' system property.");
            System.exit(1);
        }

        try {
            var spec = OpenApiConfig.processHandlers();
            new OpenApiToAsciidocParser(spec).generateDocumentation(outputDir);
            LOGGER.info("Documentation generated successfully in: {}", outputDir);
        }
        catch (Exception e) {
            LOGGER.error("Error generating documentation: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public void generateDocumentation(String outputDir) throws IOException {
        Path pathDir = Paths.get(outputDir);
        Files.createDirectories(pathDir);
        Map<String, List<DocEntry>> taggedOps = new TreeMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            if (pathItem.getGet() != null) processOperation("GET", pathItem.getGet(), taggedOps);
            if (pathItem.getPost() != null) processOperation("POST", pathItem.getPost(), taggedOps);
        });

        for (var entry : taggedOps.entrySet()) {
            writeAdocFile(entry.getKey(), entry.getValue(), pathDir);
        }
    }

    private void processOperation(String method, Operation op, Map<String, List<DocEntry>> map) {
        if (op == null || op.getTags() == null || op.getTags().isEmpty()) return;
        String tag = op.getTags().get(0);

        boolean securityRequired = isSecurityRequired(op);
        List<String> required = getFieldsByRequirement(op, true);
        List<String> optional = getFieldsByRequirement(op, false);

        List<DocEntry> entries = map.computeIfAbsent(tag, k -> new ArrayList<>());

        if (!optional.isEmpty()) {
            List<String> allParams = new ArrayList<>(required);
            allParams.addAll(optional);
            entries.add(DocEntry.create(method, op, allParams, securityRequired));
        }
        entries.add(DocEntry.create(method, op, required, securityRequired));
    }

    private void writeAdocFile(String tag, List<DocEntry> entries, Path dir) throws IOException {
        Path filePath = dir.resolve(tag + ".adoc");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            writer.printf("[#apidoc-%s]%n= %s%n%n== Available methods%n%n", tag, tag);
            for (var entry : entries) {
                writer.printf("* <<apidoc-%s-%s,%s>>%n", tag, entry.anchor(), entry.operation().getOperationId());
            }
            writer.printf("%n== Description%n%n%s%n%n*Namespace*:%n%n%s%n%n", getTagDesc(tag), tag);
            for (var entry : entries) {
                writeMethod(writer, tag, entry);
            }
        }
    }

    private void writeMethod(PrintWriter writer, String tag, DocEntry entry) {
        Operation op = entry.operation();
        String summary = Optional.ofNullable(op.getSummary()).orElse("");
        String description = (op.getDescription() != null && !op.getDescription().trim().equals(summary.trim()))
                ? op.getDescription() : "";

        writer.printf("""
            [#apidoc-%s-%s]
            == Method: %s

            HTTP `%s`

            Description:

            %s
            %s

            Parameters:

            """, tag, entry.anchor(), op.getOperationId(), entry.method(), summary, description
        );

        if (isSecurityRequired(op)) {
            writer.println("* [.string]#string#  sessionKey\n");
        }

        Map<String, Schema> allProps = getAllPossibleProperties(op);
        allProps.forEach((paramName, schema) -> {
            if (entry.activeParams().contains(paramName)) {
                String type = "array".equals(schema.getType()) ? "[.array]#string array#" : "[." + schema.getType() + "]#" + schema.getType() + "#";
                String descText = findDescription(schema);
                writer.printf("* %s  %s%s%n ", type, paramName, descText.isEmpty() ? "" : " - " + descText);
            }
        });

        writer.println("\nReturns:\n");
        writeReturn(writer, op);
        writer.print("\n\n\n");
    }

    private boolean isSecurityRequired(Operation op) {
        if (op.getSecurity() != null) return !op.getSecurity().isEmpty();
        return openAPI.getSecurity() != null && !openAPI.getSecurity().isEmpty();
    }

    private void writeReturn(PrintWriter writer, Operation op) {
        var responses = op.getResponses();
        if (responses == null || responses.get("200") == null || responses.get("200").getContent() == null) return;

        Schema<?> respSchema = responses.get("200").getContent().get("application/json").getSchema();
        if (respSchema == null) return;

        Schema<?> schema = respSchema.get$ref() != null ? resolveSchema(respSchema.get$ref()) : respSchema;
        String refName = "";

        if (schema.getProperties() != null && schema.getProperties().containsKey("result")) {
            Schema<?> resultSchema = (Schema<?>) schema.getProperties().get("result");
            refName = extractRefName(resultSchema.get$ref());
            schema = resultSchema.get$ref() != null ? resolveSchema(resultSchema.get$ref()) : resultSchema;
        }

        if (isSimpleType(schema)) {
            String type = schema.getType();
            String label = op.getOperationId()
                    .replace("get", "")
                    .replaceAll("([a-z])([A-Z])", "$1 $2")
                    .toLowerCase().trim();

            if (label.contains("system version")) label = "version";

            writer.printf("* [.%s]#%s#  %s%n ", type, type, label);
            return;
        }

        printStruct(writer, schema, 0, refName);
    }

    private void printStruct(PrintWriter writer, Schema<?> schema, int indent, String forcedLabel) {
        if (schema == null) return;
        String prefix = " ".repeat(indent);
        String marker = (indent == 0) ? "*" : "**";

        String label = (forcedLabel != null && !forcedLabel.isEmpty()) ? forcedLabel : "";
        if (label.isEmpty() && schema.getAdditionalProperties() != null) {
            label = "namespace";
        }

        if (schema.getProperties() != null || schema.getAdditionalProperties() != null) {
            writer.printf("%s%s [.struct]#struct#  %s%n", prefix, marker, label);
        }

        if (schema.getProperties() != null) {
            schema.getProperties().forEach((name, prop) -> {
                String propDesc = (prop.getDescription() != null) ? " - " + prop.getDescription() : "";
                writer.printf("%s** [.string]#string#  \"%s\"%s%n", prefix, name, propDesc);
            });
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> inner) {
            Schema<?> resolvedInner = inner.get$ref() != null ? resolveSchema(inner.get$ref()) : inner;
            if (resolvedInner.getProperties() != null) {
                resolvedInner.getProperties().forEach((name, prop) -> {
                    String propDesc = (prop.getDescription() != null) ? " - " + prop.getDescription() : "";
                    writer.printf("%s** [.string]#string#  \"%s\"%s%n", prefix, name, propDesc);
                });
            }
            else if (isSimpleType(resolvedInner)) {
                String desc = (resolvedInner.getDescription() != null) ? " - " + resolvedInner.getDescription() : "";
                writer.printf("%s** [.string]#string#%s%n", prefix, desc);
            }
        }
    }

    private String extractRefName(String ref) {
        if (ref == null) return null;
        String name = ref.substring(ref.lastIndexOf("/") + 1);
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private boolean isSimpleType(Schema<?> schema) {
        String type = schema.getType();
        return "string".equals(type) || "integer".equals(type) || "boolean".equals(type) || "number".equals(type);
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
            List<String> requiredInBody = bodySchema.getRequired() != null ? bodySchema.getRequired() : List.of();
            Map<String, Schema> props = (Map<String, Schema>) bodySchema.getProperties();
            for (String propName : props.keySet()) {
                if (requiredInBody.contains(propName) == requiredOnly) {
                    fields.add(propName);
                }
            }
        }
        return fields;
    }

    private Map<String, Schema> getAllPossibleProperties(Operation op) {
        Map<String, Schema> props = new LinkedHashMap<>();
        if (op.getParameters() != null) {
            for (var param : op.getParameters()) {
                props.put(param.getName(), param.getSchema());
            }
        }
        Schema<?> body = getBodySchema(op);
        if (body != null && body.getProperties() != null) {
            props.putAll((Map<String, Schema>) body.getProperties());
        }
        return props;
    }

    private Schema<?> resolveSchema(String ref) {
        if (ref == null || openAPI.getComponents() == null) return null;
        return openAPI.getComponents().getSchemas().get(ref.substring(ref.lastIndexOf("/") + 1));
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

    private String getTagDesc(String tag) {
        return Optional.ofNullable(openAPI.getTags()).orElse(List.of()).stream()
                .filter(t -> t.getName().equals(tag)).map(io.swagger.v3.oas.models.tags.Tag::getDescription)
                .findFirst().orElse("");
    }

    private String findDescription(Schema<?> s) {
        if (s.getDescription() != null) return s.getDescription();
        if (s.get$ref() != null) {
            Schema<?> res = resolveSchema(s.get$ref());
            if (res != null && res.getDescription() != null) return res.getDescription();
        }
        return (s.getItems() != null) ? findDescription(s.getItems()) : "";
    }

    private record DocEntry(String method, String anchor, Operation operation, List<String> activeParams) {
        static DocEntry create(String method, Operation op, List<String> params, boolean securityRequired) {
            String suffix = params.stream().sorted().collect(Collectors.joining("-"));
            String authPart = securityRequired ? "loggedInUser" : "";

            List<String> anchorParts = new ArrayList<>();
            anchorParts.add(op.getOperationId());
            if (!authPart.isEmpty()) anchorParts.add(authPart);
            if (!suffix.isEmpty()) anchorParts.add(suffix);

            String anchor = String.join("-", anchorParts);
            if (authPart.isEmpty() && suffix.isEmpty()) {
                anchor += "-";
            }

            return new DocEntry(method, anchor, op, List.copyOf(params));
        }
    }
}
