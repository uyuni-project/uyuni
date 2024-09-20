/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegate;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegateFactory;
import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.HttpApiResponse;
import com.suse.manager.api.ListDeserializer;
import com.suse.manager.api.MapDeserializer;
import com.suse.manager.api.RouteFactory;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.webui.controllers.test.BaseControllerTestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spark.HaltException;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.Route;

public class RouteFactoryTest extends BaseControllerTestCase {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Map.class, new MapDeserializer())
            .registerTypeAdapter(List.class, new ListDeserializer())
            .create();
    private final JsonParser parser = new JsonParser();
    private RouteFactory routeFactory;
    private TestHandler handler;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        handler = new TestHandler();
        routeFactory = new RouteFactory(createTestSerializerFactory());
    }

    static SerializerFactory createTestSerializerFactory() {
        return new SerializerFactory() {
            private final List<ApiResponseSerializer<?>> serializers = List.of(
                    new TestSerializer(),
                    new TestComplexSerializer());

            @Override
            public List<ApiResponseSerializer<?>> getSerializers() {
                return serializers;
            }
        };
    }

    /**
     * Tests if the authorized user instance is passed into the API method
     */
    @Test
    public void testWithUser() throws Exception {
        Method withUser = TestHandler.class.getMethod("withUser", User.class);
        Route route = routeFactory.createRoute(withUser, handler);

        Request req = createRequest("/manager/api/test/withUser");
        Response res = createResponse();
        authorizeRequest(req, res, user);
        assertEquals(user.getId(), getResult((String) route.handle(req, res), Long.class));
    }

    /**
     * Tests a read-only user against a read-write method
     */
    @Test
    public void testWithReadOnlyUser() throws Exception {
        User readOnly = UserTestUtils.createUser("readonly-user", user.getOrg().getId());
        readOnly.setReadOnly(true);

        Method withUser = TestHandler.class.getMethod("withUser", User.class);
        Route route = routeFactory.createRoute(withUser, handler);

        Request req = createRequest("/manager/api/test/withUser");
        Response res = createResponse();
        authorizeRequest(req, res, readOnly);

        HaltException e = assertThrows(HaltException.class, () -> route.handle(req, res));
        assertEquals(HttpStatus.SC_FORBIDDEN, e.statusCode());
        assertEquals("The method is not available to read-only API users", e.body());
    }

    /**
     * Tests handling of different types of numbers
     */
    @Test
    public void testNumbers() throws Exception {
        Method numbers = handler.getClass().getMethod("numbers", Integer.class, Long.class, Double.class);
        Route route = routeFactory.createRoute(numbers, handler);

        Map<String, String> queryParams = Map.of(
                "myInteger", "1",
                "myLong", Long.toString(Integer.MAX_VALUE + 1L),
                "myDouble", "3.14");
        Request req = createRequest("/manager/api/test/numbers", queryParams);
        Response res = createResponse();

        String result = (String) route.handle(req, res);
        assertTrue(result.contains("\"myInteger\":1"));
        assertTrue(result.contains("\"myLong\":" + (Integer.MAX_VALUE + 1L)));
        assertTrue(result.contains("\"myDouble\":3.14"));
    }

    /**
     * Tests handling of primitive parameter types in query string
     */
    @Test
    public void testBasicTypes() throws Exception {
        Method basicTypes = handler.getClass().getMethod("basicTypes", Integer.class, String.class, Boolean.class);
        Route route = routeFactory.createRoute(basicTypes, handler);

        Map<String, String> queryParams = Map.of("myInteger", "1", "myString", "$tr:ng", "myBoolean", "true");
        Request req = createRequest("/manager/api/test/basicTypes", queryParams);
        Response res = createResponse();
        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(1, result.get("myInteger"));
        assertEquals("$tr:ng", result.get("myString"));
        assertEquals(true, result.get("myBoolean"));
    }

    /**
     * Tests handling of empty parameter values in query string
     */
    @Test
    public void testEmptyParams() throws Exception {
        Method basicTypes = handler.getClass().getMethod("basicTypes", Integer.class, String.class, Boolean.class);
        Route route = routeFactory.createRoute(basicTypes, handler);

        Map<String, String> queryParams = Map.of("myInteger", "1", "myString", "", "myBoolean", "true");
        Request req = createRequest("/manager/api/test/basicTypes", queryParams);
        Response res = createResponse();
        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(1, result.get("myInteger")); // gson prefers long when deserializing numbers
        assertEquals("-empty-", result.get("myString"));
        assertEquals(true, result.get("myBoolean"));
    }

    /**
     * Tests precedence of an argument value provided in both query string and the body
     */
    @Test
    public void testBasicTypesMixedArgs() throws Exception {
        Method basicTypes = handler.getClass().getMethod("basicTypes", Integer.class, String.class, Boolean.class);
        Route route = routeFactory.createRoute(basicTypes, handler);

        Map<String, String> queryParams = Map.of("myInteger", "1", "myString", "foo");
        Map<String, Object> bodyParams = Map.of("myString", "bar", "myBoolean", "true");
        Request req = createRequest("/manager/api/test/basicTypes", queryParams, GSON.toJson(bodyParams));
        Response res = createResponse();
        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(1, result.get("myInteger")); // gson prefers long when deserializing numbers
        assertEquals("bar", result.get("myString")); // value in body should take precedence
        assertEquals(true, result.get("myBoolean"));
    }

    /**
     * Tests API response in case of missing arguments
     */
    @Test
    public void testBasicTypesMissingArgs() throws Exception {
        Method basicTypes = handler.getClass().getMethod("basicTypes", Integer.class, String.class, Boolean.class);
        Route route = routeFactory.createRoute(basicTypes, handler);

        Map<String, String> queryParams = Map.of("myInteger", "1");
        Request req = createRequest("/manager/api/test/basicTypes", queryParams);
        Response res = createResponse();

        HaltException e = assertThrows(HaltException.class, () -> route.handle(req, res));
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.statusCode());
    }

    /**
     * Tests handling of primitive parameter types in the request body
     */
    @Test
    public void testBasicTypesInBody() throws Exception {
        Method basicTypes = handler.getClass().getMethod("basicTypes", Integer.class, String.class, Boolean.class);
        Route route = routeFactory.createRoute(basicTypes, handler);

        Map<String, Object> bodyParams = Map.of("myInteger", 1, "myString", "$tr:ng", "myBoolean", true);
        Request req = createRequest("/manager/api/test/basicTypes", GSON.toJson(bodyParams));
        Response res = createResponse();
        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(1, result.get("myInteger")); // gson prefers long when deserializing numbers
        assertEquals("$tr:ng", result.get("myString"));
        assertEquals(true, result.get("myBoolean"));
    }

    /**
     * Tests handling of a basic {@link Date} argument in query string
     */
    @Test
    public void testBasicDate() throws Exception {
        Method basicDate = handler.getClass().getMethod("basicDate", Date.class);
        Route route = routeFactory.createRoute(basicDate, handler);

        Request req = createRequest("/manager/api/test/basicDate",
                Collections.singletonMap("myDate", "2022-04-01T00:00:00+02:00"));
        Response res = createResponse();
        String result = getResult((String) route.handle(req, res), String.class);

        assertEquals("2022-03-31T22:00:00Z", result);
    }

    /**
     * Tests handling of a basic {@link Date} argument in the request body
     */
    @Test
    public void testBasicDateInBody() throws Exception {
        Method basicDate = handler.getClass().getMethod("basicDate", Date.class);
        Route route = routeFactory.createRoute(basicDate, handler);

        Request req = createRequest("/manager/api/test/basicDate",
                GSON.toJson(Collections.singletonMap("myDate", "2022-04-01T00:00:00+02:00")));
        Response res = createResponse();
        String result = getResult((String) route.handle(req, res), String.class);

        assertEquals("2022-03-31T22:00:00Z", result);
    }

    /**
     * Tests handling of ISO 8601 dates
     */
    @Test
    public void testIso8601Date() throws Exception {
        Method basicDate = handler.getClass().getMethod("basicDate", Date.class);
        Route route = routeFactory.createRoute(basicDate, handler);

        Request req = createRequest("/manager/api/test/basicDate",
                Collections.singletonMap("myDate", "2022-04-01T12:00:05Z"));
        Response res = createResponse();
        Date result = getResult((String) route.handle(req, res), Date.class);

        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse("2022-04-01T12:00:05Z"), result);
    }

    /**
     * Tests handling of an invalid {@link Date} argument in query string
     */
    @Test
    public void testInvalidDate() throws Exception {
        Method basicDate = handler.getClass().getMethod("basicDate", Date.class);
        Route route = routeFactory.createRoute(basicDate, handler);

        Request req = createRequest("/manager/api/test/basicDate", Collections.singletonMap("myDate", "not-a-date"));
        Response res = createResponse();

        HaltException e = assertThrows(HaltException.class, () -> route.handle(req, res));
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.statusCode());
    }

    /**
     * Tests parsing of a list of integers in the request body
     *
     * Note: Because of type erasure, GSON will parse elements as {@link Long} instances
     * regardless of the declared type parameter in the handler.
     */
    @Test
    public void testIntegerList() throws Exception {
        Method sortIntegerList = handler.getClass().getMethod("sortIntegerList", List.class);
        Route route = routeFactory.createRoute(sortIntegerList, handler);

        Map<String, Object> bodyParams = Collections.singletonMap("myList", List.of(3, 2, 5));
        Request req = createRequest("/manager/api/test/sortIntegerList", GSON.toJson(bodyParams));
        Response res = createResponse();
        List<?> result = getResult((String) route.handle(req, res), List.class);

        assertEquals(List.of(2, 3, 5), result);
    }

    /**
     * Tests parsing of a list of {@link Long}s in the request body
     */
    @Test
    public void testLongList() throws Exception {
        Method sortLongList = handler.getClass().getMethod("sortLongList", List.class);
        Route route = routeFactory.createRoute(sortLongList, handler);

        Map<String, Object> bodyParams = Collections.singletonMap("myList", List.of(3L, 2L, 5L));
        Request req = createRequest("/manager/api/test/sortLongList", GSON.toJson(bodyParams));
        Response res = createResponse();
        List<Long> result = getResult((String) route.handle(req, res),
                TypeToken.getParameterized(List.class, Long.class).getType());

        assertEquals(List.of(2, 3, 5), result);
    }

    /**
     * Tests handling of a query string array of string type
     */
    @Test
    public void testStringListInQueryString() throws Exception {
        Method sortStringList = handler.getClass().getMethod("sortStringList", List.class);
        Route route = routeFactory.createRoute(sortStringList, handler);

        Map<String, List<String>> queryParams =
                Collections.singletonMap("myList", List.of("foo", "bar", "baz", "$tr:ng"));
        Request req = createMultiValueRequest("/manager/api/test/sortStringList", queryParams);
        Response res = createResponse();
        List<String> result = getResult((String) route.handle(req, res),
                TypeToken.getParameterized(List.class, String.class).getType());

        assertEquals(List.of("$tr:ng", "bar", "baz", "foo"), result);
    }

    /**
     * When passed a single value, query string parameters are always interpreted as primitives rather than arrays.
     * This case tests if a single value is correctly passed to a handler method that expects a {@link List} as the
     * parameter.
     *
     * @see <a href="https://bugzilla.suse.com/show_bug.cgi?id=1207297">bsc#1207297</a>
     */
    @Test
    public void testOneElementListInQueryString() throws Exception {
        Method sortIntegerList = handler.getClass().getMethod("sortIntegerList", List.class);
        Route route = routeFactory.createRoute(sortIntegerList, handler);

        Map<String, String> queryParams = Collections.singletonMap("myList", "42");
        Request req = createRequest("/manager/api/test/sortIntegerList", queryParams);
        Response res = createResponse();
        List<Integer> result = getResult((String) route.handle(req, res), List.class);

        assertEquals(List.of(42), result);
    }

    /**
     * Tests handling of a string array in the request body
     */
    @Test
    public void testStringListInBody() throws Exception {
        Method sortStringList = handler.getClass().getMethod("sortStringList", List.class);
        Route route = routeFactory.createRoute(sortStringList, handler);

        Map<String, Object> bodyParams = Collections.singletonMap("myList", List.of("foo", "bar", "baz", "$tr:ng"));
        Request req = createRequest("/manager/api/test/sortStringList", GSON.toJson(bodyParams));
        Response res = createResponse();
        List<String> result = getResult((String) route.handle(req, res),
                TypeToken.getParameterized(List.class, String.class).getType());

        assertEquals(List.of("$tr:ng", "bar", "baz", "foo"), result);
    }

    /**
     * Tests an overloaded API endpoint
     */
    @Test
    public void testOverloadedEndpoint() throws Exception {
        Method overload1 = handler.getClass().getMethod("overloadedEndpoint", Integer.class);
        Method overload2 = handler.getClass().getMethod("overloadedEndpoint", Integer.class, Integer.class);
        Route route = routeFactory.createRoute(List.of(overload1, overload2), handler);

        Request req = createRequest("/manager/api/test/overloadedEndpoint", Map.of("myInteger1", "1"));
        Response res = createResponse();

        int result = getResult((String) route.handle(req, res), Integer.class);
        assertEquals(1, result);

        req = createRequest("/manager/api/test/overloadedEndpoint", Map.of("myInteger1", "1", "myInteger2", "2"));
        res = createResponse();

        result = getResult((String) route.handle(req, res), Integer.class);
        assertEquals(2, result);
    }

    /**
     * Tests handling of a map of mixed types
     */
    @Test
    public void testObjectMap() throws Exception {
        Method mapKeysToSet = handler.getClass().getMethod("mapKeysToSet", Map.class, Integer.class);
        Route route = routeFactory.createRoute(mapKeysToSet, handler);

        Map<String, Object> bodyArgs = Map.of(
                "myMap", Map.of("foo", 1, "bar", "two", "baz", "3"),
                "numKeys", 3);
        Request req = createRequest("/manager/api/test/mapKeysToSet", GSON.toJson(bodyArgs));
        Response res = createResponse();

        Set<String> result = getResult((String) route.handle(req, res), Set.class);
        assertEquals(Set.of("foo", "bar", "baz"), result);
    }

    /**
     * Tests handling of a list of maps of mixed types
     */
    @Test
    public void testListOfObjects() throws Exception {
        Method listOfMaps = handler.getClass().getMethod("listOfMaps", List.class);
        Route route = routeFactory.createRoute(listOfMaps, handler);

        List<Map<String, Object>> list = List.of(
                Map.of("foo", 1, "bar", "two"),
                Map.of("baz", "3"));
        Request req = createRequest("/manager/api/test/listOfMaps",
                GSON.toJson(Collections.singletonMap("myList", list)));
        Response res = createResponse();

        List<Object> result = getResult((String) route.handle(req, res), List.class);
        assertTrue(result.containsAll(List.of(1, "two", "3")));
    }

    /**
     * Tests handling of invalid argument types
     */
    @Test
    public void testParameterTypeMismatch() throws Exception {
        Method overload1 = handler.getClass().getMethod("overloadedEndpoint", Integer.class);
        Method overload2 = handler.getClass().getMethod("overloadedEndpoint", Integer.class, Integer.class);
        Route route = routeFactory.createRoute(List.of(overload1, overload2), handler);

        Request req1 = createRequest("/manager/api/test/overloadedEndpoint", Map.of("myInteger1", "foo"));
        Response res1 = createResponse();

        HaltException e = assertThrows(HaltException.class, () -> route.handle(req1, res1));
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.statusCode());

        Request req2 = createRequest("/manager/api/test/overloadedEndpoint",
                Map.of("myInteger1", "foo", "myInteger2", "2"));
        Response res2 = createResponse();

        e = assertThrows(HaltException.class, () -> route.handle(req2, res2));
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.statusCode());
    }

    /**
     * Tests API exception output
     */
    @Test
    public void testMethodException() throws Exception {
        Method failing = handler.getClass().getMethod("failing");
        Route route = routeFactory.createRoute(failing, handler);

        Request req = createRequest("/manager/api/test/failing");
        Response res = createResponse();

        String result = getExceptionResult((String) route.handle(req, res));
        assertEquals("Test API exception", result);
    }

    /**
     * Tests forbidding object-like parameters in the query string
     */
    @Test
    public void testComplexQueryParam() throws Exception {
        Method listOfMaps = handler.getClass().getMethod("listOfMaps", List.class);
        Route route = routeFactory.createRoute(listOfMaps, handler);

        List<Map<String, Object>> list = List.of(
                Map.of("foo", 1, "bar", "two"),
                Map.of("baz", "3"));
        Request req = createRequest("/manager/api/test/listOfMaps", Map.of("myList", GSON.toJson(list)));
        Response res = createResponse();

        HaltException e = assertThrows(HaltException.class, () -> route.handle(req, res));
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.statusCode());
    }

    /**
     * Tests default serialization of API response objects
     */
    @Test
    public void testDefaultSerialization() throws Exception {
        RouteFactory noSerializerFactory = new RouteFactory(new SerializerFactory() {
            @Override
            public List<ApiResponseSerializer<?>> getSerializers() {
                return Collections.emptyList();
            }
        });

        Method customResponse = handler.getClass().getMethod("customResponse", Integer.class, String.class);
        Route route = noSerializerFactory.createRoute(customResponse, handler);

        Request req = createRequest("/manager/api/test/customResponse", Map.of("myInteger", "1", "myString", "foo"));
        Response res = createResponse();

        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertFalse(result.containsKey("isCustomSerialized"));
        assertEquals(1, result.get("myInteger"));
        assertEquals("foo", result.get("myString"));
        assertEquals(2, result.size());
    }

    /**
     * Tests custom serializers
     */
    @Test
    public void testCustomSerializer() throws Exception {
        Method customResponse = handler.getClass().getMethod("customResponse", Integer.class, String.class);
        Route route = routeFactory.createRoute(customResponse, handler);

        Request req = createRequest("/manager/api/test/customResponse", Map.of("myInteger", "1", "myString", "foo"));
        Response res = createResponse();

        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(true, result.get("isCustomSerialized"));
        assertEquals(1, result.get("myInteger"));
        assertEquals("foo", result.get("myString"));
        assertEquals(3, result.size());
    }

    /**
     * Tests custom serialization of a subclass
     */
    @Test
    public void testCustomSerializerWithSubclass() throws Exception {
        Method customResponseSubclass =
                handler.getClass().getMethod("customResponseSubclass", Integer.class, String.class);
        Route route = routeFactory.createRoute(customResponseSubclass, handler);

        Request req = createRequest("/manager/api/test/customResponseSubclass",
                Map.of("myInteger", "1", "myString", "foo"));
        Response res = createResponse();

        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(true, result.get("isCustomSerialized"));
        assertEquals(1, result.get("myInteger"));
        assertEquals("foo", result.get("myString"));
        assertEquals(3, result.size());
    }

    /**
     * Tests custom serialization of a subclass with its own serializer
     *
     * Serializers that serialize classes in the same class hierarchy override each other in the order they are
     * added. To ensure that subclass serializers take precedence, they must be added later than serializers of their
     * parent classes. In this test, the serializers are defined in wrong order to test if {@link RouteFactory} handles
     * the mentioned case properly.
     */
    @Test
    public void testCustomSerializerWithSerializedSubclass() throws Exception {
        // Create a factory with custom serializers for the case
        RouteFactory customSerializedRouteFactory = new RouteFactory(new SerializerFactory() {
            @Override
            public List<ApiResponseSerializer<?>> getSerializers() {
                return List.of(
                        // Additional serializer for the subclass itself (must be registered last)
                        new ApiResponseSerializer<TestResponseSubclass>() {
                            @Override
                            public SerializedApiResponse serialize(TestResponseSubclass src) {
                                return new SerializationBuilder()
                                        .add("serializedBySubclass", true)
                                        .build();
                            }

                            @Override
                            public Class<TestResponseSubclass> getSupportedClass() {
                                return TestResponseSubclass.class;
                            }
                        }, new TestSerializer());
            }
        });

        Method customResponseSubclass =
                handler.getClass().getMethod("customResponseSubclass", Integer.class, String.class);
        Route route = customSerializedRouteFactory.createRoute(customResponseSubclass, handler);

        Request req = createRequest("/manager/api/test/customResponseSubclass",
                Map.of("myInteger", "1", "myString", "foo"));
        Response res = createResponse();

        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        // Assert that the result is serialized properly by the serializer of the subclass
        assertEquals(true, result.get("serializedBySubclass"));
        assertEquals(1, result.size());
    }

    /**
     * Tests chain serialization of complex objects
     */
    @Test
    public void testChainSerialization() throws Exception {
        Method complexResponse = handler.getClass().getMethod("complexResponse", String.class, Map.class);
        Route route = routeFactory.createRoute(complexResponse, handler);

        Request req = createRequest("/manager/api/test/complexResponse",
                GSON.toJson(
                        Map.of(
                                "myString", "$+r:ng",
                                "nestedObjProps", Map.of(
                                        "myInteger", 3,
                                        "myString", "foo")
                        )));
        Response res = createResponse();

        Map<String, Object> result = getResult((String) route.handle(req, res), Map.class);

        // Parent object
        assertEquals(true, result.get("isCustomSerialized"));
        assertEquals("$+r:ng", result.get("myString"));

        // Nested object
        @SuppressWarnings("unchecked")
        Map<String, Object> nested = (Map<String, Object>) result.get("myObject");
        assertEquals(true, nested.get("isCustomSerialized"));
        assertEquals("foo", nested.get("myString"));
        assertEquals(3, nested.get("myInteger"));
    }

    /**
     * Tests lists of custom serialized objects
     */
    @Test
    public void testCustomSerializedList() throws Exception {
        Method customResponseList = handler.getClass().getMethod("customResponseList", String.class, String.class);
        Route route = routeFactory.createRoute(customResponseList, handler);

        Request req = createRequest("/manager/api/test/customResponseList",
                Map.of("myString1", "foo", "myString2", "bar"));
        Response res = createResponse();

        List<Map<String, Object>> result = getResult((String) route.handle(req, res), List.class);

        assertTrue(result.stream().allMatch(i -> (boolean) i.get("isCustomSerialized")));
        assertEquals(1, result.get(0).get("myInteger"));
        assertEquals("foo", result.get(0).get("myString"));
        assertEquals(2, result.get(1).get("myInteger"));
        assertEquals("bar", result.get(1).get("myString"));
    }

    /**
     * Tests lists of custom serialized objects
     */
    @Test
    public void testCustomSerializedMap() throws Exception {
        Method customResponseMap = handler.getClass().getMethod("customResponseMap", String.class, String.class);
        Route route = routeFactory.createRoute(customResponseMap, handler);

        Request req = createRequest("/manager/api/test/customResponseMap",
                Map.of("myString1", "foo", "myString2", "bar"));
        Response res = createResponse();

        Map<String, Map<String, Object>> result = getResult((String) route.handle(req, res), Map.class);

        assertEquals(2, result.size());
        assertTrue(result.values().stream().allMatch(i -> (boolean) i.get("isCustomSerialized")));
        assertEquals(1, result.get("1").get("myInteger"));
        assertEquals("foo", result.get("1").get("myString"));
        assertEquals(2, result.get("2").get("myInteger"));
        assertEquals("bar", result.get("2").get("myString"));
    }

    /**
     * Tests handling of arrays in query string
     */
    @Test
    public void testQueryStringArray() throws Exception {
        Method sortLongList = handler.getClass().getMethod("sortLongList", List.class);
        Route route = routeFactory.createRoute(sortLongList, handler);

        Request req = createMultiValueRequest("/manager/api/test/sortLongList",
                Map.of("myList", List.of("3", "5", "2")));
        Response res = createResponse();

        List<Long> result = getResult((String) route.handle(req, res), List.class);

        assertEquals(List.of(2, 3, 5), result);
    }

    /**
     * Unwraps the result object from the JSON response
     * @param response the JSON response
     * @param resultType the result type
     * @param <T> the result type
     * @return the result object
     */
    private <T> T getResult(String response, Type resultType) {
        JsonObject obj = parser.parse(response).getAsJsonObject();

        boolean isSuccess = obj.getAsJsonPrimitive("success").getAsBoolean();
        assertTrue(isSuccess);

        return GSON.fromJson(obj.get("result"), resultType);
    }

    /**
     * Unwraps the error message from an unsuccessful JSON response
     * @param response the JSON response
     * @return the response message
     */
    private String getExceptionResult(String response) {
        HttpApiResponse<?> responseObj = GSON.fromJson(response, HttpApiResponse.class);
        assertFalse(responseObj.isSuccess());
        return responseObj.getMessage();
    }

    /**
     * Sets up logged-in user in the session for a request
     * @param req the request
     * @param res the response
     */
    private void authorizeRequest(Request req, Response res, User user) {
        PxtSessionDelegateFactory factory = PxtSessionDelegateFactory.getInstance();
        PxtSessionDelegate pxtDelegate = factory.newPxtSessionDelegate();
        pxtDelegate.updateWebUserId(req.raw(), res.raw(), user.getId());
    }

    /**
     * Creates a {@link Request} for a URL
     * @param url the URL
     * @return the request object
     */
    private Request createRequest(String url) {
        return SparkTestUtils.createMockRequest(url);
    }

    /**
     * Creates a {@link Request} that has a body
     * @param url the URL
     * @param body the request body
     * @return the request object
     */
    private Request createRequest(String url, String body) {
        try {
            return SparkTestUtils.createMockRequestWithBody(url, Collections.emptyMap(), body);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link Request} that has query string arguments
     * @param url the URL
     * @param queryParams the query string key-value pairs
     * @return the request object
     */
    private Request createRequest(String url, Map<String, String> queryParams) {
        return SparkTestUtils.createMockRequestWithParams(url, queryParams);
    }

    /**
     * Creates a {@link Request} that has a body and query string arguments
     * @param url the URL
     * @param queryParams the query string key-value pairs
     * @param body the request body
     * @return the request object
     */
    private Request createRequest(String url, Map<String, String> queryParams, String body) {
        try {
            return SparkTestUtils.createMockRequestWithBodyAndParams(url, queryParams, body);
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a {@link Request} that has multi-value query string arguments
     * @param url the URL
     * @param multiValueQueryParams the query string key-value pairs
     * @return the request object
     */
    private Request createMultiValueRequest(String url, Map<String, List<String>> multiValueQueryParams) {
        return SparkTestUtils.createMockRequestWithMultiValueParams(url, multiValueQueryParams);
    }

    private Response createResponse() {
        return RequestResponseFactory.create(new RhnMockHttpServletResponse());
    }
}
