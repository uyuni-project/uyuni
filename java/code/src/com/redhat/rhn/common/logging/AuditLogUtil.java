/**
 * Copyright (c) 2011 Novell
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

package com.redhat.rhn.common.logging;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.redhat.rhn.frontend.struts.RequestContext;

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
    private static final List<String> IGNORED_PARAMS = Collections
            .unmodifiableList(Arrays.asList("csrf_token", "login_cb",
                    "pxt:trap", "password", "desiredpassword",
                    "desiredpasswordConfirm", "rootPassword",
                    "rootPasswordConfirm", "lower", "prev_lower", "next_lower",
                    "first_lower", "last_lower"));

    // Error message in case the backend is n/a
    private static final String MSG_NOT_AVAILABLE =
            "Log backend 'auditlog-keeper' is not available.";

    // The API method for sending ping to the backend
    private static final String METHOD_PING = "audit.ping";

    private AuditLogUtil() {
    }

    /**
     * Create a map of key/value pairs for logging web requests.
     *
     * @param evtType type of the event
     * @param user user
     * @param request request
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
     * @param request request
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
     * @param evtType type of the event
     * @param user user
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

    /**
     * Look for the backend: call ping() and do not care about the result.
     *
     * @throws AuditLogException if backend is not available
     */
    public static void ping() throws AuditLogException {
        String url = Config.get().getString(ConfigDefaults.AUDIT_SERVER);
        XmlRpcClient client = null;
        try {
            client = new XmlRpcClient(url, true);
            client.invoke(METHOD_PING, new ArrayList<Object>());
        }
        catch (MalformedURLException e) {
            throw new AuditLogException("Error initializing XML-RPC client", e);
        }
        catch (XmlRpcException e) {
            throw new AuditLogException(MSG_NOT_AVAILABLE, e);
        }
        catch (XmlRpcFault e) {
            throw new AuditLogException(MSG_NOT_AVAILABLE, e);
        }
        finally {
            client = null;
        }
    }

    /**
     * Send an error email.
     *
     * @param request request
     * @param t throwable
     */
    public static void sendErrorEmail(HttpServletRequest request, Throwable t) {
        TraceBackEvent evt = new TraceBackEvent();
        if (request != null) {
            RequestContext requestContext = new RequestContext(request);
            User usr = requestContext.getLoggedInUser();
            evt.setUser(usr);
            evt.setRequest(request);
        }
        evt.setException(t);
        MessageQueue.publish(evt);
    }
}
