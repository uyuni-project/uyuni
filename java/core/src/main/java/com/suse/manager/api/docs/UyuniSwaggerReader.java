/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.docs;

import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import spark.route.HttpMethod;

/**
 * Builds an OpenAPI specification from annotated XML-RPC handlers.
 */
public class UyuniSwaggerReader {

    private static final Logger LOG = LogManager.getLogger(UyuniSwaggerReader.class);

    public static final String DOC_RESPONSE_SCHEMA_EXTENSION = "x-uyuni-doc-response-schema";
    public static final String DOC_RESPONSE_TYPE_EXTENSION = "x-uyuni-doc-response-type";
    public static final String DOC_RESPONSE_NAME_EXTENSION = "x-uyuni-doc-response-name";

    public static final String DOC_RESPONSE_UNNAMED_EXTENSION = "x-uyuni-doc-response-unnamed";
    /** Parameter counts of the handler overloads an operation stands for, longest first. */
    public static final String DOC_OVERLOAD_ARITIES_EXTENSION = "x-uyuni-doc-overload-arities";

    public static final String DOC_OVERLOAD_SHAPES_EXTENSION = "x-uyuni-doc-overload-shapes";

    /** Extension holding the description the namespace documented for each allowed value. */
    public static final String DOC_OPTION_DESCRIPTIONS_EXTENSION = "x-uyuni-doc-option-descriptions";
    public static final String DEFAULT_MEDIA_TYPE = "application/json";
    public static final String HTTP_200 = "200";
    /** Legacy name for a date, bound as {@code $date} in the doclet's macros for both formats. */
    public static final String LEGACY_DATE_TYPE = "dateTime.iso8601";

    private final OpenAPI openAPI;
    private final Components components;

    /**
     * Creates a reader with the base Uyuni OpenAPI metadata.
     */
    public UyuniSwaggerReader() {
        this.openAPI = new OpenAPI();
        this.components = new Components();
        this.openAPI.setComponents(components);
        this.openAPI.setPaths(new Paths());

        this.openAPI.setInfo(new Info().title("Uyuni API Docs").description("Welcome to the Uyuni API."));

        this.components.addSecuritySchemes("session", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("pxt-session-cookie")
                .in(SecurityScheme.In.COOKIE));
    }

    /**
     * Reads the given handler class and merges its operations into the OpenAPI specification.
     *
     * @param cls handler class
     * @param namespace API namespace
     * @return generated OpenAPI specification
     */
    public OpenAPI read(Class<?> cls, String namespace) {
        Tag tagAnnotation = findClassAnnotation(cls, Tag.class);
        if (tagAnnotation == null) {
            LOG.warn("Class {} does not have @Tag annotation, skipping.", cls.getName());
            return openAPI;
        }

        openAPI.addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                .name(tagAnnotation.name())
                .description(tagAnnotation.description()));

        Arrays.stream(cls.getMethods())
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> processMethod(cls, namespace, method, tagAnnotation));

        return openAPI;
    }

    private void processMethod(Class<?> cls, String namespace, Method method, Tag tagAnnotation) {
        ApiEndpointDoc apiDoc = findMethodAnnotation(method, ApiEndpointDoc.class);
        if (apiDoc == null) {
            return;
        }

        Operation openApiOperation = createOperationWithBasicInfo(method, tagAnnotation, apiDoc);
        applyOverloadArities(cls, method, openApiOperation);
        applySecurityIfNeeded(method, openApiOperation);
        configureRequestBodyIfPresent(apiDoc, openApiOperation);
        configureResponses(apiDoc, method, openApiOperation);
        processLiteralParameters(method, openApiOperation);
        registerOperationOnPath(namespace, method, apiDoc.method(), openApiOperation);
    }

    private Operation createOperationWithBasicInfo(Method method, Tag tagAnnotation, ApiEndpointDoc apiDoc) {
        Operation operation = new Operation();
        operation.setOperationId(method.getName());
        if (findMethodAnnotation(method, Deprecated.class) != null) {
            operation.setDeprecated(true);
        }
        if (!apiDoc.summary().isEmpty()) {
            operation.setSummary(apiDoc.summary());
        }
        if (!apiDoc.description().isEmpty()) {
            operation.setDescription(apiDoc.description());
        }
        operation.addTagsItem(tagAnnotation.name());
        return operation;
    }

    /**
     * Records how many parameters each handler overload of an operation takes.
     *
     * A single operation with optional parameters stands for every overload of the handler method,
     * and the legacy doclet documents one call per overload. Which parameter combinations exist is
     * a property of the handler, not of the schema: overloads may add several parameters at once,
     * so the combinations cannot be derived from the optional parameters alone. The counts exclude
     * the logged in user, which is not a documented parameter.
     *
     * @param cls the handler class being read
     * @param method the handler method backing the operation
     * @param operation the operation being built
     */
    private void applyOverloadArities(Class<?> cls, Method method, Operation operation) {
        List<Integer> arities = Arrays.stream(cls.getMethods())
                .filter(candidate -> candidate.getName().equals(method.getName()))
                .filter(candidate -> candidate.getParameterCount() > 0 &&
                        User.class.equals(candidate.getParameterTypes()[0]))
                .map(candidate -> candidate.getParameterCount() - 1)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        if (arities.size() > 1) {
            operation.addExtension(DOC_OVERLOAD_ARITIES_EXTENSION, arities);
        }
    }

    private List<String> documentedParameters(Operation operation) {
        List<String> names = new ArrayList<>();
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(parameter -> names.add(parameter.getName()));
        }
        Schema<?> body = requestBodySchema(operation);
        if (body != null && body.getProperties() != null) {
            names.addAll(body.getProperties().keySet());
        }
        return names;
    }

    private boolean isRequired(Operation operation, String name) {
        if (operation.getParameters() != null) {
            for (var parameter : operation.getParameters()) {
                if (name.equals(parameter.getName())) {
                    return Boolean.TRUE.equals(parameter.getRequired());
                }
            }
        }
        Schema<?> body = requestBodySchema(operation);
        return body != null && body.getRequired() != null && body.getRequired().contains(name);
    }

    private Schema<?> requestBodySchema(Operation operation) {
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return null;
        }
        MediaType mediaType = operation.getRequestBody().getContent().get(DEFAULT_MEDIA_TYPE);
        if (mediaType == null || mediaType.getSchema() == null) {
            return null;
        }
        Schema<?> schema = mediaType.getSchema();
        if (schema.get$ref() == null || this.components.getSchemas() == null) {
            return schema;
        }
        return this.components.getSchemas()
                .get(schema.get$ref().substring(schema.get$ref().lastIndexOf('/') + 1));
    }

    /**
     * Lists the parameter combinations the legacy doclet documents for an operation.
     *
     * Each combination is the required parameters plus as many of the optional ones as the
     * corresponding handler overload takes, so an overload that adds several parameters at once
     * produces no intermediate documented call. Combinations are returned longest first, the order
     * the doclet uses, and an operation backed by a single overload yields a single combination.
     *
     * @param operation the operation to expand
     * @param required the parameters present in every overload
     * @param optional the parameters added by the longer overloads, in declaration order
     * @return the parameter names of each documented call
     */
    public static List<List<String>> expandOverloads(Operation operation, List<String> required,
                                                     List<String> optional) {
        List<List<String>> resolved = overloadShapes(operation);
        if (resolved != null) {
            return resolved;
        }

        List<List<String>> combinations = new ArrayList<>();
        for (int count : optionalParameterCounts(operation, required.size(), optional.size())) {
            List<String> params = new ArrayList<>(required);
            params.addAll(optional.subList(0, count));
            combinations.add(params);
        }
        return combinations;
    }

    /**
     * Reads the parameters of each documented call, when counting them cannot describe them.
     *
     * @param operation the operation to expand
     * @return the parameters of each documented call, or null when the counts describe them
     */
    private static List<List<String>> overloadShapes(Operation operation) {
        Object shapes = operation.getExtensions() == null ?
                null : operation.getExtensions().get(DOC_OVERLOAD_SHAPES_EXTENSION);
        if (!(shapes instanceof List<?> recorded) || recorded.isEmpty()) {
            return null;
        }
        List<List<String>> calls = new ArrayList<>();
        for (Object shape : recorded) {
            if (shape instanceof List<?> parameters) {
                calls.add(parameters.stream().map(String::valueOf).toList());
            }
        }
        return calls.isEmpty() ? null : calls;
    }

    /**
     * Resolves how many optional parameters each documented call carries.
     *
     * @param operation the operation to expand
     * @param requiredCount the number of required parameters
     * @param optionalCount the number of optional parameters
     * @return the optional parameter counts, descending
     */
    private static List<Integer> optionalParameterCounts(Operation operation, int requiredCount, int optionalCount) {
        Object arities = operation.getExtensions() == null ?
                null : operation.getExtensions().get(DOC_OVERLOAD_ARITIES_EXTENSION);

        if (arities instanceof List<?> recorded && !recorded.isEmpty()) {
            return recorded.stream()
                    .filter(Number.class::isInstance)
                    .map(arity -> ((Number) arity).intValue() - requiredCount)
                    .filter(count -> count >= 0 && count <= optionalCount)
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .toList();
        }
        return List.of(optionalCount);
    }

    private void applySecurityIfNeeded(Method method, Operation operation) {
        PublicApiEndpoint publicEndpointAnnotation = findMethodAnnotation(method, PublicApiEndpoint.class);
        if (publicEndpointAnnotation == null) {
            operation.addSecurityItem(new SecurityRequirement().addList("session"));
        }
        else {
            operation.setSecurity(new ArrayList<>());
        }
    }

    private void configureRequestBodyIfPresent(ApiEndpointDoc apiDoc, Operation operation) {
        if (apiDoc.requestClass() == Void.class) {
            return;
        }

        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(true);
        if (!apiDoc.requestDescription().isEmpty()) {
            requestBody.setDescription(apiDoc.requestDescription());
        }

        Content content = new Content();
        MediaType mediaType = new MediaType();

        resolveAndRegisterSchema(apiDoc.requestClass());
        applyLegacyDocTypes(apiDoc.requestClass());
        mediaType.setSchema(buildSchemaRef(apiDoc.requestClass()));
        content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);
        requestBody.setContent(content);
        operation.setRequestBody(requestBody);
    }

    /**
     * Carries the legacy documentation type of the documented properties into the specification.
     *
     * Some legacy types have no OpenAPI counterpart, so swagger-core normalises them to the
     * closest primitive. Annotating the getter with {@link LegacyDocResponse#type()} keeps the
     * original name available to the documentation parsers.
     *
     * A documented class describes its payload through the classes its getters return, so the
     * whole reachable graph is visited: the annotation means the same thing on a request property,
     * on a response property and on a property of a nested structure.
     *
     * @param documentedClass the request or response class of the endpoint
     */
    private void applyLegacyDocTypes(Class<?> documentedClass) {
        if (this.components.getSchemas() == null) {
            return;
        }
        applyLegacyDocTypes(documentedClass, new HashSet<>());
    }

    private void applyLegacyDocTypes(Type type, Set<Class<?>> visited) {
        if (type instanceof ParameterizedType parameterized) {
            applyLegacyDocTypes(parameterized.getRawType(), visited);
            Arrays.stream(parameterized.getActualTypeArguments())
                    .forEach(argument -> applyLegacyDocTypes(argument, visited));
            return;
        }
        if (!(type instanceof Class<?> documentedClass) || documentedClass.getPackageName().startsWith("java.") ||
                !visited.add(documentedClass)) {
            return;
        }

        stampLegacyDocTypes(documentedClass);

        Arrays.stream(documentedClass.getGenericInterfaces())
                .forEach(iface -> applyLegacyDocTypes(iface, visited));
        Arrays.stream(documentedClass.getMethods())
                .forEach(getter -> applyLegacyDocTypes(getter.getGenericReturnType(), visited));
    }

    private void stampLegacyDocTypes(Class<?> documentedClass) {
        Schema<?> schema = this.components.getSchemas().get(schemaRefName(documentedClass));
        if (schema == null || schema.getProperties() == null) {
            return;
        }
        LegacyDocResponse structDoc = documentedClass.getAnnotation(LegacyDocResponse.class);
        if (structDoc != null && !structDoc.name().isBlank()) {
            schema.addExtension(DOC_RESPONSE_NAME_EXTENSION, structDoc.name());
        }
        for (Method getter : documentedClass.getMethods()) {
            Schema<?> property = schema.getProperties().get(resolvePropertyName(getter));
            if (property == null) {
                continue;
            }
            applyDocumentedValues(property, getter);
            LegacyDocResponse legacyDoc = getter.getAnnotation(LegacyDocResponse.class);
            if (legacyDoc == null || (legacyDoc.type().isBlank() && legacyDoc.name().isBlank())) {
                continue;
            }
            if (!legacyDoc.type().isBlank()) {
                property.addExtension(DOC_RESPONSE_TYPE_EXTENSION, legacyDoc.type());
            }
            if (!legacyDoc.name().isBlank()) {
                property.addExtension(DOC_RESPONSE_NAME_EXTENSION, legacyDoc.name());
            }
        }
    }

    /**
     * Applies the values a property documents to its schema.
     *
     * The model converter builds a property schema knowing the declared Java type, and coerces
     * every documented value to it: a value the type cannot hold is reshaped, or lost. Resolving
     * the annotation without a type context, the way a documented parameter already does, keeps
     * the values as the namespace spelled them.
     *
     * @param property the property schema built for the getter
     * @param getter the getter documenting the property
     */
    private void applyDocumentedValues(Schema<?> property, Method getter) {
        var arraySchema = getter.getAnnotation(io.swagger.v3.oas.annotations.media.ArraySchema.class);
        if (arraySchema != null) {
            applyDocumentedValues(property, arraySchema.schema());
            return;
        }
        var schemaAnnotation = getter.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        if (schemaAnnotation != null) {
            applyDocumentedValues(property, schemaAnnotation);
        }
    }

    private String resolvePropertyName(Method getter) {
        var schemaAnnotation = getter.getAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        if (schemaAnnotation != null && !schemaAnnotation.name().isEmpty()) {
            return schemaAnnotation.name();
        }
        String name = getter.getName();
        if (name.startsWith("get")) {
            name = name.substring(3);
        }
        else if (name.startsWith("is")) {
            name = name.substring(2);
        }
        return Introspector.decapitalize(name);
    }

    private void configureResponses(ApiEndpointDoc apiDoc, Method method, Operation operation) {
        ApiResponses apiResponses = new ApiResponses();

        if (apiDoc.isIntegerResponse()) {
            apiResponses.addApiResponse(HTTP_200,
                    addLegacyDocResponse(apiDoc, createIntegerResponse(apiDoc.responseDescription())));
            operation.setResponses(apiResponses);
            return;
        }

        if (apiDoc.responseClass() != Void.class) {
            Class<?> responseClass = apiDoc.responseClass();
            if (responseClass == String.class || responseClass == Integer.class || responseClass == Boolean.class) {
                ApiResponse response = new ApiResponse().description(
                        apiDoc.responseDescription().isEmpty() ? "Success" : apiDoc.responseDescription()
                );
                Content content = new Content();
                MediaType mediaType = new MediaType();

                Schema<?> schema = switch (responseClass.getSimpleName()) {
                    case "String" -> new StringSchema();
                    case "Integer" -> new IntegerSchema();
                    case "Boolean" -> new BooleanSchema();
                    default -> new StringSchema();
                };

                var responseSchemaAnnotation =
                        findMethodAnnotation(method, io.swagger.v3.oas.annotations.media.Schema.class);
                if (responseSchemaAnnotation != null) {
                    applyDocumentedValues(schema, responseSchemaAnnotation);
                }

                mediaType.setSchema(schema);
                content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);
                response.setContent(content);
                apiResponses.addApiResponse(HTTP_200, addLegacyDocResponse(apiDoc, response));
            }
            else {
                processApiResponseClass(apiDoc, apiResponses);
            }
        }
        else {
            ApiResponse response = new ApiResponse().description("Success");
            apiResponses.addApiResponse(HTTP_200, addLegacyDocResponse(apiDoc, response));
        }

        operation.setResponses(apiResponses);
    }

    private void processApiResponseClass(ApiEndpointDoc apiDoc, ApiResponses apiResponses) {
        ApiResponse response = new ApiResponse();
        if (!apiDoc.responseDescription().isEmpty()) {
            response.setDescription(apiDoc.responseDescription());
        }

        Content content = new Content();
        MediaType mediaType = new MediaType();

        resolveAndRegisterSchema(apiDoc.responseClass());
        applyLegacyDocTypes(apiDoc.responseClass());
        mediaType.setSchema(buildSchemaRef(apiDoc.responseClass()));
        content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);

        response.setContent(content);
        apiResponses.addApiResponse(HTTP_200, addLegacyDocResponse(apiDoc, response));
    }

    private ApiResponse addLegacyDocResponse(ApiEndpointDoc apiDoc, ApiResponse response) {
        LegacyDocResponse legacyDocResponse = apiDoc.legacyDocResponse();
        if (isEmptyLegacyDocResponse(legacyDocResponse)) {
            return response;
        }

        if (legacyDocResponse.responseClass() != Void.class) {
            resolveAndRegisterSchema(legacyDocResponse.responseClass());
            applyLegacyDocTypes(legacyDocResponse.responseClass());
            response.addExtension(DOC_RESPONSE_SCHEMA_EXTENSION, buildSchemaRef(legacyDocResponse.responseClass()));
        }
        if (!legacyDocResponse.type().isBlank()) {
            response.addExtension(DOC_RESPONSE_TYPE_EXTENSION, legacyDocResponse.type());
        }
        if (!legacyDocResponse.name().isBlank()) {
            response.addExtension(DOC_RESPONSE_NAME_EXTENSION, legacyDocResponse.name());
        }
        if (legacyDocResponse.unnamed()) {
            response.addExtension(DOC_RESPONSE_UNNAMED_EXTENSION, Boolean.TRUE);
        }
        return response;
    }

    private boolean isEmptyLegacyDocResponse(LegacyDocResponse legacyDocResponse) {
        return legacyDocResponse.responseClass() == Void.class &&
                legacyDocResponse.type().isBlank() &&
                legacyDocResponse.name().isBlank() &&
                !legacyDocResponse.unnamed();
    }

    private void processLiteralParameters(Method method, Operation operation) {
        java.lang.reflect.Parameter[] reflectionParams = method.getParameters();
        IntStream.range(0, method.getParameterCount())
                .forEach(index -> {
                    var parameterAnnotation = findParameterAnnotation(
                            method,
                            index,
                            io.swagger.v3.oas.annotations.Parameter.class
                    );
                    if (parameterAnnotation != null && !parameterAnnotation.hidden()) {
                        operation.addParametersItem(
                                mapToOpenApiParameter(parameterAnnotation,
                                        reflectionParams[index].getParameterizedType())
                        );
                    }
                });
    }

    private Parameter mapToOpenApiParameter(
            io.swagger.v3.oas.annotations.Parameter parameterAnnotation,
            Type type) {
        Schema<?> schema = literalParameterSchema(type);
        applyDocumentedValues(schema, parameterAnnotation.schema());
        Parameter openApiParam = new Parameter()
                .name(parameterAnnotation.name())
                .required(parameterAnnotation.required())
                .in(parameterAnnotation.in().toString().toLowerCase())
                .schema(schema);

        if (!parameterAnnotation.description().isBlank()) {
            openApiParam.setDescription(parameterAnnotation.description());
        }

        return openApiParam;
    }

    /**
     * Carries the values a literal parameter documents onto its schema.
     *
     * The type of a literal parameter is derived from its declared Java type, so the documented
     * values are the only part of the annotation left to read. An array parameter documents the
     * values of its element type, the same way an array property does.
     *
     * @param schema the schema built from the declared type
     * @param annotation the schema annotation of the parameter
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applyDocumentedValues(Schema<?> schema, io.swagger.v3.oas.annotations.media.Schema annotation) {
        AnnotationsUtils.getSchemaFromAnnotation(annotation, null)
                .filter(documented -> documented.getEnum() != null && !documented.getEnum().isEmpty())
                .ifPresent(documented -> {
                    Schema target = schema.getItems() != null ? schema.getItems() : schema;
                    target.setEnum(documented.getEnum());
                    if (documented.getExtensions() != null) {
                        target.setExtensions(documented.getExtensions());
                    }
                });
    }

    /**
     * Maps the declared type of a literal parameter to its schema.
     *
     * A collection parameter is documented as an array of its element type, the same way a
     * collection property of a request body is.
     *
     * @param type the declared parameter type
     * @return the schema describing the parameter
     */
    private Schema<?> literalParameterSchema(Type type) {
        if (type instanceof ParameterizedType parameterized &&
                parameterized.getRawType() instanceof Class<?> rawType &&
                Collection.class.isAssignableFrom(rawType)) {
            Type[] arguments = parameterized.getActualTypeArguments();
            return new ArraySchema()
                    .items(literalParameterSchema(arguments.length == 1 ? arguments[0] : Object.class));
        }

        String typeName = type instanceof Class<?> parameterClass ? parameterClass.getName() : "";
        return switch (typeName) {
            case "int", "java.lang.Integer" -> new IntegerSchema();
            case "boolean", "java.lang.Boolean" -> new BooleanSchema();
            default -> new StringSchema();
        };
    }

    private void registerOperationOnPath(String namespace, Method method, HttpMethod httpMethod, Operation operation) {
        String path = buildPath(namespace, method.getName());
        PathItem pathItem = openAPI.getPaths().computeIfAbsent(path, key -> new PathItem());
        Operation documented = getOperationOnPathItem(pathItem, httpMethod);
        setOperationOnPathItem(pathItem, httpMethod,
                documented == null ? operation : mergeAlternativeOverload(documented, operation));
    }

    /**
     * Folds an overload taking different parameters into the operation already documented.
     *
     * Overloads of a handler method answer on one endpoint, so they are one operation, while the
     * legacy doclet documents one call per overload. An overload that only takes further
     * parameters is described by the optional ones it leaves out, but an overload taking a
     * different parameter instead has to contribute that parameter, and the calls it stands for,
     * to the operation the others share.
     *
     * @param documented the operation built from the overloads read so far
     * @param alternative the operation built from an overload taking different parameters
     * @return the operation standing for both
     */
    private Operation mergeAlternativeOverload(Operation documented, Operation alternative) {
        List<List<String>> calls = new ArrayList<>(documentedCalls(documented));
        documentedCalls(alternative).stream().filter(call -> !calls.contains(call)).forEach(calls::add);

        if (alternative.getParameters() != null) {
            alternative.getParameters().stream()
                    .filter(parameter -> !documentedParameters(documented).contains(parameter.getName()))
                    .forEach(parameter -> documented.addParametersItem(parameter));
        }
        mergeRequestBodies(documented, alternative);

        // A parameter the operation does not take in every call is optional, whichever overload
        // introduced it.
        if (documented.getParameters() != null) {
            documented.getParameters().stream()
                    .filter(parameter -> !calls.stream().allMatch(call -> call.contains(parameter.getName())))
                    .forEach(parameter -> parameter.setRequired(false));
        }
        Schema<?> body = requestBodySchema(documented);
        if (body != null && body.getRequired() != null) {
            body.setRequired(body.getRequired().stream()
                    .filter(name -> calls.stream().allMatch(call -> call.contains(name)))
                    .toList());
        }

        documented.addExtension(DOC_OVERLOAD_SHAPES_EXTENSION, calls);
        return documented;
    }

    /**
     * Merges the request body of an alternative overload into the documented one.
     *
     * Two overloads sending different bodies describe one payload holding the properties of both,
     * so the merged body is spelled out rather than referring to either side's schema.
     *
     * @param documented the operation built from the overloads read so far
     * @param alternative the operation built from an overload taking different parameters
     */
    private void mergeRequestBodies(Operation documented, Operation alternative) {
        Schema<?> alternativeBody = requestBodySchema(alternative);
        if (alternativeBody == null || alternativeBody.getProperties() == null) {
            return;
        }
        if (documented.getRequestBody() == null) {
            documented.setRequestBody(alternative.getRequestBody());
            return;
        }
        Schema<?> documentedBody = requestBodySchema(documented);
        if (documentedBody == null || documentedBody.getProperties() == null) {
            return;
        }

        Schema<Object> merged = new Schema<>().type("object");
        merged.setProperties(new LinkedHashMap<>(documentedBody.getProperties()));
        merged.setRequired(documentedBody.getRequired() == null ?
                new ArrayList<>() : new ArrayList<>(documentedBody.getRequired()));
        alternativeBody.getProperties().forEach((name, property) -> {
            if (!merged.getProperties().containsKey(name)) {
                merged.addProperty(name, property);
            }
        });
        documented.getRequestBody().getContent().get(DEFAULT_MEDIA_TYPE).setSchema(merged);
    }

    /**
     * Lists the calls an operation stands for on its own.
     *
     * @param operation the operation to expand
     * @return the parameters of each documented call
     */
    private List<List<String>> documentedCalls(Operation operation) {
        List<String> documented = documentedParameters(operation);
        return expandOverloads(operation,
                documented.stream().filter(name -> isRequired(operation, name)).toList(),
                documented.stream().filter(name -> !isRequired(operation, name)).toList());
    }

    private Operation getOperationOnPathItem(PathItem pathItem, HttpMethod httpMethod) {
        return switch (httpMethod) {
            case get -> pathItem.getGet();
            case put -> pathItem.getPut();
            case delete -> pathItem.getDelete();
            case patch -> pathItem.getPatch();
            default -> pathItem.getPost();
        };
    }

    private String buildPath(String namespace, String methodName) {
        if (namespace == null || namespace.isBlank()) {
            return "/" + methodName;
        }
        return "/" + namespace + "/" + methodName;
    }

    private void setOperationOnPathItem(PathItem pathItem, HttpMethod httpMethod, Operation operation) {
        switch (httpMethod) {
            case get -> pathItem.setGet(operation);
            case put -> pathItem.setPut(operation);
            case delete -> pathItem.setDelete(operation);
            case patch -> pathItem.setPatch(operation);
            default -> pathItem.setPost(operation);
        }
    }

    private ApiResponse createIntegerResponse(String description) {
        if (this.components.getSchemas() == null || !this.components.getSchemas().containsKey("IntegerResponse")) {
            Schema<?> integerResponseSchema = new Schema<>()
                    .type("object")
                    .addRequiredItem("success")
                    .addRequiredItem("result")
                    .addProperty(
                            "message",
                            new StringSchema().description("Error message if success is false").nullable(true)
                    )
                    .addProperty("result", new IntegerSchema().description("The payload result").format("int32"))
                    .addProperty("success", new BooleanSchema().description("Operation success status").example(true));
            this.components.addSchemas("IntegerResponse", integerResponseSchema);
        }

        ApiResponse response = new ApiResponse();
        response.setDescription(description.isBlank() ? "1 on success, exception thrown otherwise." : description);

        Content content = new Content();
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new Schema<>().$ref("#/components/schemas/IntegerResponse"));
        content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);

        response.setContent(content);
        return response;
    }

    private void resolveAndRegisterSchema(Class<?> clazz) {
        var schemas = ModelConverters.getInstance().readAll(clazz);
        if (schemas != null) {
            schemas.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> this.components.addSchemas(entry.getKey(), entry.getValue()));
        }
    }

    private Schema<?> buildSchemaRef(Class<?> clazz) {
        return new Schema<>().$ref("#/components/schemas/" + schemaRefName(clazz));
    }

    private String schemaRefName(Class<?> clazz) {
        var classAnnotation = findClassAnnotation(clazz, io.swagger.v3.oas.annotations.media.Schema.class);
        return Optional.ofNullable(classAnnotation)
                .map(io.swagger.v3.oas.annotations.media.Schema::name)
                .filter(name -> !name.isEmpty())
                .orElse(clazz.getSimpleName());
    }

    private <A extends Annotation> A findClassAnnotation(Class<?> cls, Class<A> annotationClass) {
        if (cls == null || cls == Object.class) {
            return null;
        }
        A annotation = cls.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> iface : cls.getInterfaces()) {
            annotation = findClassAnnotation(iface, annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return findClassAnnotation(cls.getSuperclass(), annotationClass);
    }

    private <A extends Annotation> A findMethodAnnotation(Method method, Class<A> annotationClass) {
        A annotation = method.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }

        Class<?> declaringClass = method.getDeclaringClass();
        for (Class<?> iface : declaringClass.getInterfaces()) {
            try {
                Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                annotation = findMethodAnnotation(ifaceMethod, annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
            catch (NoSuchMethodException e) {
                // Method not found in this interface, continue searching.
            }
        }

        Class<?> superClass = declaringClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                return findMethodAnnotation(superMethod, annotationClass);
            }
            catch (NoSuchMethodException e) {
                // Method not found in superclass, continue searching.
            }
        }
        return null;
    }

    private <A extends Annotation> A findParameterAnnotation(Method method, int paramIndex, Class<A> annotationClass) {
        for (Annotation annotation : method.getParameterAnnotations()[paramIndex]) {
            if (annotationClass.isInstance(annotation)) {
                return annotationClass.cast(annotation);
            }
        }
        Class<?> declaringClass = method.getDeclaringClass();
        for (Class<?> iface : declaringClass.getInterfaces()) {
            try {
                Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                A annotation = findParameterAnnotation(ifaceMethod, paramIndex, annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
            catch (NoSuchMethodException e) {
                // Method not found in this interface, continue searching.
            }
        }
        Class<?> superClass = declaringClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                return findParameterAnnotation(superMethod, paramIndex, annotationClass);
            }
            catch (NoSuchMethodException e) {
                // Method not found in superclass, continue searching.
            }
        }
        return null;
    }

    /**
     * Returns the generated OpenAPI specification.
     *
     * @return OpenAPI specification
     */
    public OpenAPI getSpec() {
        return openAPI;
    }
}
