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
package com.suse.manager.api;


import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;
import com.redhat.rhn.manager.session.SessionManager;

import com.suse.utils.ParameterizedTypeImpl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import spark.Request;
import spark.Route;
import spark.Spark;

/**
 * Factory class that creates HTTP API {@link Route}s from API handler methods
 */
public class RouteFactory {

    private static final Logger LOG = LogManager.getLogger(RouteFactory.class);
    private final SerializerFactory serializerFactory;
    private final ApiRequestParser requestParser;
    private final Gson gson;

    /**
     * Constructs an instance with the default {@link SerializerFactory}
     *
     * Serializers registered in the {@link SerializerFactory} will be used to serialize the returned objects from the
     * created routes.
     */
    public RouteFactory() {
        this(new SerializerFactory());
    }

    /**
     * Constructs an instance with the specified {@link SerializerFactory}
     *
     * Serializers registered in the {@link SerializerFactory} will be used to serialize the returned objects from the
     * created routes.
     * @param serializerFactoryIn the serializer factory
     */
    public RouteFactory(SerializerFactory serializerFactoryIn) {
        this.serializerFactory = serializerFactoryIn;
        this.gson = initGsonWithSerializers();
        this.requestParser = new ApiRequestParser(gson);
    }

    /**
     * Returns a collector that returns the only value in the input elements as an {@link Optional}, excluding null
     * values
     * @param <T> input element type
     * @return an {@link Optional} of the unique element, can be empty
     */
    public static <T> Collector<T, ?, Optional<T>> toUnique() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    list = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
                    if (list.size() > 1) {
                        throw new IllegalStateException("Multiple items found.");
                    }
                    else if (list.size() == 1) {
                        return Optional.of(list.get(0));
                    }
                    else {
                        return Optional.empty();
                    }
                }
        );
    }

    /**
     * Creates an API {@link Route} from a method defined in an API handler
     *
     * The created {@link Route} will parse the request body as a JSON object and combine its properties together with
     * the query parameters and the authorized {@link User}, and match the list of parameters to the parameters of the
     * provided method. If there is a match, it invokes the method from the specified handler. The return value is
     * wrapped into a {@link HttpApiResponse} object and serialized to JSON using a matching serializer. The resulting
     * JSON string is returned as the {@link Route}'s return value.
     *
     * If the method does not match, or if a parameter can't be parsed, a 400 response is returned.
     *
     * If the invoked method throws a {@link FaultException}, an {@link HttpApiResponse} is created with
     * <code>success:false</code>, and it contains the exception message as the JSON object's <code>result</code>
     * property.
     * @param method the method to be matched with the specific route
     * @param handler the API handler from which the matched method will be invoked
     * @return the {@link Route}
     */
    public Route createRoute(Method method, BaseHandler handler) {
        return createRoute(Collections.singletonList(method), handler);
    }

    private Map<Type, Type> primToBoxed = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class
    );

    /**
     * Creates an API {@link Route} from a list of methods defined in an API handler
     *
     * The created {@link Route} will parse the request body as a JSON object and combine its properties together with
     * the query parameters and the authorized {@link User}, and match the list of parameters to the parameters of the
     * provided methods. If there is a match, it invokes the method from the specified handler. The return value is
     * wrapped into a {@link HttpApiResponse} object and serialized to JSON using a matching serializer. The resulting
     * JSON string is returned as the {@link Route}'s return value.
     *
     * If no matching method is found, or if a parameter can't be parsed, a 400 response is returned.
     *
     * If the invoked method throws a {@link FaultException}, an {@link HttpApiResponse} is created with
     * <code>success:false</code>, and it contains the exception message as the JSON object's <code>result</code>
     * property.
     * @param methods the pool of methods to be matched with the specific route
     * @param handler the API handler from which the matched method will be invoked
     * @return the {@link Route}
     */
    public Route createRoute(List<Method> methods, BaseHandler handler) {
        Route route = (req, res) -> {
            // Collect all the parameters from the query string and the body
            Map<String, JsonElement> requestParams;
            try {
                requestParams = requestParser.parseQueryParams(req.queryMap().toMap());
                requestParams.putAll(requestParser.parseBody(req.body()));
            }
            catch (ParseException e) {
                LOG.error(e.getMessage(), e);
                throw Spark.halt(HttpStatus.SC_BAD_REQUEST, e.getMessage());
            }

            String sessionKey = new RequestContext(req.raw()).getWebSession().getKey();
            try {
                // Find an overload matching the parameter names and types
                MethodCall call = findMethod(methods, requestParams, sessionKey, req);
                HttpApiResponse<?> response = HttpApiResponse.success(call.invoke(handler));

                Type genericReturnType = call.getMethod().getGenericReturnType();
                ParameterizedType parameterizedType = new ParameterizedTypeImpl(null, HttpApiResponse.class,
                        primToBoxed.getOrDefault(genericReturnType, genericReturnType));

                res.type("application/json");
                return gson.toJson(response, TypeToken.get(parameterizedType).getType());
            }
            catch (NoSuchMethodException e) {
                throw Spark.halt(HttpStatus.SC_BAD_REQUEST, e.getMessage());
            }
            catch (UserNotPermittedException e) {
                throw Spark.halt(HttpStatus.SC_FORBIDDEN, e.getMessage());
            }
            catch (IllegalAccessException e) {
                // Should not happen since we're only evaluating public methods
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e) {
                Throwable exceptionInMethod = e.getCause();
                if (exceptionInMethod instanceof FaultException) {
                    return json(gson, res,
                            HttpApiResponse.error(exceptionInMethod.getMessage()), new TypeToken<>() { });
                }
                throw new RuntimeException(exceptionInMethod);
            }
        };
        return asJson(route);
    }

    /**
     * Finds a single method matching the specified JSON argument names and types
     *
     * Type matching is performed by trying to parse every argument according to a method's parameter types
     * The parsed arguments are packed together with the chosen method and returned as a {@link MethodCall} object.
     * @param methods list of methods
     * @param jsonArgs the JSON arguments
     * @param sessionKey the session key
     * @param request the spark request
     * @return the matched method, if exists
     * @throws NoSuchMethodException if no match is found
     */
    private MethodCall findMethod(List<Method> methods, Map<String, JsonElement> jsonArgs,
                                  String sessionKey, Request request)
            throws NoSuchMethodException {
        User user = SessionManager.loadSession(sessionKey).getUser();
        // Filter methods with parameter names that match the request parameters, excluding the User parameter
        return methods.stream()
                .filter(m -> jsonArgs.keySet().equals(
                        Arrays.stream(m.getParameters())
                                .filter(p -> !(User.class.equals(p.getType()) ||
                                        HttpServletRequest.class.equals(p.getType())))
                                .map(Parameter::getName)
                                .filter(p -> !"sessionKey".equals(p))
                                .collect(Collectors.toSet())))
                // Try to parse arguments according to method parameter types
                .map(method -> {
                    List<Object> args = new ArrayList<>(method.getParameterCount());
                    for (Parameter param : method.getParameters()) {
                        // If the method contains a User parameter, add the current user to the argument list
                        if (User.class.equals(param.getType())) {
                            args.add(user);
                        }
                        else if (HttpServletRequest.class.equals(param.getType())) {
                            args.add(request.raw());
                        }
                        else if ("sessionKey".equals(param.getName())) {
                            args.add(sessionKey);
                        }
                        else {
                            JsonElement jsonArg = jsonArgs.get(param.getName());

                            // If the method expects a List, try to wrap the value in a JSON array
                            if (List.class.isAssignableFrom(param.getType()) && !jsonArg.isJsonArray()) {
                                JsonArray arr = new JsonArray(1);
                                arr.add(jsonArg);
                                jsonArg = arr;
                            }

                            try {
                                // Parse each value and add to the argument list
                                args.add(requestParser.parseValue(jsonArg, param.getType()));
                            }
                            catch (ParseException e) {
                                // Type mismatch, skip the method
                                LOG.debug(MessageFormat.format("Cannot parse {0} into {1}. Skipping current method.",
                                        param.getName(), param.getType().getSimpleName()));
                                return null;
                            }
                        }
                    }
                    return new MethodCall(method, args.toArray());
                })
                .collect(toUnique())
                .orElseThrow(() -> new NoSuchMethodException("No method exists with the matching parameters"));
    }

    /**
     * Initializes a {@link Gson} instance, registering the custom serializers that the specified
     * {@link SerializerFactory} provides
     * @return the {@link Gson} instance
     */
    private Gson initGsonWithSerializers() {
        GsonBuilder builder = new GsonBuilder()
                .registerTypeAdapter(Map.class, new MapDeserializer())
                .registerTypeAdapter(List.class, new ListDeserializer());

        // Serializers that serialize classes in the same class hierarchy override each other in the order they are
        // added. To ensure subclass serializers take precedence, they must be added later than serializers of their
        // parent classes.
        // See: RouteFactoryTest.testCustomSerializerWithSerializedSubclass
        serializerFactory.getSerializers().stream()
                // Sort the list to ensure serializers of subclasses are added after the serializers of parents
                .sorted(this::compareSerializerHierarchy)
                .forEach(s -> builder.registerTypeHierarchyAdapter(s.getSupportedClass(), s));

        return builder.create();
    }

    /**
     * Compare two {@link ApiResponseSerializer}s according to the class hierarchy of their supported classes
     *
     * A serializer of a type that is upper in the hierarchy comes before the other.
     * @param a the first serializer
     * @param b the second serializer
     * @return the comparison value
     */
    private int compareSerializerHierarchy(ApiResponseSerializer<?> a, ApiResponseSerializer<?> b) {
        Class<?> aClass = a.getSupportedClass();
        Class<?> bClass = b.getSupportedClass();
        return aClass.isAssignableFrom(bClass) ? -1 : (bClass.isAssignableFrom(aClass) ? 1 : 0);
    }
}
