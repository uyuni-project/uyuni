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
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.api.RouteFactory;
import com.suse.manager.api.docs.UyuniSwaggerReader;
import com.suse.utils.Json;

import com.google.gson.Gson;

import org.everit.json.schema.loader.SchemaLoader;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.routematch.RouteMatch;

/**
 * Base class for API contract testing against OpenAPI specifications.
 * This class facilitates testing of API handlers by generating the OpenAPI spec
 * on-the-fly and validating both the request (input) and response (output) against it.
 * It uses a mocked Spark Java environment to execute handler logic without starting
 * a real network server.
 */
public abstract class BaseOpenApiTest {

    protected final Mockery context = new Mockery() {{
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    protected HttpServletRequest mockServletRequest;
    protected HttpServletResponse mockServletResponse;
    protected User mockUser;
    protected Org fakeOrg;
    protected RouteFactory testableRouteFactory;
    protected BaseHandler handlerMock;

    /**
     * Cache for the generated OpenAPI specification.
     * It is cleared or updated when a test class for a different handler is executed.
     */
    private static Map.Entry<Class<?>, OpenAPI> openApiSpecification;

    /**
     * @return the API namespace/prefix used in URLs (e.g., "access", "system").
     */
    protected abstract String getApiNamespace();

    /**
     * @return the handler class being tested (e.g., AccessHandler.class).
     */
    protected abstract Class<?> getHandlerClass();

    /**
     * @return the mocked instance of the handler being tested.
     */
    protected BaseHandler getHandlerMock() {
        return handlerMock;
    }

    @BeforeEach
    public void commonSetUp() {
        ensureOpenApiIsLoaded();
        initializeMocks();
        initializeRouteFactory();
    }

    @AfterEach
    public void commonTearDown() {
        context.assertIsSatisfied();
    }

    protected ContractBuilder validateApiContract(String path, String method) {
        return new ContractBuilder(path, method);
    }

    /**
     * Fluent builder to configure and execute API contract validations.
     */
    public class ContractBuilder {
        private final String path;
        private final String httpMethod;
        private Object body;
        private Map<String, String[]> params;

        ContractBuilder(String requestPath, String requestMethod) {
            this.path = requestPath;
            this.httpMethod = requestMethod;
        }

        /**
         * Attaches a JSON body to the request.
         *
         * @param requestBody object to be serialized as JSON
         * @return this builder
         */
        public ContractBuilder withBody(Object requestBody) {
            this.body = requestBody;
            return this;
        }

        /**
         * Attaches query parameters to the request.
         *
         * @param requestParams map of parameter names to string arrays
         * @return this builder
         */
        public ContractBuilder withParams(Map<String, String[]> requestParams) {
            this.params = requestParams;
            return this;
        }

        /**
         * Validates and executes the contract against the specified handler method.
         * Parameter types are automatically inferred if the method is not overloaded.
         *
         * @param methodName name of the Java method in the handler
         * @throws Exception if method is not found or is ambiguous
         */
        public void onHandlerMethod(String methodName) throws Exception {
            var methods = Stream.of(getHandlerClass().getMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .toList();

            if (methods.isEmpty()) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            if (methods.size() > 1) {
                throw new IllegalStateException("Multiple methods found: " + methodName);
            }

            onHandlerMethod(methodName, methods.get(0).getParameterTypes());
        }

        public void onHandlerMethod(String methodName, Class<?>... argTypes) throws Exception {
            validateInputAgainstSwagger();
            setupMockServlet(httpMethod, body, params);
            executeAndValidate(path, httpMethod, methodName, argTypes);
        }

        private void validateInputAgainstSwagger() {
            PathItem pathItem = getOpenApi().getPaths().get(path);
            Assertions.assertNotNull(pathItem, "Path %s not found in Swagger spec.".formatted(path));

            Operation operation = getOperation(pathItem, httpMethod);
            Assertions.assertNotNull(operation, "Method %s not defined for %s".formatted(httpMethod, path));

            validateQueryParams(operation);
            validateRequestBody(operation);
        }

        private void validateQueryParams(Operation operation) {
            if (params == null || params.isEmpty()) {
                return;
            }

            var documentedParams = Stream.ofNullable(operation.getParameters())
                    .flatMap(Collection::stream)
                    .filter(parameter -> "query".equals(parameter.getIn()))
                    .map(Parameter::getName)
                    .collect(Collectors.toUnmodifiableSet());

            var undocumented = params.keySet().stream()
                    .filter(name -> !documentedParams.contains(name))
                    .toList();

            if (!undocumented.isEmpty()) {
                Assertions.fail(
                        "CONTRACT VIOLATION (Query Param) for %s in %s: %s are not documented."
                                .formatted(path, getHandlerClass(), undocumented)
                );
            }
        }

        private void validateRequestBody(Operation operation) {
            if (body == null) {
                return;
            }

            var content = Optional.ofNullable(operation.getRequestBody())
                    .map(RequestBody::getContent)
                    .map(contentMap -> contentMap.get("application/json"))
                    .orElse(null);

            if (content == null || content.getSchema() == null) {
                Assertions.fail(
                        "CONTRACT VIOLATION (Request Body): Test sent body but no JSON schema defined for " + path
                );
            }

            validateJsonAgainstSchema(content.getSchema(), Json.GSON.toJson(body));
        }
    }

    private void ensureOpenApiIsLoaded() {
        Class<?> currentClass = getHandlerClass();
        if (openApiSpecification == null || !openApiSpecification.getKey().equals(currentClass)) {
            OpenAPI spec = new UyuniSwaggerReader().read(currentClass, getApiNamespace());
            openApiSpecification = Map.entry(currentClass, spec);
        }
    }

    private OpenAPI getOpenApi() {
        return openApiSpecification.getValue();
    }

    /**
     * Validates a JSON string against a given OpenAPI schema.
     * It automatically injects the global OpenAPI 'components' into the local schema
     * context to resolve internal $ref references and enforces strict property checking.
     *
     * @param schema OpenAPI schema definition
     * @param jsonToValidate actual JSON content to validate
     */
    private void validateJsonAgainstSchema(Schema<?> schema, String jsonToValidate) {
        JSONObject rawSchema = new JSONObject(
                new JSONTokener(io.swagger.v3.core.util.Json.pretty(schema))
        );
        if (getOpenApi().getComponents() != null) {
            JSONObject components = new JSONObject(
                    new JSONTokener(io.swagger.v3.core.util.Json.pretty(getOpenApi().getComponents()))
            );
            rawSchema.put("components", components);
        }

        if (Stream.of("anyOf", "oneOf", "allOf").noneMatch(rawSchema::has)) {
            rawSchema.put("additionalProperties", false);
        }

        try {
            SchemaLoader.load(rawSchema).validate(new JSONObject(new JSONTokener(jsonToValidate)));
        }
        catch (Exception e) {
            Assertions.fail(
                    """
                    CONTRACT VIOLATION (Schema Validation):
                    Path: %s
                    Error: %s
                    JSON: %s
                    """.formatted(openApiSpecification.getKey().getSimpleName(), e.getMessage(), jsonToValidate)
            );
        }
    }

    /**
     * Verifies if the handler's output matches the documented 200 OK response
     * in the OpenAPI specification.
     */
    private void assertJsonCompliance(String path, String httpMethod, String jsonOutput) {
        PathItem item = getOpenApi().getPaths().get(path);
        Operation operation = getOperation(item, httpMethod);
        var response = operation.getResponses().get("200");

        if (response.getContent() == null || response.getContent().get("application/json") == null) {
            return;
        }

        validateJsonAgainstSchema(response.getContent().get("application/json").getSchema(), jsonOutput);
    }

    protected void setupDefaultUserBehavior() {
        context.checking(new Expectations() {{
            allowing(mockUser).getId();
            will(returnValue(100L));
            allowing(mockUser).getOrg();
            will(returnValue(fakeOrg));
            allowing(mockUser).getLogin();
            will(returnValue("admin"));
            allowing(mockUser).isReadOnly();
            will(returnValue(false));
        }});
    }

    protected void setupMockServlet(String method, Object body, Map<String, String[]> params) throws IOException {
        final String jsonBody = body != null ? Json.GSON.toJson(body) : "";
        final Map<String, String[]> queryMap = params != null ? params : Collections.emptyMap();

        context.checking(new Expectations() {{
            allowing(mockServletRequest).getMethod();
            will(returnValue(method));
            allowing(mockServletRequest).getCharacterEncoding();
            will(returnValue("UTF-8"));
            allowing(mockServletRequest).getContentType();
            will(returnValue("application/json"));
            allowing(mockServletRequest).getInputStream();
            will(returnValue(new StringServletInputStream(jsonBody)));
            allowing(mockServletRequest).getParameterMap();
            will(returnValue(queryMap));

            queryMap.forEach((key, value) -> {
                allowing(mockServletRequest).getParameter(key);
                will(returnValue(value[0]));
            });

            allowing(mockServletRequest).getHeader("Accept");
            will(returnValue("application/json"));
            allowing(mockServletRequest).getHeaders(with(any(String.class)));
            will(returnValue(Collections.enumeration(Collections.emptyList())));
            allowing(mockServletRequest).getAttribute(with(any(String.class)));
            will(returnValue(null));
            allowing(mockServletRequest).getContentLength();
            will(returnValue(jsonBody.length()));
            ignoring(mockServletResponse);
        }});
    }

    protected void executeAndValidate(String path, String method, String methodName, Class<?>... argTypes)
            throws Exception {
        Method handlerMethod = getHandlerClass().getMethod(methodName, argTypes);
        Route route = testableRouteFactory.createRoute(handlerMethod, getHandlerMock());

        Request sparkReq = createSparkRequest(mockServletRequest);
        Response sparkRes = createSparkResponse(mockServletResponse);

        Object response = route.handle(sparkReq, sparkRes);
        String jsonOutput = (String) response;
        assertJsonCompliance(path, method, jsonOutput);
    }

    private Operation getOperation(PathItem item, String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> item.getGet();
            case "POST" -> item.getPost();
            case "PUT" -> item.getPut();
            case "DELETE" -> item.getDelete();
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

    private Request createSparkRequest(HttpServletRequest servletRequest) throws Exception {
        Constructor<Request> constructor = Request.class.getDeclaredConstructor(
                RouteMatch.class,
                HttpServletRequest.class
        );
        constructor.setAccessible(true);
        return constructor.newInstance(createDummyRouteMatch(), servletRequest);
    }

    private Response createSparkResponse(HttpServletResponse servletResponse) throws Exception {
        Constructor<Response> constructor = Response.class.getDeclaredConstructor(HttpServletResponse.class);
        constructor.setAccessible(true);
        return constructor.newInstance(servletResponse);
    }

    private Object createDummyRouteMatch() throws Exception {
        Class<?> routeMatchClass = Class.forName("spark.routematch.RouteMatch");
        Constructor<?> constructor = routeMatchClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            if (constructor.getParameterTypes()[i].equals(String.class)) {
                args[i] = "/dummy";
            }
            else if (constructor.getParameterTypes()[i].equals(Map.class)) {
                args[i] = new HashMap<>();
            }
        }
        return constructor.newInstance(args);
    }

    private static class StringServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream stream;

        StringServletInputStream(String content) {
            this.stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public int read() {
            return stream.read();
        }

        @Override
        public boolean isFinished() {
            return stream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }
    }

    private void initializeRouteFactory() {
        this.testableRouteFactory = new RouteFactory(null) {
            @Override
            protected String getSessionKeyFromRequest(Request req) {
                return "fake-session";
            }

            @Override
            protected User getUserFromSessionKey(String key) {
                return mockUser;
            }

            @Override
            protected Route wrapRouteAsJson(Route route) {
                return (req, res) -> {
                    res.type("application/json");
                    return route.handle(req, res);
                };
            }

            @Override
            protected Object renderErrorJson(Response res, Throwable e) {
                return "{\"error\": \"" + e.getMessage() + "\"}";
            }

            @Override
            protected void rollbackTransactionSafe() {
            }

            @Override
            protected Gson initGsonWithSerializers() {
                return Json.GSON;
            }
        };
    }

    private void initializeMocks() {
        String id = String.valueOf(System.nanoTime());
        mockServletRequest = context.mock(HttpServletRequest.class, "req" + id);
        mockServletResponse = context.mock(HttpServletResponse.class, "res" + id);
        mockUser = context.mock(User.class, "user" + id);
        handlerMock = (BaseHandler) context.mock(getHandlerClass(), "handlerMock" + id);
        fakeOrg = new Org();
        fakeOrg.setId(1L);
        fakeOrg.setName("Default Test Org");
        setupDefaultUserBehavior();
    }
}
