/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.api;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.HandlerFactory;

import com.suse.manager.api.ReadOnly;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ApiHandler
 * Corresponds to API.pm in old perl code.
 * @apidoc.namespace api
 * @apidoc.doc Methods providing information about the API.
 */
public class ApiHandler extends BaseHandler {

    private final HandlerFactory handlers;

    /**
     * ApiHandler provides information about the xmlrpc api.
     * @param handlersIn HandlerFactory to introspect the xmlrpc api.
     */
    public ApiHandler(HandlerFactory handlersIn) {
        this.handlers = handlersIn;
    }

    /**
     * Returns the server version.
     * @return Returns the server version.
     *
     * @apidoc.doc Returns the server version.
     * @apidoc.returntype #param("string", "version")
     */
    public String systemVersion() {
        return ConfigDefaults.get().getProductVersion();
    }

    /**
     * Returns the api version. Called as: api.get_version
     * @return the api version.
     *
     * @apidoc.doc Returns the version of the API.
     * @apidoc.returntype #param("string", "version")
     */
    @ReadOnly
    public String getVersion() {
        return Config.get().getString("java.apiversion");
    }

    /** Lists available API namespaces
     * @param loggedInUser The current user
     * @return map of API namespaces
     *
     * @apidoc.doc Lists available API namespaces
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *   #struct_begin("namespace")
     *        #prop_desc("string", "namespace", "API namespace")
     *        #prop_desc("string", "handler", "API Handler")
     *   #struct_end()
     */
    @ReadOnly
    public Map<String, String> getApiNamespaces(User loggedInUser) {
        return handlers.getKeys().stream().collect(Collectors.toMap(
           namespace -> namespace,
           namespace -> StringUtil.getClassNameNoPackage(
                       handlers.getHandler(namespace).get().getClass())
        ));
    }

    /**
     * Lists all available api calls grouped by namespace
     * @param loggedInUser The current user
     * @return a map containing list of api calls for every namespace
     *
     * @apidoc.doc Lists all available api calls grouped by namespace
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *   #struct_begin("method_info")
     *       #prop_desc("string", "name", "method name")
     *       #prop_desc("string", "parameters", "method parameters")
     *       #prop_desc("string", "exceptions", "method exceptions")
     *       #prop_desc("string", "return", "method return type")
     *   #struct_end()
     */
    @ReadOnly
    public Map<String, Object> getApiCallList(User loggedInUser) {
        return handlers.getKeys().stream().collect(Collectors.toMap(
            namespace -> namespace,
            namespace -> getApiNamespaceCallList(loggedInUser, namespace)
        ));
    }

    /**
     * Lists all available api calls for the specified namespace
     * @param loggedInUser The current user
     * @param namespace namespace of interest
     * @return a map containing list of api calls for every namespace
     *
     * @apidoc.doc Lists all available api calls for the specified namespace
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "namespace")
     * @apidoc.returntype
     *   #struct_begin("method_info")
     *        #prop_desc("string", "name", "method name")
     *        #prop_desc("string", "parameters", "method parameters")
     *        #prop_desc("string", "exceptions", "method exceptions")
     *        #prop_desc("string", "return", "method return type")
     *   #struct_end()
     */
    @ReadOnly
    public Map getApiNamespaceCallList(User loggedInUser, String namespace) {
        Class<? extends BaseHandler> handlerClass =
                handlers.getHandler(namespace)
                        .orElseThrow(() -> new RuntimeException("Handler " + namespace + " not found."))
                        .getClass();
        Map<String, Map<String, Object>> methods  =
                new HashMap<>();

        for (Method method : handlerClass.getDeclaredMethods()) {

            if (0 != (method.getModifiers() & Modifier.PUBLIC)) {

                Map<String, Object> methodInfo = new HashMap<>();

                methodInfo.put("name", method.getName());

                List<String> paramList = new ArrayList<>();
                String paramListString = "";
                for (Type paramType : method.getParameterTypes()) {
                    String paramTypeString = getType(paramType,
                            StringUtils.isEmpty(paramListString));
                    paramList.add(paramTypeString);
                    paramListString += "_" + paramTypeString;
                }
                methodInfo.put("parameters", paramList);

                Set<String> exceptList = new HashSet<>();
                for (Class<?> exceptClass : method.getExceptionTypes()) {
                    exceptList.add(StringUtil.getClassNameNoPackage(exceptClass));
                }
                methodInfo.put("exceptions", exceptList);
                methodInfo.put("return", getType(method.getReturnType(), false));

                String methodName = namespace + "." +
                                    methodInfo.get("name") + paramListString;
                methods.put(methodName, methodInfo);
            }
        }
        return methods;
    }

    private String getType(Type classType, boolean firstParam) {
        if (classType.equals(String.class)) {
            return "string";
        }
        else if ((classType.equals(Integer.class)) ||
                 (classType.equals(int.class))) {
            return "int";
        }
        else if (classType.equals(Date.class)) {
            return "date";
        }
        else if (classType.equals(Boolean.class) ||
                 classType.equals(boolean.class)) {
            return "boolean";
        }
        else if (classType.equals(Map.class)) {
            return "struct";
        }
        else if ((classType.equals(List.class)) ||
                 (classType.equals(Set.class)) ||
                 (classType.toString().contains("class [L")) ||
                 (classType.toString().contains("class [I"))) {
            return "array";
        }
        else if (classType.toString().contains("class [B")) {
            return "base64";
        }
        else if (classType.equals(com.redhat.rhn.domain.user.User.class) && firstParam) {
            // this is a workaround needed due to ef911709..2765f023bd
            return "string";
        }
        else {
            return "struct";
        }
    }
}
