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
import java.io.StringWriter;
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

    /** Bullet level the doclet uses for the properties of a struct. */
    private static final int STRUCT_PROPERTY_LEVEL = 2;

    /** Bullet level the doclet uses for the element struct of an array-valued parameter. */
    private static final int PARAMETER_ELEMENT_STRUCT_LEVEL = 2;

    /** Bullet level the doclet uses for the documented values of a parameter. */
    private static final int PARAMETER_OPTION_LEVEL = 2;

    private static final int RETURN_OPTION_LEVEL = 2;

    /** OpenAPI type name for an array-valued schema. */
    private static final String ARRAY_TYPE = "array";

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
        for (Map.Entry<String, String> entry : generateDocumentation().entrySet()) {
            Path filePath = pathDir.resolve(entry.getKey() + ".adoc");
            Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Generates AsciiDoc documentation in memory for all tags present in the OpenAPI specification.
     *
     * @return generated documentation mapped by tag
     */
    public Map<String, String> generateDocumentation() {
        Map<String, List<DocEntry>> taggedOps = new TreeMap<>();

        openAPI.getPaths().forEach((path, pathItem) -> {
            if (pathItem.getGet() != null) {
                processOperation("GET", pathItem.getGet(), taggedOps);
            }
            if (pathItem.getPost() != null) {
                processOperation("POST", pathItem.getPost(), taggedOps);
            }
        });

        Map<String, String> documents = new TreeMap<>();
        for (Map.Entry<String, List<DocEntry>> entry : taggedOps.entrySet()) {
            documents.put(entry.getKey(), renderAdocFile(entry.getKey(), entry.getValue()));
        }
        return documents;
    }

    private void processOperation(String method, Operation operation, Map<String, List<DocEntry>> operationsByTag) {
        if (operation == null || operation.getTags() == null || operation.getTags().isEmpty()) {
            return;
        }
        String tag = operation.getTags().get(0);

        List<String> required = getFieldsByRequirement(operation, true);
        List<String> optional = getFieldsByRequirement(operation, false);

        List<DocEntry> entries = operationsByTag.computeIfAbsent(tag, key -> new ArrayList<>());

        for (List<String> params : UyuniSwaggerReader.expandOverloads(operation, required, optional)) {
            Operation documentedByOverload = UyuniSwaggerReader.operationForCall(operation, params);
            entries.add(DocEntry.create(method, operation, documentedByOverload, params,
                    isSecurityRequired(documentedByOverload)));
        }
    }

    private String renderAdocFile(String tag, List<DocEntry> entries) {
        StringWriter buffer = new StringWriter();
        try (PrintWriter writer = new PrintWriter(buffer)) {
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
        return buffer.toString();
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
                Boolean.TRUE.equals(entry.documentedByOverload().getDeprecated()) ?
                        operation.getOperationId() + " (Deprecated)" : operation.getOperationId(),
                entry.method(),
                summary,
                description
        );

        if (isSecurityRequired(entry.documentedByOverload())) {
            writer.println("* [.string]#string#  sessionKey\n");
        }

        // A call takes the parameters of the overload documenting it, which describes the ones it
        // shares with the others as its own signature declares them.
        Map<String, Schema> allProps = getAllPossibleProperties(operation);
        allProps.putAll(getAllPossibleProperties(entry.documentedByOverload()));
        for (String paramName : entry.activeParams()) {
            Schema schema = allProps.get(paramName);
            if (schema != null) {
                writeParameter(writer, paramName, schema);
            }
        }

        writer.println("\nReturns:\n");
        writeReturn(writer, operation, entry.activeParams());
        writer.print("\n\n\n");
    }

    private void writeParameter(PrintWriter writer, String paramName, Schema<?> schema) {
        String descriptionText = findDescription(schema);
        String suffix = descriptionText.isEmpty() ? "" : " - " + descriptionText;
        Schema<?> resolved = resolveSchemaReference(schema);

        if (hasProperties(resolved)) {
            String label = legacyDocType(schema);
            writer.printf("* [.%s]#%s#  %s%s%n%n", label.isEmpty() ? "struct" : label,
                    label.isEmpty() ? "struct" : label, paramName, suffix);
            printStructProperties(writer, resolved);
            writer.println();
            return;
        }

        writer.printf("* %s  %s%s%n", parameterType(schema), paramName, suffix);
        printOptions(writer, resolved == null ? schema : resolved, PARAMETER_OPTION_LEVEL);
        writeParameterElement(writer, resolved == null ? schema : resolved);
        writer.println();
    }

    /**
     * Writes the element type of an array parameter.
     *
     * The doclet expands the body of {@code #array_begin} into a nested item, one level deeper
     * than the equivalent expansion in a return value. Struct elements bring their properties
     * with them; simple elements are a single item labelled by the legacy element name.
     */
    private void writeParameterElement(PrintWriter writer, Schema<?> schema) {
        if (!nestsElement(schema)) {
            return;
        }
        Schema<?> resolvedItems = resolveSchemaReference(schema.getItems());
        if (hasProperties(resolvedItems)) {
            printElementStruct(writer, schema, PARAMETER_ELEMENT_STRUCT_LEVEL);
            return;
        }
        String itemType = displayType(resolvedItems);
        writer.printf("%s [.%s]#%s#  %s%n", "*".repeat(PARAMETER_ELEMENT_STRUCT_LEVEL),
                itemType, itemType, legacyDocName(schema));
    }

    /**
     * Tells whether an array documents its element type as a nested item.
     *
     * The doclet compiles a struct element, and a scalar element that carries a legacy element
     * name, into {@code #array_begin}, which renders a bare {@code array} and nests the element
     * below it. A plain scalar array compiles to {@code #array_single}, which renders the element
     * type inline as {@code "<type> array"} and nests nothing.
     *
     * @param schema the array schema
     * @return true if the element is documented as a nested item
     */
    private boolean nestsElement(Schema<?> schema) {
        if (!ARRAY_TYPE.equals(schema.getType()) || schema.getItems() == null) {
            return false;
        }
        Schema<?> resolvedItems = resolveSchemaReference(schema.getItems());
        if (resolvedItems == null) {
            return false;
        }
        return hasProperties(resolvedItems) ||
                (isSimpleType(resolvedItems) && !legacyDocName(schema).isEmpty());
    }

    private boolean hasProperties(Schema<?> schema) {
        return schema != null && schema.getProperties() != null && !schema.getProperties().isEmpty();
    }

    private String parameterType(Schema<?> schema) {
        String legacyType = legacyDocType(schema);
        if (!legacyType.isEmpty()) {
            return "[." + legacyType + "]#" + legacyType + "#";
        }
        if (ARRAY_TYPE.equals(schema.getType())) {
            // An element rendered as a nested item leaves the parent a bare "array", exactly as
            // #array_begin does; otherwise the element type is shown inline by #array_single.
            String type = nestsElement(schema) ? ARRAY_TYPE : structPropertyType(schema);
            return "[.array]#" + type + "#";
        }
        return "[." + displayType(schema) + "]#" + displayType(schema) + "#";
    }

    private String legacyDocType(Schema<?> schema) {
        if (schema.getExtensions() == null) {
            return "";
        }
        Object value = schema.getExtensions().get(UyuniSwaggerReader.DOC_RESPONSE_TYPE_EXTENSION);
        return value == null ? "" : value.toString();
    }

    private boolean isSecurityRequired(Operation operation) {
        if (operation.getSecurity() != null) {
            return !operation.getSecurity().isEmpty();
        }
        return openAPI.getSecurity() != null && !openAPI.getSecurity().isEmpty();
    }

    private void writeReturn(PrintWriter writer, Operation operation, List<String> activeParams) {
        var responses = UyuniSwaggerReader.operationForCall(operation, activeParams).getResponses();
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
        LegacyDocResponseData legacyDocResponse = getLegacyDocResponse(successResponse);
        Schema<?> docSchema = legacyDocResponse.schema();
        String responseDescription = responseDescription(successResponse);
        String responseLabel = legacyDocResponse.label(responseDescription).orElse(responseDescription);
        if (docSchema != null) {
            refName = docSchema.get$ref() != null ? extractRefName(docSchema.get$ref()) : "";
            schema = resolveSchemaReference(docSchema);
        }

        if (docSchema == null && schema.getProperties() != null && schema.getProperties().containsKey("result")) {
            Schema<?> resultSchema = (Schema<?>) schema.getProperties().get("result");
            refName = extractRefName(resultSchema.get$ref());
            schema = resolveSchemaReference(resultSchema);
        }

        if (ARRAY_TYPE.equals(schema.getType()) && schema.getItems() != null) {
            writeArrayReturn(writer, schema, responseLabel, legacyDocResponse.name());
            return;
        }

        // A declared legacy type describes the whole return value, so it also applies to schemas
        // that are not simple: the doclet renders those as a single typed line, without expanding
        // the schema.
        if (isSimpleType(schema) || !legacyDocResponse.type().isBlank()) {
            writeSimpleReturn(writer, schema, responseLabel, legacyDocResponse.type(), operation,
                    legacyDocResponse.plainText(), legacyDocResponse.values());
            return;
        }

        printStruct(writer, schema, 0, refName, responseLabel);
    }

    private LegacyDocResponseData getLegacyDocResponse(ApiResponse response) {
        if (response.getExtensions() == null) {
            return LegacyDocResponseData.EMPTY;
        }
        Object schema = response.getExtensions().get(UyuniSwaggerReader.DOC_RESPONSE_SCHEMA_EXTENSION);
        String type = extensionString(response, UyuniSwaggerReader.DOC_RESPONSE_TYPE_EXTENSION);
        String name = extensionString(response, UyuniSwaggerReader.DOC_RESPONSE_NAME_EXTENSION);
        return new LegacyDocResponseData(
                schema instanceof Schema<?> docSchema ? docSchema : null,
                type,
                name,
                Boolean.TRUE.equals(response.getExtensions().get(UyuniSwaggerReader.DOC_RESPONSE_PLAIN_TEXT_EXTENSION)),
                response.getExtensions().get(UyuniSwaggerReader.DOC_RESPONSE_VALUES_EXTENSION)
                        instanceof Schema<?> values ? values : null
        );
    }

    private String extensionString(ApiResponse response, String extensionName) {
        Object value = response.getExtensions().get(extensionName);
        return value instanceof String stringValue ? stringValue : "";
    }

    private void writeArrayReturn(PrintWriter writer, Schema<?> schema, String responseLabel,
                                  String legacyStructLabel) {
        Schema<?> itemSchema = schema.getItems();
        Schema<?> resolved = resolveSchemaReference(itemSchema);
        String itemRefName = itemSchema.get$ref() != null ? extractRefName(itemSchema.get$ref()) : "";

        if (resolved != null && isSimpleType(resolved)) {
            writeSimpleArrayReturn(writer, resolved, responseLabel);
            return;
        }

        writer.println("* [.array]#array# :");
        writer.printf("    * [.struct]#struct#  %s%n",
                legacyStructLabel.isEmpty() ? itemRefName : legacyStructLabel);
        printStructProperties(writer, resolved);
        writer.println();
    }

    private void writeSimpleArrayReturn(PrintWriter writer, Schema<?> resolved, String responseLabel) {
        String itemType = displayType(resolved);
        String label = responseLabel;
        if (!label.isEmpty()) {
            writer.printf("* [.array]#%s array#  %s%n", itemType, label);
            return;
        }
        writer.println("* [.array]#array# :");
        writer.printf("    * [.%s]#%s#%n", itemType, itemType);
        writer.println();
    }

    private void printStructProperties(PrintWriter writer, Schema<?> resolved) {
        printStructProperties(writer, resolved, STRUCT_PROPERTY_LEVEL);
    }

    private void printStructProperties(PrintWriter writer, Schema<?> resolved, int level) {
        if (resolved == null || resolved.getProperties() == null) {
            return;
        }
        resolved.getProperties().forEach((name, prop) -> {
            Schema<?> nested = resolveNestedStruct(prop);
            // An array property spends its legacy name on the element struct, so only a scalar or
            // struct property renames itself with it.
            String label = ARRAY_TYPE.equals(prop.getType()) || legacyDocName(prop).isEmpty() ?
                    name : legacyDocName(prop);
            writeStructProperty(writer, "", label, prop, level);
            printOptions(writer, prop, level + 1);
            printElementStruct(writer, prop, level + 1);
            printSimpleElement(writer, prop, level);
            // A serializer referenced by a struct property brings its own struct label, which the
            // doclet renders as a sibling of the property before the fields it introduces.
            if (nested != null && !legacyDocName(nested).isEmpty()) {
                writer.printf("%s [.struct]#struct#  %s%n", "*".repeat(level), legacyDocName(nested));
            }
            printStructProperties(writer, nested, level + 1);
        });
    }

    /**
     * Resolves a property that documents a nested struct, or {@code null} for any other property.
     *
     * The doclet expands a struct valued property into its own properties one level below the
     * property itself. Array properties are handled by {@link #printElementStruct} instead.
     *
     * @param property the property schema
     * @return the resolved schema of the nested struct, or null if the property is not one
     */
    private Schema<?> resolveNestedStruct(Schema<?> property) {
        if (ARRAY_TYPE.equals(property.getType())) {
            return null;
        }
        Schema<?> resolved = resolveSchemaReference(property);
        if (resolved == null || resolved.getProperties() == null || resolved.getProperties().isEmpty()) {
            return null;
        }
        return resolved;
    }

    /**
     * Writes the element type of an array property that holds structs.
     *
     * The doclet expands the {@code $Serializer} reference inside {@code #prop_array_begin} into
     * the element struct followed by its properties, in the same shape it uses for an array return
     * value.
     */
    private void printElementStruct(PrintWriter writer, Schema<?> property, int level) {
        if (!ARRAY_TYPE.equals(property.getType())) {
            return;
        }
        Schema<?> items = property.getItems();
        if (items == null) {
            return;
        }
        Schema<?> resolvedItems = resolveSchemaReference(items);
        if (resolvedItems == null || resolvedItems.getProperties() == null ||
                resolvedItems.getProperties().isEmpty()) {
            return;
        }
        String label = legacyDocName(property);
        if (label.isEmpty()) {
            label = items.get$ref() != null ? extractRefName(items.get$ref()) : "";
        }
        writer.printf("%s [.struct]#struct#  %s%n", "*".repeat(level), label);
        printStructProperties(writer, resolvedItems, level + 1);
    }

    /**
     * Writes the named element of an array property that holds a simple type.
     *
     * The doclet names such an element with {@code #array_single}, which it renders as an item of
     * its own beside a property documented as a bare array. A property documenting the element
     * type on itself, with {@code #prop_array}, names the element there and shows no such item,
     * so only a property declaring the bare array type carries one.
     *
     * @param writer the writer to write to
     * @param property the array property schema
     * @param level the bullet level of the property
     */
    private void printSimpleElement(PrintWriter writer, Schema<?> property, int level) {
        String label = legacyDocName(property);
        if (!ARRAY_TYPE.equals(legacyDocType(property)) || label.isEmpty()) {
            return;
        }
        Schema<?> items = resolveSchemaReference(property.getItems());
        if (items == null || !isSimpleType(items)) {
            return;
        }
        writer.printf("%s [.array]#%s array#  %s%n", "*".repeat(level), structPropertyType(items), label);
    }

    /**
     * Writes the documented values of a parameter or property, one bullet level below it.
     *
     * The doclet renders {@code #options()} as a plain bullet list carrying no type marker, and
     * appends the description after a dash for the {@code #item_desc} form.
     *
     * @param writer the writer to write to
     * @param schema the parameter or property schema
     * @param level the bullet level of the values
     */
    private void printOptions(PrintWriter writer, Schema<?> schema, int level) {
        Schema<?> documented = optionsSchema(schema);
        if (documented == null || documented.getEnum() == null) {
            return;
        }
        Map<String, String> descriptions = optionDescriptions(documented);
        for (Object value : documented.getEnum()) {
            String option = String.valueOf(value);
            String description = descriptions.getOrDefault(option, "");
            writer.printf("%s %s%s%n", "*".repeat(level), option,
                    description.isEmpty() ? "" : " - " + description);
        }
    }

    /**
     * Resolves the schema that carries the documented values.
     *
     * An array documents the values of its element type; every other parameter or property
     * documents its own.
     *
     * @param schema the parameter or property schema
     * @return the schema holding the values, or null when there is none
     */
    private Schema<?> optionsSchema(Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        return ARRAY_TYPE.equals(schema.getType()) && schema.getItems() != null ?
                resolveSchemaReference(schema.getItems()) : schema;
    }

    /**
     * Reads the descriptions the namespace documented for individual values.
     *
     * @param schema the schema holding the values
     * @return the description of each value, empty when the values carry none
     */
    private Map<String, String> optionDescriptions(Schema<?> schema) {
        Object descriptions = schema.getExtensions() == null ? null :
                schema.getExtensions().get(UyuniSwaggerReader.DOC_OPTION_DESCRIPTIONS_EXTENSION);
        if (!(descriptions instanceof Map<?, ?> documented)) {
            return Map.of();
        }
        Map<String, String> byOption = new LinkedHashMap<>();
        documented.forEach((option, description) ->
                byOption.put(String.valueOf(option), String.valueOf(description)));
        return byOption;
    }

    private String legacyDocName(Schema<?> schema) {
        if (schema.getExtensions() == null) {
            return "";
        }
        Object value = schema.getExtensions().get(UyuniSwaggerReader.DOC_RESPONSE_NAME_EXTENSION);
        return value == null ? "" : value.toString();
    }

    private void writeSimpleReturn(PrintWriter writer, Schema<?> schema, String responseLabel,
                                   String legacyType, Operation operation, boolean plainText,
                                   Schema<?> documentedValues) {
        String displayType = legacyType.isBlank() ? displayType(schema) : legacyType;
        Schema<?> values = documentedValues == null ? schema : documentedValues;
        // The doclet passes a return value documented without a macro through as written, so it
        // carries neither the type role nor a label synthesised from the operation.
        if (plainText) {
            writer.printf("* %s%s %n", displayType, responseLabel.isBlank() ? "" : " - " + responseLabel);
            printOptions(writer, values, RETURN_OPTION_LEVEL);
            writer.print(" ");
            return;
        }

        String label = Optional.of(responseLabel)
                .filter(d -> !d.isBlank())
                .orElseGet(() -> operation.getOperationId()
                        .replace("get", "")
                        .replaceAll("([a-z])([A-Z])", "$1 $2")
                        .toLowerCase().trim());

        writer.printf("* [.%s]#%s#  %s%n", displayType, displayType, label);
        printOptions(writer, values, RETURN_OPTION_LEVEL);
        writer.print(" ");
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

    private record LegacyDocResponseData(Schema<?> schema, String type, String name, boolean plainText,
                                         Schema<?> values) {
        private static final LegacyDocResponseData EMPTY =
                new LegacyDocResponseData(null, "", "", false, null);

        Optional<String> label(String description) {
            if (name.isBlank()) {
                return Optional.empty();
            }
            if (description.isBlank()) {
                return Optional.of(name);
            }
            return Optional.of(name + " - " + description);
        }
    }

    private String displayType(Schema<?> schema) {
        if ("integer".equals(schema.getType())) {
            return "int";
        }
        // The doclet binds $date to its own type name, whichever value carries the date.
        if ("string".equals(schema.getType()) && "date-time".equals(schema.getFormat())) {
            return UyuniSwaggerReader.LEGACY_DATE_TYPE;
        }
        return schema.getType();
    }

    /**
     * Resolves the legacy type name of a struct property, mirroring the doclet macros:
     * {@code #type($t)} renders {@code [.$t]#$t#} and {@code #atype($t)} renders
     * {@code [.array]#$t array#}, with {@code $date} bound to {@code dateTime.iso8601}.
     */
    private String structPropertyType(Schema<?> schema) {
        // A declared legacy type has no OpenAPI counterpart to derive, so it wins over the
        // resolved type.
        String legacyType = legacyDocType(schema);
        if (!legacyType.isEmpty()) {
            return legacyType;
        }
        String type = schema.getType();
        if ("integer".equals(type)) {
            return "int";
        }
        if ("string".equals(type)) {
            return "date-time".equals(schema.getFormat()) ? UyuniSwaggerReader.LEGACY_DATE_TYPE : type;
        }
        if ("boolean".equals(type) || "number".equals(type)) {
            return type;
        }
        if (ARRAY_TYPE.equals(type)) {
            // #prop_array renders the element type ("string array"); #prop_array_begin, which
            // is what a non-scalar element compiles to, renders a bare "array".
            Schema<?> items = resolveSchemaReference(schema.getItems());
            return items != null && isSimpleType(items) ? structPropertyType(items) + " array" : ARRAY_TYPE;
        }
        if ("object".equals(type) || schema.get$ref() != null || schema.getProperties() != null) {
            return "struct";
        }
        return "string";
    }

    /**
     * The AsciiDoc marker differs from the rendered type only for arrays, where the doclet
     * emits {@code [.array]} but shows the element type.
     */
    private String structPropertyMarker(Schema<?> schema) {
        return ARRAY_TYPE.equals(schema.getType()) ? ARRAY_TYPE : structPropertyType(schema);
    }

    private void writeStructProperty(PrintWriter writer, String prefix, String name, Schema<?> schema) {
        writeStructProperty(writer, prefix, name, schema, STRUCT_PROPERTY_LEVEL);
    }

    private void writeStructProperty(PrintWriter writer, String prefix, String name, Schema<?> schema, int level) {
        String description = schema.getDescription() != null ? " - " + schema.getDescription() : "";
        writer.printf("%s%s [.%s]#%s#  \"%s\"%s%n", prefix, "*".repeat(level), structPropertyMarker(schema),
                structPropertyType(schema), name, description);
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

    private void printStruct(PrintWriter writer, Schema<?> schema, int indent, String forcedLabel,
                             String responseDescription) {
        if (schema == null) {
            return;
        }
        String prefix = " ".repeat(indent);
        String marker = indent == 0 ? "*" : "**";

        String label = "";
        if (responseDescription != null && !responseDescription.isEmpty()) {
            label = responseDescription;
        }
        else if (forcedLabel != null && !forcedLabel.isEmpty()) {
            label = forcedLabel;
        }
        if (label.isEmpty() && schema.getAdditionalProperties() != null) {
            label = "map";
        }

        if (schema.getProperties() != null || schema.getAdditionalProperties() != null) {
            writer.printf("%s%s [.struct]#struct#  %s%n", prefix, marker, label);
        }

        if (schema.getProperties() != null) {
            printStructProperties(writer, schema);
        }

        if (schema.getAdditionalProperties() instanceof Schema<?> inner) {
            Schema<?> resolvedInner = inner.get$ref() != null ? resolveSchema(inner.get$ref()) : inner;
            while (resolvedInner.getProperties() == null &&
                    resolvedInner.getAdditionalProperties() instanceof Schema<?> nested) {
                resolvedInner = nested.get$ref() != null ? resolveSchema(nested.get$ref()) : nested;
            }
            if (resolvedInner.getProperties() != null) {
                resolvedInner.getProperties().forEach((name, property) ->
                        writeStructProperty(writer, prefix, name, property));
            }
            else if (isSimpleType(resolvedInner)) {
                String description = "";
                if (resolvedInner.getDescription() != null) {
                    description = " - " + resolvedInner.getDescription();
                }
                String innerType = structPropertyType(resolvedInner);
                writer.printf("%s** [.%s]#%s#%s%n", prefix, innerType, innerType, description);
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

    private record DocEntry(String method, String anchor, Operation operation,
                           Operation documentedByOverload, List<String> activeParams) {

        static DocEntry create(String method, Operation operation, Operation documentedByOverload,
                               List<String> params, boolean securityRequired) {
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

            return new DocEntry(method, anchor, operation, documentedByOverload, List.copyOf(params));
        }
    }
}
