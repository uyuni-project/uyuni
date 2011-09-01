package com.redhat.rhn.common.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.redhat.rhn.domain.user.User;

/**
 * Utility methods for logging.
 */
public class AuditLogUtil {

    // Keys to be used for the extmap
    private static final String EXTMAP_KEY_EVTSRC = "EVT.SRC";
    private static final String EXTMAP_KEY_EVTTYPE = "EVT.TYPE";
    private static final String EXTMAP_KEY_USRID = "USR.ID";
    private static final String EXTMAP_KEY_USRORGID = "USR.ORGID";

    // Values to be used for the extmap
    private static final String EXTMAP_VALUE_WEBUI = "WEBUI";
    private static final String EXTMAP_VALUE_FRONTEND_API = "FRONTEND_API";

    // Namespace for request parameters
    private static final String EXTMAP_PREFIX_REQUEST = "REQ.";

    // Request parameters that will not go into logs
    private static List<String> IGNORED_PARAMS = Collections
            .unmodifiableList(Arrays.asList("csrf_token", "login_cb",
                    "pxt:trap", "password", "desiredpassword",
                    "desiredpasswordConfirm", "rootPassword",
                    "rootPasswordConfirm", "lower", "prev_lower", "next_lower",
                    "first_lower", "last_lower"));

    /**
     * Create a map of key/value pairs for logging web requests.
     *
     * @param evtType
     * @param user
     * @param request
     * @return extMap
     */
    public static Map<String, String> createExtMap(String evtType, User user,
            HttpServletRequest request) {
        // Setup the return map
        Map<String, String> extMap = new TreeMap<String, String>();

        // Put information about the user
        if (user != null) {
            extMap.put(EXTMAP_KEY_USRID, String.valueOf(user.getId()));
            extMap.put(EXTMAP_KEY_USRORGID,
                    String.valueOf(user.getOrg().getId()));
        }

        // Put information about the event
        extMap.put(EXTMAP_KEY_EVTSRC, EXTMAP_VALUE_WEBUI);
        if (evtType != null) {
            extMap.put(EXTMAP_KEY_EVTTYPE, evtType);
        }

        // Put request parameters
        extMap.putAll(getParameterMap(request));

        return extMap;
    }

    /**
     * Create a generic key/value map of request parameters from a
     * {@link HttpServletRequest}.
     *
     * @param request
     * @return {@link Map} of parameters (key, value)
     */
    private static Map<String, String> getParameterMap(
            HttpServletRequest request) {
        // Setup the return map
        Map<String, String> paramMap = new TreeMap<String, String>();

        // Iterate over all parameters in the request
        Enumeration items = request.getParameterNames();
        while (items.hasMoreElements()) {
            String name = (String) items.nextElement();
            // Check with the list of ignored params
            if (IGNORED_PARAMS.contains(name)) {
                continue;
            }
            // Ignore these matches as well
            if (name.startsWith("list_") && !name.matches("list_.*_sel")) {
                continue;
            }
            if (name.contains("PAGE_SIZE_LABEL")) {
                continue;
            }
            String value = "";
            for (String s : request.getParameterValues(name)) {
                // Concatenate values if there is more
                if (!value.isEmpty()) {
                    value = value + ", " + s;
                    continue;
                }
                value = s;
            }
            paramMap.put(EXTMAP_PREFIX_REQUEST + name, value);
        }
        return paramMap;
    }

    /**
     * Create a map of key/value pairs for logging of API calls.
     *
     * @param evtType
     * @param user
     * @return extMap
     */
    public static Map<String, String> createExtMapAPI(String evtType, User user) {
        // Setup the return map
        Map<String, String> extMap = new TreeMap<String, String>();

        // Put information about the user
        if (user != null) {
            extMap.put(EXTMAP_KEY_USRID, String.valueOf(user.getId()));
            extMap.put(EXTMAP_KEY_USRORGID,
                    String.valueOf(user.getOrg().getId()));
        }

        // Put information about the event
        extMap.put(EXTMAP_KEY_EVTSRC, EXTMAP_VALUE_FRONTEND_API);
        if (evtType != null) {
            extMap.put(EXTMAP_KEY_EVTTYPE, evtType);
        }

        return extMap;
    }
}
