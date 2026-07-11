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

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * Converts the generated OpenAPI specification into AsciiDoc files.
 */
public class OpenApiToAsciidocParser {

    private static final Logger LOGGER = LogManager.getLogger(OpenApiToAsciidocParser.class);

    private final OpenAPI openAPI;

    /**
     * Creates a new parser for the given OpenAPI specification.
     *
     * @param openApiSpec OpenAPI specification
     */
    public OpenApiToAsciidocParser(OpenAPI openApiSpec) {
        this.openAPI = Objects.requireNonNull(openApiSpec, "OpenAPI spec cannot be null");
    }

    /**
     * Generates AsciiDoc documentation into the configured output directory.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String outputDir = System.getProperty("apidoc.output");
        if (outputDir == null || outputDir.isEmpty()) {
            LOGGER.error("Missing or empty 'apidoc.output' system property.");
            System.exit(1);
        }

        try {
            OpenAPI spec = OpenApiConfig.processHandlers();
            new OpenApiToAsciidocParser(spec).generateDocumentation(outputDir);
            LOGGER.info("Documentation generated successfully in: {}", outputDir);
        }
        catch (Exception e) {
            LOGGER.error("Error generating documentation: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Generates AsciiDoc files for all tags present in the OpenAPI specification.
     *
     * @param outputDir output directory
     * @throws IOException if writing documentation fails
     */
    public void generateDocumentation(String outputDir) throws IOException {
        Path pathDir = Paths.get(outputDir);
        Files.createDirectories(pathDir);
        Map<String, List<DocEntry>> taggedOps = new TreeMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            if (pathItem.getGet() != null) {
                processOperation("GET", pathItem.getGet(), taggedOps);
            }
            if (pathItem.getPost() != null) {
                processOperation("POST", pathItem.getPost(), taggedOps);
            }
        });

        for (Map.Entry<String, List<DocEntry>> entry : taggedOps.entrySet()) {
            writeAdocFile(entry.getKey(), entry.getValue(), pathDir);
        }
    }

    private void processOperation(String method, Operation operation, Map<String, List<DocEntry>> operationsByTag) {
        if (operation == null || operation.getTags() == null || operation.getTags().isEmpty()) {
            return;
        }
        String tag = operation.getTags().get(0);

        boolean securityRequired = isSecurityRequired(operation);
        List<String> required = getFieldsByRequirement(operation, true);
        List<String> optional = getFieldsByRequirement(operation, false);

        List<DocEntry> entries = operationsByTag.computeIfAbsent(tag, key -> new ArrayList<>());

        if (!optional.isEmpty()) {
            List<String> allParams = new ArrayList<>(required);
            allParams.addAll(optional);
            entries.add(DocEntry.create(method, operation, allParams, securityRequired));
        }
        entries.add(DocEntry.create(method, operation, required, securityRequired));
    }

    private void writeAdocFile(String tag, List<DocEntry> entries, Path dir) throws IOException {
        Path filePath = dir.resolve(tag + ".adoc");
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))) {
            writer.printf("[#apidoc-%s]%n= %s%n%n== Available methods%n%n", tag, tag);
            for (DocEntry entry : entries) {
                writer.printf("* <<apidoc-%s-%s,%s>>%n", tag, entry.anchor(),
                        entry.operation().getOperationId());
            }
            writer.printf("%n== Description%n%n%s%n%n*Namespace*:%n%n%s%n%n", getTagDesc(tag), tag);
            for (DocEntry entry : entries) {
                writeMethod(writer, tag, entry);
            }
        }
    }

    private void writeMethod(PrintWriter writer, String tag, DocEntry entry) {
        Operation operation = entry.operation();
        String summary = Optional.ofNullable(operation.getSummary()).orElse("");
        String description = "";
        if (operation.getDescription() != null && !operation.getDescription().trim().equals(summary.trim())) {
            description = operation.getDescription();
        }

        writer.printf(
                """
                    [#apidoc-%s-%s]
                    == Method: %s

                    HTTP `%s`

                    Description:

                    %s
                    %s

                    Parameters:

                    """,
                tag,
                entry.anchor(),
                operation.getOperationId(),
                entry.method(),
                summary,
                description
        );

        if (isSecurityRequired(operation)) {
            writer.println("* [.string]#string#  sessionKey\n");
        }

        Map<String, Schema> allProps = getAllPossibleProperties(operation);
        for (String paramName : entry.activeParams()) {
            Schema schema = allProps.get(paramName);
            if (schema != null) {
                String type;
                if ("array".equals(schema.getType())) {
                    type = "[.array]#string array#";
                }
                else {
                    type = "[." + displayType(schema) + "]#" + displayType(schema) + "#";
                }
                String descriptionText = findDescription(schema);
                writer.printf(
                        "* %s  %s%s%n%n",
                        type,
                        paramName,
                        descriptionText.isEmpty() ? "" : " - " + descriptionText
                );
            }
        }

        writer.println("\nReturns:\n");
        writeReturn(writer, operation);
        writer.print("\n\n\n");
    }

    private boolean isSecurityRequired(Operation operation) {
        if (operation.getSecurity() != null) {
            return !operation.getSecurity().isEmpty();
        }
        return openAPI.getSecurity() != null && !openAPI.getSecurity().isEmpty();
    }

    private void writeReturn(PrintWriter writer, Operation operation) {
        var responses = operation.getResponses();
        if (responses == null) {
            return;
        }
        ApiResponse successResponse = getSuccessResponse(responses);
        if (successResponse == null || successResponse.getContent() == null) {
            return;
        }

        var jsonContent = successResponse.getContent().get("application/json");
        if (jsonContent == null || jsonContent.getSchema() == null) {
            return;
        }
        Schema<?> schema = resolveSchemaReference(jsonContent.getSchema());
        String refName = "";

        if (schema.getProperties() != null && schema.getProperties().containsKey("result")) {
            Schema<?> resultSchema = (Schema<?>) schema.getProperties().get("result");
            refName = extractRefName(resultSchema.get$ref());
            schema = resolveSchemaReference(resultSchema);
        }

        if ("array".equals(schema.getType()) && schema.getItems() != null) {
            writeArrayReturn(writer, schema, successResponse);
            return;
        }

        if (isSimpleType(schema)) {
            writeSimpleReturn(writer, schema, successResponse, operation);
            return;
        }

        printStruct(writer, schema, 0, refName);
    }

    private void writeArrayReturn(PrintWriter writer, Schema<?> schema, ApiResponse successResponse) {
        Schema<?> itemSchema = schema.getItems();
        Schema<?> resolved = resolveSchemaReference(itemSchema);
        String itemRefName = itemSchema.get$ref() != null ? extractRefName(itemSchema.get$ref()) : "";

        if (resolved != null && isSimpleType(resolved)) {
            writeSimpleArrayReturn(writer, resolved, successResponse);
            return;
        }

        writer.println("* [.array]#array# :");
        writer.printf("    * [.struct]#struct#  %s%n", itemRefName);
        printArrayStructProperties(writer, resolved);
        writer.println();
    }

    private void writeSimpleArrayReturn(PrintWriter writer, Schema<?> resolved, ApiResponse successResponse) {
        String itemType = displayType(resolved);
        String label = responseDescription(successResponse);
        if (!label.isEmpty()) {
            writer.printf("* [.array]#%s array#  %s%n", itemType, label);
            return;
        }
        writer.println("* [.array]#array# :");
        writer.printf("    * [.%s]#%s#%n", itemType, itemType);
        writer.println();
    }

    private void printArrayStructProperties(PrintWriter writer, Schema<?> resolved) {
        if (resolved == null || resolved.getProperties() == null) {
            return;
        }
        resolved.getProperties().forEach((name, prop) -> {
            Schema<?> propertySchema = prop;
            String propDesc = propertySchema.getDescription() != null ?
                    " - " + propertySchema.getDescription() : "";
            writer.printf("** [.string]#string#  \"%s\"%s%n", name, propDesc);
        });
    }

    private void writeSimpleReturn(PrintWriter writer, Schema<?> schema,
                                   ApiResponse successResponse, Operation operation) {
        String displayType = displayType(schema);
        String label = Optional.of(responseDescription(successResponse))
                .filter(d -> !d.isBlank())
                .orElseGet(() -> operation.getOperationId()
                        .replace("get", "")
                        .replaceAll("([a-z])([A-Z])", "$1 $2")
                        .toLowerCase().trim());

        writer.printf("* [.%s]#%s#  %s%n ", displayType, displayType, label);
    }

    private Schema<?> resolveSchemaReference(Schema<?> schema) {
        if (schema == null || schema.get$ref() == null) {
            return schema;
        }
        return resolveSchema(schema.get$ref());
    }

    private String responseDescription(ApiResponse response) {
        return Optional.ofNullable(response.getDescription())
                .filter(d -> !d.isBlank())
                .orElse("");
    }

    private String displayType(Schema<?> schema) {
        return "integer".equals(schema.getType()) ? "int" : schema.getType();
    }

    private ApiResponse getSuccessResponse(ApiResponses responses) {
        ApiResponse response = responses.get("200");
        if (response != null) {
            return response;
        }
        for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
            if (entry.getKey().startsWith("2")) {
                return entry.getValue();
            }
        }
        return responses.get("default");
    }

    private void printStruct(PrintWriter writer, Schema<?> schema, int indent, String forcedLabel) {
        if (schema == null) {
            return;
        }
        String prefix = " ".repeat(indent);
        String marker = indent == 0 ? "*" : "**";

        String label = "";
        if (forcedLabel != null && !forcedLabel.isEmpty()) {
            label = forcedLabel;
        }
        if (label.isEmpty() && schema.getAdditionalProperties() != null) {
            label = "namespace";
        }

        if (schema.getProperties() != null || schema.getAdditionalProperties() != null) {
            writer.printf("%s%s [.struct]#struct#  %s%n", prefix, marker, label);
        }

        if (schema.getProperties() != null) {
            schema.getProperties().forEach((name, property) -> {
                Schema<?> propertySchema = (Schema<?>) property;
                String propertyDescription = "";
                if (propertySchema.getDescription() != null) {
                    propertyDescription = " - " + propertySchema.getDescription();
                }
                writer.printf("%s** [.string]#string#  \"%s\"%s%n", prefix, name, propertyDescription);
            });
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> inner) {
            Schema<?> resolvedInner = inner.get$ref() != null ? resolveSchema(inner.get$ref()) : inner;
            if (resolvedInner.getProperties() != null) {
                resolvedInner.getProperties().forEach((name, property) -> {
                    Schema<?> propertySchema = (Schema<?>) property;
                    String propertyDescription = "";
                    if (propertySchema.getDescription() != null) {
                        propertyDescription = " - " + propertySchema.getDescription();
                    }
                    writer.printf("%s** [.string]#string#  \"%s\"%s%n", prefix, name, propertyDescription);
                });
            }
            else if (isSimpleType(resolvedInner)) {
                String description = "";
                if (resolvedInner.getDescription() != null) {
                    description = " - " + resolvedInner.getDescription();
                }
                writer.printf("%s** [.string]#string#%s%n", prefix, description);
            }
        }
    }

    private String extractRefName(String ref) {
        if (ref == null) {
            return null;
        }
        String name = ref.substring(ref.lastIndexOf('/') + 1);
        return name.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
    }

    private boolean isSimpleType(Schema<?> schema) {
        String type = schema.getType();
        return "string".equals(type) || "integer".equals(type) ||
                "boolean".equals(type) || "number".equals(type);
    }

    private List<String> getFieldsByRequirement(Operation operation, boolean requiredOnly) {
        List<String> fields = new ArrayList<>();
        if (operation.getParameters() != null) {
            operation.getParameters().stream()
                    .filter(parameter -> Boolean.TRUE.equals(parameter.getRequired()) == requiredOnly)
                    .map(Parameter::getName)
                    .forEach(fields::add);
        }
        Schema<?> bodySchema = getBodySchema(operation);
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

    private Map<String, Schema> getAllPossibleProperties(Operation operation) {
        Map<String, Schema> props = new LinkedHashMap<>();
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                Schema schema = parameter.getSchema();
                if (schema != null && schema.getDescription() == null && parameter.getDescription() != null) {
                    schema.setDescription(parameter.getDescription());
                }
                props.put(parameter.getName(), schema);
            }
        }
        Schema<?> body = getBodySchema(operation);
        if (body != null && body.getProperties() != null) {
            props.putAll((Map<String, Schema>) body.getProperties());
        }
        return props;
    }

    private Schema<?> resolveSchema(String ref) {
        if (ref == null || openAPI.getComponents() == null) {
            return null;
        }
        return openAPI.getComponents().getSchemas().get(ref.substring(ref.lastIndexOf('/') + 1));
    }

    private Schema<?> getBodySchema(Operation operation) {
        try {
            var mediaType = operation.getRequestBody().getContent().get("application/json");
            Schema<?> schema = mediaType.getSchema();
            return schema.get$ref() != null ? resolveSchema(schema.get$ref()) : schema;
        }
        catch (Exception e) {
            return null;
        }
    }

    private String getTagDesc(String tag) {
        return Optional.ofNullable(openAPI.getTags()).orElse(List.of()).stream()
                .filter(openApiTag -> openApiTag.getName().equals(tag))
                .map(io.swagger.v3.oas.models.tags.Tag::getDescription)
                .findFirst()
                .orElse("");
    }

    private String findDescription(Schema<?> schema) {
        if (schema.getDescription() != null) {
            return schema.getDescription();
        }
        if (schema.get$ref() != null) {
            Schema<?> resolved = resolveSchema(schema.get$ref());
            if (resolved != null && resolved.getDescription() != null) {
                return resolved.getDescription();
            }
        }
        return schema.getItems() != null ? findDescription(schema.getItems()) : "";
    }

    private record DocEntry(String method, String anchor, Operation operation, List<String> activeParams) {

        static DocEntry create(String method, Operation operation, List<String> params, boolean securityRequired) {
            String suffix = String.join("-", params);
            String authPart = securityRequired ? "loggedInUser" : "";

            List<String> anchorParts = new ArrayList<>();
            anchorParts.add(operation.getOperationId());
            if (!authPart.isEmpty()) {
                anchorParts.add(authPart);
            }
            if (!suffix.isEmpty()) {
                anchorParts.add(suffix);
            }

            String anchor = String.join("-", anchorParts);
            if (authPart.isEmpty() && suffix.isEmpty()) {
                anchor += "-";
            }

            return new DocEntry(method, anchor, operation, List.copyOf(params));
        }
    }
}
