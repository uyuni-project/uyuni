/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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

package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.client.ClientCertificateDigester;
import com.redhat.rhn.common.client.InvalidCertificateException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.translation.TranslationException;
import com.redhat.rhn.common.translation.Translator;
import com.redhat.rhn.common.util.MethodUtil;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.session.SessionManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.api.ApiIgnore;
import com.suse.manager.api.ApiType;
import com.suse.manager.api.ReadOnly;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcInvocationHandler;

/**
 * A basic xmlrpc handler class.  Uses reflection + an arbitrary algorithm
 * to call the appropriate method on a subclass.  So, an xmlrpc call to
 * 'registration.privacy_message' might call
 * RegistrationHandler.privacyMessage
 */
public class BaseHandler implements XmlRpcInvocationHandler {
    public static final int VALID = 1;

    private static Logger log = LogManager.getLogger(BaseHandler.class);

    private static final String KEY_REGEX = "^[1-9][0-9]*x[a-f0-9]{64}$";

    protected boolean providesAuthentication() {
        return false;
    }

    /**
     * called by BaseHandler.doPost, contains the code that determines what
     * method to call of a subclassed-object
     *
     * @param methodCalled the xmlrpc function called, like
     *                     'registration.privacy_statement'
     * @param params a Vector of the parameters to methodCalled
     * @return the results of the method of the subclass
     * @exception XmlRpcFault if some error occurs
     */
    @Override
    public Object invoke(String methodCalled, List params) throws XmlRpcFault {
        Class<? extends BaseHandler> myClass = this.getClass();
        Method[] methods = Arrays.stream(myClass.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(BaseHandler::isMethodAvailable)
                .toArray(Method[]::new);

        String[] byNamespace = methodCalled.split("\\.");
        String beanifiedMethod = StringUtil.beanify(byNamespace[byNamespace.length - 1]);
        WebSession session = null;
        User user = null;

        if (!params.isEmpty() && params.get(0) instanceof String &&
                isSessionKey((String)params.get(0))) {
            if (!myClass.getName().endsWith("AuthHandler") &&
                !myClass.getName().endsWith("SearchHandler")) {
                session = SessionManager.loadSession((String)params.get(0));
                user = getLoggedInUser((String) params.get(0));
                params.set(0, user);
            }
        }

        //we've found all the methods that have the same number of parameters
        List<Method> matchedMethods = findMethods(methods, params, beanifiedMethod);

        //Attempt to find a perfect match
        Method foundMethod = findPerfectMethod(params, matchedMethods);
        Object[] converted = params.toArray();

        if (foundMethod == null) {
            Tuple2<Method, Object[]> fallbackMethod = findFallbackMethod(params, matchedMethods);
            foundMethod = fallbackMethod.getA();
            converted = fallbackMethod.getB();
        }
        XmlRpcLoggingInvocationProcessor.setCalledMethod(foundMethod);

        if (user != null && user.isReadOnly()) {
            if (!foundMethod.isAnnotationPresent(ReadOnly.class)) {
                throw new SecurityException("The " + beanifiedMethod + " API is not available to read-only API users");
            }
        }

        try {
            return foundMethod.invoke(this, converted);
        }
        catch (IllegalAccessException e) {
            throw new XmlRpcFault(-1, "unhandled internal exception");
        }
        catch (InvocationTargetException e) {
            Throwable cause = e.getCause();

            if (cause instanceof FaultException) {
                // FaultExceptions are "bad request" type of exceptions
                // Normally they should be thrown as response to the client but there's no need to log them as errors.
                FaultException fault = (FaultException) cause;
                log.debug("'{}' returned: [{}] {}", methodCalled, fault.getErrorCode(), fault.getMessage());
            }
            else {
                log.error("Error calling method: ", e);
                log.error("Caused by: ", cause);
            }

            /*
             * HACK: this should really be handled by SessionFilter.doFilter,
             * but unfortunately Redstone XMLRPC swallows our exceptions (see
             * XmlRpcDispatcher.dispatch()). Thus doFilter will never be reached
             * with exceptions, and will always COMMIT the transaction. To avoid
             * committing changes after an Exception, roll back here.
             */
            try {
                log.debug("Rolling back transaction");
                HibernateFactory.rollbackTransaction();
            }
            catch (HibernateException he) {
                log.error("Additional error during rollback", he);
            }

            // handle the exception based on the cause
            throw ExceptionTranslator.translateException(cause);
        }
        finally {
            if (session != null) {
                SessionManager.extendSessionLifetime(session);
            }
        }
    }

    private Tuple2<Method, Object[]> findFallbackMethod(
            List<Object> params, List<Method> matchedMethods) {

        Map<Boolean, List<Xor<TranslationException, Tuple2<Method, Object[]>>>> collect = matchedMethods
                .stream()
                .map(method -> {
                    Class<?>[] types = method.getParameterTypes();
                    Object[] converted = params.toArray();

                    Iterator<Object> iter = params.iterator();
                    for (int i = 0; i < types.length; i++) {
                        Object curr = iter.next();
                        if (!types[i].equals(curr.getClass())) {
                            try {
                                converted[i] = Translator.convert(curr, types[i]);
                            }
                            catch (TranslationException e) {
                                return Xor.<TranslationException, Tuple2<Method, Object[]>>left(e);
                            }
                        }
                    }
                    return Xor.<TranslationException, Tuple2<Method, Object[]>>right(new Tuple2<>(method, converted));

                }).collect(Collectors.partitioningBy(x -> x.isRight()));

        List<Tuple2<Method, Object[]>> candidates = collect.get(true).stream()
                .flatMap(x -> x.right().stream()).collect(Collectors.toList());

        List<TranslationException> exceptions = collect.get(false).stream()
                .flatMap(x -> x.left().stream()).collect(Collectors.toList());

        if (candidates.isEmpty()) {
           throw exceptions.get(0);
        }
        else if (candidates.size() == 1) {
            return candidates.get(0);
        }
        else  {
            throw new TranslationException("more than one method candidate found during conversion fallback");
        }
    }

    /**
     * Finds the perfect match for a method based upon type
     * @param params The parameters to find the match for.
     * @param matchedMethods the list of methods to check for a perfect match
     * @return null if no perfect match was found, otherwise the matched method.
     */
    private Method findPerfectMethod(List params, List<Method> matchedMethods) {
        //now lets try to find one that matches parameters exactly
        for (Method currMethod : matchedMethods) {
            if (log.isDebugEnabled()) {
                log.debug("findPerfectMethod test:{}", currMethod.toGenericString());
            }
            Class[] types = currMethod.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                if (log.isDebugEnabled()) {
                    log.debug("  findPerfectMethod: compare: {} isAssignableFrom {}", types[i].getCanonicalName(),
                            params.get(i).getClass().getCanonicalName());
                }
                //if we find a param that doesn't match, go to the next method
                if (!types[i].isAssignableFrom(params.get(i).getClass())) {
                    break;
                }
                //if we have gone through all of the params, and are here it is a
                //      perfect match.
                if (i == types.length - 1) {
                    log.debug("  all parameter match");
                    return currMethod;
                }
            }
        }
        return null;
    }

    /**
     * Private method to find the method in the java class that is being called
     * via xml-rpc
     * @param methods The methods contained in the class
     * @param params The parameters sent to us via xml-rpc
     * @param beanifiedMethod The method name we are looking for
     * @return The matching method we're looking for
     * @throws XmlRpcFault Thrown if we can't find the method asked for
     */
    /*
     * TODO: Make this method even smarterer.
     *      Currently this finds methods that match the number of parameters and returns
     *          those.
     */
    private List<Method> findMethods(Method[] methods, Collection params,
            String beanifiedMethod) throws XmlRpcFault {

        List<Method> toReturn = new ArrayList<>();

        //Loop through the methods array and find the one we are trying to call.
        for (Method methodIn : methods) {
            if (methodIn.getName().equals(beanifiedMethod)) {
                // We found a method with the right name, but does the parameter count
                // match?
                int numberOfParams = methodIn.getParameterTypes().length;
                if (numberOfParams == params.size()) {
                    //Method name and number of parameters match.
                    toReturn.add(methodIn);
                }
            }
        }
        if (toReturn.isEmpty()) {
            //The caller didn't get the method name or number of parameters right
            String message = "Could not find method: " + beanifiedMethod +
            " in class: " + this.getClass().getName() + " with params: [";
            for (Iterator iter = params.iterator(); iter.hasNext();) {
                Object param = iter.next();
                message += (param.getClass().getName());
                    if (iter.hasNext()) {
                        message = message + ", ";
                    }
            }
            message = message + "]";
            throw new XmlRpcFault(-1, message);
        }
        return toReturn;
    }

    /**
     * Gets the currently logged in user. This is all done through the sessionkey we send
     * the user in AuthHandler.login.
     * @param sessionKey The key containing the session id that we can use to load the
     * session.
     * @return Returns the user logged into the session corresponding to the given
     * sessionkey.
     */
    public static User getLoggedInUser(String sessionKey) {
        //Load the session
        WebSession session = SessionManager.loadSession(sessionKey);
        User user = session.getUser();

        //Make sure there was a valid user in the session. If not, the session is invalid.
        if (user == null) {
            throw new LookupException("Could not find a valid user for session with key: " +
                                      sessionKey);
        }

        //Return the logged in user
        return user;
    }

    /**
     * Private helper method to make sure a user has org admin role. If not, this will
     * throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is not an org admin
     */
    public static void ensureOrgAdmin(User user) throws PermissionCheckFailureException {
        ensureUserRole(user, RoleFactory.ORG_ADMIN);
    }

    /**
     * Private helper method to make sure a user has sat admin role. If not, this will
     * throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is not an sat admin
     */
    public static void ensureSatAdmin(User user) throws PermissionCheckFailureException {
        ensureUserRole(user, RoleFactory.SAT_ADMIN);
    }

    /**
     * Private helper method to make sure a user has system group admin role.
     * If not, this will throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is not a system group admin
     */
    public static void ensureSystemGroupAdmin(User user)
        throws PermissionCheckFailureException {
        ensureUserRole(user, RoleFactory.SYSTEM_GROUP_ADMIN);
    }

    /**
     * Private helper method to make sure a user has config admin role.
     * If not, this will throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is not a config admin.
     */
    public static void ensureConfigAdmin(User user)
        throws PermissionCheckFailureException {
        ensureUserRole(user, RoleFactory.CONFIG_ADMIN);
    }

    /**
     * Public helper method to make sure a user has either
     * an org admin or a config admin role
     * If not, this will throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is neither org nor config admin.
     */
    public static void ensureOrgOrConfigAdmin(User user)
        throws PermissionCheckFailureException {
        if (!user.hasRole(RoleFactory.ORG_ADMIN) &&
                !user.hasRole(RoleFactory.CONFIG_ADMIN)) {
            throw new PermissionCheckFailureException(RoleFactory.ORG_ADMIN,
                    RoleFactory.CONFIG_ADMIN);
        }
    }

    /**
     * Private helper method to make sure a user has image admin role.
     * If not, this will throw a generic Permission exception.
     * @param user The user to check
     * @throws PermissionCheckFailureException if user is not an image admin.
     */
    public static void ensureImageAdmin(User user)
        throws PermissionCheckFailureException {
        ensureUserRole(user, RoleFactory.IMAGE_ADMIN);
    }

    /**
     * Private helper method to make sure a user  the given role..
     * If not, this will throw a generic Permission exception.
     * @param user The user to check
     * @param role the role to check
     * @throws PermissionCheckFailureException if user does not
     *                      have the given role
     */
    public static void ensureUserRole(User user, Role role)
        throws PermissionCheckFailureException {
        if (!user.hasRole(role)) {
            throw new PermissionCheckFailureException(role);
        }
    }

    /**
     * Parse an input element uniformly for both XMLRPC and JSON APIs.
     * <p>
     * Useful when parsing input parameter values inside complex structs where there's no type information available.
     * <p>
     * XMLRPC and JSON APIs automatically parse the values for top-level parameters according to the type information
     * available. However, the values nested inside a complex struct must be parsed inside the specific handler method.
     *
     * @param argIn the input value
     * @return the parsed {@link T} object
     * @throws InvalidParameterException when the input cannot be parsed
     */
    protected static <T> T parseInputValue(Object argIn, Class<T> typeIn) throws InvalidParameterException {
        T value;
        try {
            if (typeIn.isAssignableFrom(argIn.getClass())) {
                // Assume exact type (XMLRPC)
                value = typeIn.cast(argIn);
            }
            else {
                // Interpret as string (JSON over HTTP)
                value = new Gson().fromJson("\"" + argIn + "\"", typeIn);
            }
        }
        catch (ClassCastException | JsonSyntaxException e) {
            throw new InvalidParameterException("Wrong input format", e);
        }

        return value;
    }

    /**
     * Ensure the org exists
     * @param orgId the org id to check
     * @return the org
     */
    protected Org verifyOrgExists(Number orgId) {
        if (orgId == null) {
            throw new NoSuchOrgException("null Id");
        }
        Org org = OrgFactory.lookupById(orgId.longValue());
        if (org == null) {
            throw new NoSuchOrgException(orgId.toString());
        }
        return org;
    }

    /**
     * Validate that specified entitlement names correspond to real entitlements
     * that can be changed via API (in other words, they are not permanent).
     *
     * @param entitlements List of string entitlement labels to be validated.
     */
    protected void validateEntitlements(List<Entitlement> entitlements) {

        for (Entitlement ent : entitlements) {
            if ((ent == null) || (ent.isPermanent())) {
                throw new InvalidEntitlementException();
            }
        }
    }

    /**
     * Validate that the keys provided in the map provided
     * by the user are valid.
     * @param validKeys Set of keys that are valid for this request
     * @param map The map to validate
     */
    protected void validateMap(Set<String> validKeys, Map map) {
        String errors = null;
        for (Object oIn : map.keySet()) {
            String key = (String) oIn;

            if (!validKeys.contains(key)) {
                // user passed an invalid key...
                if (errors == null) {
                    errors = new String(key);
                }
                else {
                    errors += ", " + key;
                }
            }
        }
        if (errors != null) {
            // at least one invalid key was found...
            throw new InvalidArgsException(errors);
        }
    }

    /**
     * Take an attributeName and value, and apply them to an Object.
     * Takes advantage of introspection and bean-stds to decide what call to make
     * @param attrName Attribute to set - assumes {@literal entity.set<Attrname>(value)}
     *  exists
     * @param entity The Object we are updating
     * @param value The new value to pass to {@literal set<AttrName>}
     */
    protected void setEntityAttribute(String attrName, Object entity, Object value) {
        String methodName = StringUtil.beanify("set_" + attrName);
        Object[] params = {
            value
        };
        MethodUtil.callMethod(entity, methodName, params);
    }


    protected Server validateClientCertificate(String clientCert) {
        StringReader rdr = new StringReader(clientCert);
        Server server = null;

        ClientCertificate cert;
        try {
            cert = ClientCertificateDigester.buildCertificate(rdr);
            server = SystemManager.lookupByCert(cert);
        }
        catch (IOException ioe) {
            log.error("IOException - Trying to access a system with an " +
                    "invalid certificate", ioe);
            throw new MethodInvalidParamException();
        }
        catch (SAXException se) {
            log.error("SAXException - Trying to access a " +
                    "system with an invalid certificate", se);
            throw new MethodInvalidParamException();
        }
        catch (InvalidCertificateException e) {
            log.error("InvalidCertificateException - Trying to access a " +
                    "system with an invalid certificate", e);
            throw new MethodInvalidParamException();
        }
        if (server == null) {
            throw new NoSuchSystemException();
        }

        return server;
    }

    private boolean isSessionKey(String string) {
        return string.matches(KEY_REGEX);
    }

    /**
     * Returns true if the method is available to be exposed in the XMLRPC interface
     * @param method the method
     * @return true if the method is available
     */
    private static boolean isMethodAvailable(Method method) {
        return !(method.isAnnotationPresent(ApiIgnore.class) &&
                Arrays.asList(method.getAnnotation(ApiIgnore.class).value()).contains(ApiType.XMLRPC));
    }
}
