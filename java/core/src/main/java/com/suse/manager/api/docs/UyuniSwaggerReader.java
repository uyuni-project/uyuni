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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import io.swagger.models.HttpMethod;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

public class UyuniSwaggerReader {

    private static final Logger LOG = LogManager.getLogger(UyuniSwaggerReader.class);
    public static final String DEFAULT_MEDIA_TYPE = "application/json";
    public static final String HTTP_200 = "200";

    private final OpenAPI openAPI;
    private final Components components;

    public UyuniSwaggerReader() {
        this.openAPI = new OpenAPI();
        this.components = new Components();
        this.openAPI.setComponents(components);
        this.openAPI.setPaths(new Paths());

        this.openAPI.setInfo(new Info()
                .title("Uyuni API Docs")
                .description("Welcome to the Uyuni API.")
                .version("1.0.0"));

        this.components.addSecuritySchemes("session", new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("pxt-session-cookie")
                .in(SecurityScheme.In.COOKIE));
    }

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
            .forEach(method -> processMethod(namespace, method, tagAnnotation));

        return openAPI;
    }

    private void processMethod(String namespace, Method method, Tag tagAnnotation) {
        ApiEndpointDoc apiDoc = findMethodAnnotation(method, ApiEndpointDoc.class);
        if (apiDoc == null) return;

        Operation openApiOperation = createOperationWithBasicInfo(method, tagAnnotation, apiDoc);
        applySecurityIfNeeded(method, openApiOperation);
        configureRequestBodyIfPresent(apiDoc, openApiOperation);
        configureResponses(apiDoc, openApiOperation);
        processLiteralParameters(method, openApiOperation);
        registerOperationOnPath(namespace, method, apiDoc.method(), openApiOperation);
    }

    private Operation createOperationWithBasicInfo(Method method, Tag tagAnnotation, ApiEndpointDoc apiDoc) {
        Operation op = new Operation();
        op.setOperationId(method.getName());
        if (!apiDoc.summary().isEmpty()) op.setSummary(apiDoc.summary());
        if (!apiDoc.description().isEmpty()) op.setDescription(apiDoc.description());
        op.addTagsItem(tagAnnotation.name());
        return op;
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
        if (apiDoc.requestClass() == Void.class) return;

        RequestBody requestBody = new RequestBody();
        requestBody.setRequired(true);
        if (!apiDoc.requestDescription().isEmpty()) {
            requestBody.setDescription(apiDoc.requestDescription());
        }

        Content content = new Content();
        MediaType mediaType = new MediaType();

        resolveAndRegisterSchema(apiDoc.requestClass());
        mediaType.setSchema(buildSchemaRef(apiDoc.requestClass()));
        content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);
        requestBody.setContent(content);
        operation.setRequestBody(requestBody);
    }

    private void configureResponses(ApiEndpointDoc apiDoc, Operation operation) {
        ApiResponses apiResponses = new ApiResponses();

        if (apiDoc.isIntegerResponse()) {
            apiResponses.addApiResponse(HTTP_200, createIntegerResponse());
            operation.setResponses(apiResponses);
            return;
        }

        if (apiDoc.responseClass() != Void.class) {
            Class<?> respCls = apiDoc.responseClass();
            if (respCls == String.class || respCls == Integer.class || respCls == Boolean.class) {
                ApiResponse response = new ApiResponse().description(
                    apiDoc.responseDescription().isEmpty() ? "Success" : apiDoc.responseDescription()
                );
                Content content = new Content();
                MediaType mediaType = new MediaType();

                Schema<?> schema = switch (respCls.getSimpleName()) {
                    case "String" -> new StringSchema();
                    case "Integer" -> new IntegerSchema();
                    case "Boolean" -> new BooleanSchema();
                    default -> new StringSchema();
                };

                mediaType.setSchema(schema);
                content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);
                response.setContent(content);
                apiResponses.addApiResponse(HTTP_200, response);
            }
            else {
                processApiResponseClass(apiDoc, apiResponses);
            }
        }
        else {
            apiResponses.addApiResponse(HTTP_200, new ApiResponse().description("Success"));
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
        mediaType.setSchema(buildSchemaRef(apiDoc.responseClass()));
        content.addMediaType(DEFAULT_MEDIA_TYPE, mediaType);

        response.setContent(content);
        apiResponses.addApiResponse(HTTP_200, response);
    }

    private void processLiteralParameters(Method method, Operation op) {
        java.lang.reflect.Parameter[] reflectionParams = method.getParameters();
        IntStream.range(0, method.getParameterCount())
            .forEach(i -> {
                var paramAnnotation = findParameterAnnotation(method, i, io.swagger.v3.oas.annotations.Parameter.class);
                if (paramAnnotation != null && !paramAnnotation.hidden()) {
                    op.addParametersItem(mapToOpenApiParameter(paramAnnotation, reflectionParams[i].getType()));
                }
            });
    }

    private Parameter mapToOpenApiParameter(io.swagger.v3.oas.annotations.Parameter paramAnnotation, Class<?> type) {
        var openApiParam = new io.swagger.v3.oas.models.parameters.Parameter()
            .name(paramAnnotation.name())
            .required(paramAnnotation.required())
            .in(paramAnnotation.in().toString().toLowerCase())
            .schema(switch (type.getName()) {
                case "int", "java.lang.Integer" -> new IntegerSchema();
                case "boolean", "java.lang.Boolean" -> new BooleanSchema();
                default -> new StringSchema();
            });

        if (!paramAnnotation.description().isBlank()) {
            openApiParam.setDescription(paramAnnotation.description());
        }

        return openApiParam;
    }

    private void registerOperationOnPath(String namespace, Method method, HttpMethod httpMethod, Operation op) {
        String path = buildPath(namespace, method.getName());
        PathItem pathItem = openAPI.getPaths().computeIfAbsent(path, k -> new PathItem());
        setOperationOnPathItem(pathItem, httpMethod, op);
    }

    private String buildPath(String namespace, String methodName) {
        if (namespace == null || namespace.isBlank()) {
            return "/" + methodName;
        }
        return "/" + namespace + "/" + methodName;
    }

    private void setOperationOnPathItem(PathItem pathItem, HttpMethod httpMethod, Operation operation) {
        switch (httpMethod) {
            case GET    -> pathItem.setGet(operation);
            case PUT    -> pathItem.setPut(operation);
            case DELETE -> pathItem.setDelete(operation);
            case PATCH  -> pathItem.setPatch(operation);
            default     -> pathItem.setPost(operation);
        }
    }

    private ApiResponse createIntegerResponse() {
        if (this.components.getSchemas() == null || !this.components.getSchemas().containsKey("IntegerResponse")) {
            Schema<?> integerResponseSchema = new Schema<>()
                .type("object")
                .addRequiredItem("success")
                .addRequiredItem("result")
                .addProperty(
                    "message", new StringSchema().description("Error message if success is false").nullable(true)
                )
                .addProperty("result", new IntegerSchema().description("The payload result").format("int32"))
                .addProperty("success", new BooleanSchema().description("Operation success status").example(true));
            this.components.addSchemas("IntegerResponse", integerResponseSchema);
        }

        ApiResponse response = new ApiResponse();
        response.setDescription("1 on success, exception thrown otherwise.");

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
        var classAnnotation = findClassAnnotation(clazz, io.swagger.v3.oas.annotations.media.Schema.class);
        String refName = Optional.ofNullable(classAnnotation)
                .map(io.swagger.v3.oas.annotations.media.Schema::name)
                .filter(name -> !name.isEmpty())
                .orElse(clazz.getSimpleName());
        return new Schema<>().$ref("#/components/schemas/" + refName);
    }

    private <A extends Annotation> A findClassAnnotation(Class<?> cls, Class<A> annotationClass) {
        if (cls == null || cls == Object.class) return null;
        A annotation = cls.getAnnotation(annotationClass);
        if (annotation != null) return annotation;
        for (Class<?> iface : cls.getInterfaces()) {
            annotation = findClassAnnotation(iface, annotationClass);
            if (annotation != null) return annotation;
        }
        return findClassAnnotation(cls.getSuperclass(), annotationClass);
    }

    private <A extends Annotation> A findMethodAnnotation(Method method, Class<A> annotationClass) {
        A annotation = method.getAnnotation(annotationClass);
        if (annotation != null) return annotation;

        Class<?> declaringClass = method.getDeclaringClass();
        for (Class<?> iface : declaringClass.getInterfaces()) {
            try {
                Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                annotation = findMethodAnnotation(ifaceMethod, annotationClass);
                if (annotation != null) return annotation;
            } catch (NoSuchMethodException e) {
                // Method not found in this interface, continue searching
            }
        }

        Class<?> superClass = declaringClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                return findMethodAnnotation(superMethod, annotationClass);
            } catch (NoSuchMethodException e) {
                // Method not found in superclass, continue searching
            }
        }
        return null;
    }

    private <A extends Annotation> A findParameterAnnotation(Method method, int paramIndex, Class<A> annotationClass) {
        for (Annotation ann : method.getParameterAnnotations()[paramIndex]) {
            if (annotationClass.isInstance(ann)) {
                return annotationClass.cast(ann);
            }
        }
        Class<?> declaringClass = method.getDeclaringClass();
        for (Class<?> iface : declaringClass.getInterfaces()) {
            try {
                Method ifaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                A ann = findParameterAnnotation(ifaceMethod, paramIndex, annotationClass);
                if (ann != null) return ann;
            } catch (NoSuchMethodException e) {
                // Method not found in this interface, continue searching
            }
        }
        Class<?> superClass = declaringClass.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            try {
                Method superMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                return findParameterAnnotation(superMethod, paramIndex, annotationClass);
            } catch (NoSuchMethodException e) {
                // Method not found in superclass, continue searching
            }
        }
        return null;
    }

    public OpenAPI getSpec() {
        return openAPI;
    }
}
