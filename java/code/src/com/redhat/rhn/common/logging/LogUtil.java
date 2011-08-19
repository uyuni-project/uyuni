package com.redhat.rhn.common.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

/**
 * Utility methods for logging.
 */
public class LogUtil {

    // Keys to be used for the extmap
    private static final String EXTMAP_KEY_EVTSRC = "EVT.SRC";
    private static final String EXTMAP_KEY_EVTTYPE = "EVT.TYPE";
    private static final String EXTMAP_KEY_EVTURL = "EVT.URL";
    private static final String EXTMAP_KEY_USRID = "USR.ID";
    private static final String EXTMAP_KEY_USRORGID = "USR.ORGID";

    // Values to be used for the extmap
    private static final String EXTMAP_VALUE_WEBUI = "WEBUI";
    private static final String EXTMAP_VALUE_FRONTEND_API = "FRONTEND_API";

    // Namespace for request parameters
    private static final String EXTMAP_PREFIX_REQUEST = "REQ.";

    // Request parameters to be ignored
    private static List<String> IGNORED_PARAMS = Collections
            .unmodifiableList(Arrays.asList("password", "desiredpassword",
                    "desiredpasswordConfirm", "login_cb", "csrf_token",
                    "pxt:trap", "submitted", "formvars", "lower", "next_lower",
                    "prev_lower", "first_lower", "last_lower"));

    /**
     * Create a map of key/value pairs for logging of webui actions.
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
        extMap.put(EXTMAP_KEY_EVTURL, request.getRequestURI());

        // Put information about the request
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

    /**
     * Convert the {@link Object}s in an array to {@link String}
     * representations, but return them as {@link Object}s.
     *
     * @param params
     * @return array with {@link Object}s
     */
    public static Object[] convertParameters(Object[] params) {
        List<String> ret = new LinkedList<String>();
        for (Object o : params) {
            if (o instanceof String) {
                ret.add((String) o);
            } else if (o instanceof Action) {
                ret.add(LogUtil.getActionString((Action) o));
            } else if (o instanceof Channel) {
                ret.add(LogUtil.getChannelString((Channel) o));
            } else if (o instanceof CustomDataKey) {
                ret.add(LogUtil.getCustomDataKeyString((CustomDataKey) o));
            } else if (o instanceof Errata) {
                ret.add(LogUtil.getErrataString((Errata) o));
            } else if (o instanceof ManagedServerGroup) {
                ret.add(LogUtil.getGroupString((ManagedServerGroup) o));
            } else if (o instanceof Org) {
                ret.add(LogUtil.getOrgString((Org) o));
            } else if (o instanceof Server) {
                ret.add(LogUtil.getServerString((Server) o));
            } else if (o instanceof User) {
                ret.add(LogUtil.getUserString((User) o));
            } else {
                ret.add(o.toString());
            }
        }
        Object[] a = {};
        return ret.toArray(a);
    }

    // Private Methods ////////////////////////////////////////////////////////

    /**
     * Create a string representation of a given {@link Action}.
     *
     * @param action
     * @return
     */
    private static String getActionString(Action action) {
        String ret = "none";
        if (action != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(action.getName());
            builder.append("' (id ");
            builder.append(String.valueOf(action.getId()));
            builder.append(")");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link Channel}.
     *
     * @param channel
     * @return
     */
    private static String getChannelString(Channel channel) {
        String ret = "none";
        if (channel != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(channel.getName());
            builder.append("' (id ");
            builder.append(String.valueOf(channel.getId()));
            builder.append(")");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link CustomDataKey}.
     *
     * @param cdkey
     * @return
     */
    private static String getCustomDataKeyString(CustomDataKey cdkey) {
        String ret = "none";
        if (cdkey != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(cdkey.getLabel());
            builder.append("'");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link Errata}.
     *
     * @param errata
     * @return
     */
    private static String getErrataString(Errata errata) {
        String ret = "none";
        if (errata != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(errata.getAdvisoryName());
            builder.append("'");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link ManagedServerGroup}.
     *
     * @param group
     * @return
     */
    private static String getGroupString(ManagedServerGroup group) {
        String ret = "none";
        if (group != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(group.getName());
            builder.append("'");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link Org}.
     *
     * @param org
     * @return
     */
    private static String getOrgString(Org org) {
        String ret = "none";
        if (org != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(org.getName());
            builder.append("'");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link Server}.
     *
     * @param server
     * @return
     */
    private static String getServerString(Server server) {
        String ret = "none";
        if (server != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(server.getName());
            builder.append("' (id ");
            builder.append(String.valueOf(server.getId()));
            builder.append(", org_id ");
            builder.append(String.valueOf(server.getOrg().getId()));
            builder.append(")");
            ret = builder.toString();
        }
        return ret;
    }

    /**
     * Create a string representation of a given {@link User}.
     *
     * @param user
     * @return
     */
    private static String getUserString(User user) {
        String ret = "none";
        if (user != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("'");
            builder.append(user.getLogin());
            builder.append("' (id ");
            builder.append(String.valueOf(user.getId()));
            builder.append(", org_id ");
            builder.append(String.valueOf(user.getOrg().getId()));
            builder.append(")");
            ret = builder.toString();
        }
        return ret;
    }
}
