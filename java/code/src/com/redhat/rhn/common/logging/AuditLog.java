package com.redhat.rhn.common.logging;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

/**
 * Utility class providing static access to the audit log. Maybe make this a
 * factory for making it possible to exchange implementations.
 *
 * @author jrenner
 */
public class AuditLog {

    // Event types of user actions
    public static String LOGIN = "LOGIN";
    public static String LOGOUT = "LOGOUT";
    public static String ADDUSER = "ADD_USER";
    public static String EDITUSER = "EDIT_USER";
    public static String REMOVEUSER = "REMOVE_USER";
    public static String ASSIGNGRPROLE = "ASSIGN_GROUP_ROLE";
    public static String REVOKEGRPROLE = "REVOKE_GROUP_ROLE";
    public static String USERCREDUPDATE = "USER_CREDENTIAL_UPDATE";
    // General types of events
    public static String ACTION_CREATE = "ACTION_CREATE";
    public static String ACTION_CHANGE = "ACTION_CHANGE";
    public static String ACTION_DELETE = "ACTION_DELETE";

    // The name of the logger and a static reference
    private static String LOGGER_NAME = "auditlog";
    private static Logger log = Logger.getLogger(AuditLog.LOGGER_NAME);

    /**
     * Private constructor.
     */
    private AuditLog() {
    }

    /**
     * Log a Web UI action.
     *
     * @param eventType
     * @param request
     * @param callerClass
     * @param parameters
     */
    public static void log(String eventType, HttpServletRequest request,
            Class callerClass, Object... parameters) {
        if (!ConfigDefaults.get().isAuditEnabled()) {
            return;
        }
        log(false, eventType, request, callerClass, parameters);
    }

    /**
     * Log a failed Web UI action.
     *
     * @param eventType
     * @param request
     * @param callerClass
     * @param parameters
     */
    public static void logFailure(String eventType, HttpServletRequest request,
            Class callerClass, Object... parameters) {
        if (!ConfigDefaults.get().isAuditEnabled()) {
            return;
        }
        log(true, eventType, request, callerClass, parameters);
    }

    /**
     * Method for logging API calls.
     *
     * @param eventType
     * @param user
     * @param message
     * @param host
     */
    public static void logAPI(String eventType, User user, String message,
            String host) {
        if (!ConfigDefaults.get().isAuditEnabled()) {
            return;
        }
        log(user, message, host, LogUtil.createExtMapAPI(eventType, user));
    }

    // PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * This is a proxy method for logging Web UI actions.
     *
     * @param failure
     *            if false mark this as log of a failed action
     * @param request
     *            the {@link HttpServletRequest}
     * @param callerClass
     *            used as the key to lookup the log message
     */
    private static void log(boolean failure, String eventType,
            HttpServletRequest request, Class callerClass, Object... parameters) {
        // Determine the current user
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();

        // Determine the message using the name of the class as key
        String message;
        if (callerClass != null) {
            message = LocalizationService.getInstance().getMessage(
                    callerClass.getName(),
                    LogUtil.convertParameters(parameters));
        } else {
            message = request.getServletPath();
        }

        // Check if this is a failure log
        if (failure) {
            message = "FAILED: " + message;
        }

        // Call the logger
        log(user, message, request.getRemoteAddr(),
                LogUtil.createExtMap(eventType, user, request));
    }

    /**
     * Perform the actual call to the logger.
     *
     * @param uid
     * @param message
     * @param host
     * @param extmap
     */
    private static void log(User user, String message, String host,
            Map<String, String> extmap) {
        // Create the message object
        AuditLogMessage m = new AuditLogMessage();
        m.setUid(user != null ? user.getLogin() : "none");
        m.setMessage(message);
        m.setHost(host);
        m.setExtmap(extmap);
        log.info(m);
    }
}
