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

import com.redhat.rhn.FaultException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.serializer.SerializerFactory;

import com.suse.manager.webui.utils.RouteWithUser;
import com.suse.manager.webui.utils.SparkApplicationHelper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;
import com.google.gson.ToNumberPolicy;

import org.apache.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import redstone.xmlrpc.XmlRpcCustomSerializer;
import spark.Route;
import spark.Spark;

/**
 * Factory class that creates HTTP API {@link Route}s from API handler methods
 */
public class RouteFactory {

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
     * serialized to JSON using a matching serializer and sent as the response.
     *
     * If the method does not match, or if a parameter can't be parsed, a 400 response is returned.
     *
     * If the invoked method throws a {@link FaultException}, a 500 response is returned and the exception message is
     * sent as the response body.
     * @param method the method to be matched with the specific route
     * @param handler the API handler from which the matched method will be invoked
     * @return the {@link Route}
     */
    public Route createRoute(Method method, BaseHandler handler) {
        return createRoute(Collections.singletonList(method), handler);
    }

    /**
     * Creates an API {@link Route} from a list of methods defined in an API handler
     *
     * The created {@link Route} will parse the request body as a JSON object and combine its properties together with
     * the query parameters and the authorized {@link User}, and match the list of parameters to the parameters of the
     * provided methods. If there is a match, it invokes the method from the specified handler. The return value is
     * serialized to JSON using a matching serializer and sent as the response.
     *
     * If no matching method is found, or if a parameter can't be parsed, a 400 response is returned.
     *
     * If the invoked method throws a {@link FaultException}, a 500 response is returned and the exception message is
     * sent as the response body.
     * @param methods the pool of methods to be matched with the specific route
     * @param handler the API handler from which the matched method will be invoked
     * @return the {@link Route}
     */
    public Route createRoute(List<Method> methods, BaseHandler handler) {
        RouteWithUser routeWithUser = (req, res, user) -> {
            // Collect all the parameters from the query string and the body
            Map<String, JsonElement> requestParams;
            try {
                requestParams = requestParser.parseQueryParams(req.queryMap().toMap());
                requestParams.putAll(requestParser.parseBody(req.body()));
            }
            catch (IllegalArgumentException e) {
                // TODO: Report in a better way?
                throw Spark.halt(HttpStatus.SC_BAD_REQUEST, e.getMessage());
            }

            try {
                // Find an overload matching the parameter names and types
                MethodCall call = findMethod(methods, requestParams, user);
                return SparkApplicationHelper.json(gson, res, call.invoke(handler));
            }
            catch (NoSuchMethodException e) {
                throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
            }
            catch (IllegalAccessException e) {
                // Should not happen since we're only evaluating public methods
                throw new RuntimeException(e);
            }
            catch (InvocationTargetException e) {
                Throwable exceptionInMethod = e.getCause();
                if (exceptionInMethod instanceof FaultException) {
                    throw Spark.halt(HttpStatus.SC_INTERNAL_SERVER_ERROR, exceptionInMethod.getLocalizedMessage());
                }
                throw new RuntimeException(exceptionInMethod);
            }
        };
        return SparkApplicationHelper.withUser(routeWithUser);
    }

    /**
     * Finds a single method matching the specified JSON argument names and types
     *
     * Type matching is performed by trying to parse every argument according to a method's parameter types
     * The parsed arguments are packed together with the chosen method and returned as a {@link MethodCall} object.
     * @param methods list of methods
     * @param jsonArgs the JSON arguments
     * @param user the logged-in user
     * @return the matched method, if exists
     * @throws NoSuchMethodException if no match is found
     */
    private MethodCall findMethod(List<Method> methods, Map<String, JsonElement> jsonArgs, User user)
            throws NoSuchMethodException {
        // TODO: Simplify / explain
        return methods.stream()
                // Filter methods with matching parameter names
                .filter(m -> jsonArgs.keySet().equals(
                        Arrays.stream(m.getParameters())
                                .filter(p -> !User.class.equals(p.getType()))
                                .map(Parameter::getName)
                                .collect(Collectors.toSet())))
                // Try to parse arguments according to method parameter types
                .map(method -> {
                    List<Object> args = new ArrayList<>(method.getParameterCount());
                    for (Parameter param : method.getParameters()) {
                        if (User.class.equals(param.getType())) {
                            args.add(user);
                            continue;
                        }
                        try {
                            args.add(requestParser.parseValue(jsonArgs.get(param.getName()), param.getType()));
                        }
                        catch (Exception e) {
                            // Type mismatch, skip the method
                            // TODO: Log
                            return null;
                        }
                    }
                    return new MethodCall(method, args.toArray());
                })
                .collect(toUnique())
                .orElseThrow(() -> new NoSuchMethodException("Can't find a method match with proposed parameters."));
    }

    /**
     * Initializes a {@link Gson} instance, registering the custom serializers that the specified
     * {@link SerializerFactory} provides
     * @return the {@link Gson} instance
     */
    private Gson initGsonWithSerializers() {
        //TODO: Update GSON to >= 2.8.9 for below to work, otherwise GSON parses every number as double
        GsonBuilder builder = new GsonBuilder()
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE);

        for (XmlRpcCustomSerializer serializer : serializerFactory.getSerializers()) {
            if (serializer instanceof JsonSerializer) {
                // Serialize subclasses as well
                builder.registerTypeHierarchyAdapter(serializer.getSupportedClass(), serializer);
            }
        }
        return builder.create();
    }
}
