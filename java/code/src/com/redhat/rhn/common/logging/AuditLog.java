package com.redhat.rhn.common.logging;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;

/**
 * Singleton class for providing access to the audit log.
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

    // The singleton instance
    private static AuditLog instance = null;

    /**
     * Private constructor.
     */
    private AuditLog() {
    }

    /**
     * Static method to get the singleton {@link AuditLog} instance.
     *
     * @return the singleton instance
     */
    public static synchronized AuditLog getInstance() {
        if (instance == null) {
            instance = new AuditLog();
        }
        return instance;
    }

    /**
     * Log a Web UI action.
     *
     * @param evtType
     * @param request
     * @param msgKey
     * @param parameters
     */
    public void log(String evtType, HttpServletRequest request, String msgKey,
            Object... parameters) {
        if (!Config.get().getBoolean(ConfigDefaults.AUDIT_ENABLED)) {
            return;
        }
        log(false, evtType, request, msgKey, parameters);
    }

    /**
     * Log a failed Web UI action.
     *
     * @param evtType
     * @param request
     * @param msgKey
     * @param parameters
     */
    public void logFailure(String evtType, HttpServletRequest request,
            String msgKey, Object... parameters) {
        if (!Config.get().getBoolean(ConfigDefaults.AUDIT_ENABLED)) {
            return;
        }
        log(true, evtType, request, msgKey, parameters);
    }

    /**
     * Method for logging API calls.
     *
     * @param evtType
     * @param user
     * @param message
     * @param host
     */
    public void logAPI(String eventType, User user, String message, String host) {
        if (!Config.get().getBoolean(ConfigDefaults.AUDIT_ENABLED)) {
            return;
        }
        log(user, message, host, LogUtil.createExtMapAPI(eventType, user));
    }

    // PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * This is a proxy method for logging web requests.
     *
     * @param failure
     * @param evtType
     * @param request
     * @param msgKey
     * @param parameters
     */
    private void log(boolean failure, String evtType,
            HttpServletRequest request, String msgKey, Object... parameters) {
        // Determine the current user
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();

        // Determine the message using the name of the class as key
        String message;
        if (msgKey != null) {
            message = LocalizationService.getInstance().getMessage(msgKey,
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
                LogUtil.createExtMap(evtType, user, request));
    }

    /**
     * Perform the actual call to the logger.
     *
     * @param user
     * @param message
     * @param host
     * @param extmap
     */
    private void log(User user, String message, String host,
            Map<String, String> extmap) {
        // Create the message object
        AuditLogMessage m = new AuditLogMessage();
        m.setUid(user != null ? user.getLogin() : "");
        m.setMessage(message);
        m.setHost(host);
        m.setExtmap(extmap);
        log.info(m);
    }
}
