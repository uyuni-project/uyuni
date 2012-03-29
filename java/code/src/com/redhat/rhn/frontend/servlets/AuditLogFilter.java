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

package com.redhat.rhn.frontend.servlets;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUpload;
import org.apache.struts.Globals;
import org.yaml.snakeyaml.Yaml;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.logging.AuditLog;
import com.redhat.rhn.common.logging.AuditLogException;
import com.redhat.rhn.common.logging.AuditLogMultipartRequest;
import com.redhat.rhn.common.logging.AuditLogUtil;
import com.redhat.rhn.frontend.context.Context;

/**
 * AuditLogFilter for generic logging of HTTP requests.
 */
public class AuditLogFilter implements Filter {

    // Path to the config file
    private final String configFile = "/usr/share/spacewalk/audit/auditlog-config.yaml";

    // Config keys
    private final String KEY_TYPE = "type";
    private final String KEY_REQUIRED = "required";
    private final String KEY_DISPATCH = "dispatch";
    private final String KEY_LOG_BEFORE = "log_before";
    private final String KEY_LOG_FAILURES = "log_failures";

    // Values of 'dispatch' corresponding to these keys will be globally ignored
    private String[] dispatchIgnoredKeys = {
            "Go", "Select All", "Unselect All", "Update List" };
    private Map<Locale, List<String>> dispatchIgnoredValues =
            new LinkedHashMap<Locale, List<String>>();

    // Local boolean to check if logging is enabled
    private Boolean enabled = null;

    // Indicate that the backend is not available
    private Boolean backendAvailable = false;

    // Configuration objects
    private HashMap auditConfig;

    /** {@inheritDoc} */
    public void init(FilterConfig filterConfig) {
        // Put 'audit.enabled' into a local boolean
        enabled = Config.get().getBoolean(ConfigDefaults.AUDIT_ENABLED);

        // Continue only if enabled
        if (enabled) {
            // Load configuration from YAML file
            Object obj = null;
            try {
                Yaml yaml = new Yaml();
                obj = yaml.load(new FileReader(configFile));
            }
            catch (FileNotFoundException e) {
                AuditLogUtil.sendErrorEmail(null, e);
            }
            if (obj instanceof HashMap) {
                auditConfig = (HashMap) obj;
            }

            // Check if the backend is available
            try {
                AuditLogUtil.ping();
                backendAvailable = true;
            }
            catch (AuditLogException e) {
                backendAvailable = false;
                AuditLogUtil.sendErrorEmail(null, e);
            }
        }
    }

    /** {@inheritDoc} */
    public void destroy() {
        auditConfig = null;
    }

    /** {@inheritDoc} */
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        // Indicators
        boolean log = false;
        boolean isMultipart = false;

        // The current HTTP request and logging configuration
        HttpServletRequest request = (HttpServletRequest) req;
        Map uriConfig = null;

        if (enabled) {
            // Get the configuration for this URI
            uriConfig = (HashMap) auditConfig.get(request.getServletPath());
            if (uriConfig != null) {

                // Check if the backend is available now if it wasn't before
                if (!backendAvailable) {
                    try {
                        AuditLogUtil.ping();
                        // It is available again
                        backendAvailable = true;
                    }
                    catch (AuditLogException e) {
                        throw e;
                    }
                }

                // If content is 'multipart/form-data', wrap the request
                if (FileUpload.isMultipartContent(request)) {
                    isMultipart = true;
                    request = new AuditLogMultipartRequest(request);
                }

                // Check the configuration for this URI
                if (!checkRequirements(uriConfig, request)) {
                    // Do not log this request
                }
                else if (logBefore(uriConfig)) {
                    // Log this request now
                    log(false, (String) uriConfig.get(KEY_TYPE), request);
                }
                else {
                    // Log this request later
                    log = true;
                }
            }
        }

        // Finished for now
        chain.doFilter(request, resp);

        // For multipart content check the requirements again, parameters may
        // in fact not have been available before!
        if (isMultipart && checkRequirements(uriConfig, request)) {
            log = true;
        }

        // Perform logging after processing the request
        if (log) {
            if (request.getAttribute(Globals.ERROR_KEY) == null) {
                log(false, (String) uriConfig.get(KEY_TYPE), request);
            }
            else if (logFailures(uriConfig)) {
                log(true, (String) uriConfig.get(KEY_TYPE), request);
            }
        }
    }

    /**
     * Wrapper method for calls to the logger.
     *
     * @param failure
     * @param evtType
     * @param request
     */
    private void log(boolean failure, String evtType, HttpServletRequest request) {
        try {
            AuditLog.getInstance().log(failure, evtType, request);
        }
        catch (AuditLogException e) {
            backendAvailable = false;
            AuditLogUtil.sendErrorEmail(request, e);
            throw e;
        }
    }

    /**
     * Check all of the parameter specific requirements for a given
     * {@link HttpServletRequest} and a URI configuration given as {@link Map}.
     *
     * @param uriConfig
     * @param request
     * @return true if all requirements are met, else false.
     */
    private boolean checkRequirements(Map uriConfig, HttpServletRequest request) {
        boolean success = hasRequiredParams(uriConfig, request) &&
                dispatch(uriConfig, request);
        return success;
    }

    /**
     * Check if all of the required parameters are contained in the request.
     *
     * @param uriConfig
     * @param request
     * @return true if required parameters are there, else false.
     */
    private boolean hasRequiredParams(Map uriConfig, HttpServletRequest request) {
        boolean ret = true;
        if (uriConfig.containsKey(KEY_REQUIRED)) {
            List requiredParams = (List) uriConfig.get(KEY_REQUIRED);
            for (Object key : requiredParams) {
                if (!request.getParameterMap().containsKey(key)) {
                    // Required parameter is not there
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Check for specific (internationalized!) values of 'dispatch' as well as
     * the list of globally ignored values.
     *
     * @param uriConfig
     * @param request
     * @return false if 'dispatch' contains a globally ignored value
     */
    private boolean dispatch(Map uriConfig, HttpServletRequest request) {
        boolean ret = true;
        // Check 'dispatch' for an explicit value
        String dispatch = request.getParameter("dispatch");
        if (uriConfig.containsKey(KEY_DISPATCH)) {
            String value = LocalizationService.getInstance().getMessage(
                    (String) uriConfig.get(KEY_DISPATCH));
            if (dispatch == null || !value.equals(dispatch)) {
                ret = false;
            }
        }
        else if (dispatch != null) {
            // Lookup the given value on localized list
            Locale locale = Context.getCurrentContext().getLocale();
            List<String> ignoredValues = dispatchIgnoredValues.get(locale);

            // Lazy initialize values for the current locale if necessary
            if (ignoredValues == null) {
                ignoredValues = getDispatchIgnoredValues(locale);
                dispatchIgnoredValues.put(locale, ignoredValues);
            }

            if (ignoredValues.contains(dispatch)) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * Check if this request should be logged before it goes to the server.
     *
     * @param uriConfig
     * @return true or false
     */
    private boolean logBefore(Map uriConfig) {
        boolean ret = false;
        if (uriConfig.containsKey(KEY_LOG_BEFORE)) {
            Object bool = uriConfig.get(KEY_LOG_BEFORE);
            if (bool instanceof Boolean) {
                ret = (Boolean) bool;
            }
        }
        return ret;
    }

    /**
     * Check if failures should be logged for this URL.
     *
     * @param uriConfig
     * @return true or false
     */
    private boolean logFailures(Map uriConfig) {
        boolean ret = false;
        if (uriConfig.containsKey(KEY_LOG_FAILURES)) {
            Object bool = uriConfig.get(KEY_LOG_FAILURES);
            if (bool instanceof Boolean) {
                ret = (Boolean) bool;
            }
        }
        return ret;
    }

    /**
     * A request is not going to be logged, if the request parameter 'dispatch'
     * has one of the values contained in these lists (one list per locale).
     *
     * @return unmodifiable list
     */
    private List<String> getDispatchIgnoredValues(Locale locale) {
        List<String> values = new ArrayList<String>();
        for (String s : dispatchIgnoredKeys) {
            values.add(LocalizationService.getInstance().getMessage(s, locale));
        }
        return Collections.unmodifiableList(values);
    }
}
