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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.yaml.snakeyaml.Yaml;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.logging.AuditLog;

/**
 * AuditLogFilter for generic logging of HTTP requests.
 *
 * @version $Rev$
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

    // Local boolean to check if logging is enabled
    private Boolean enabled = null;

    // Configuration objects
    private HashMap auditConfig;

    /** {@inheritDoc} */
    public void init(FilterConfig filterConfig) {
        // Put enabled into a local boolean
        if (enabled == null) {
            enabled = Config.get().getBoolean(ConfigDefaults.AUDIT_ENABLED);
        }

        // Load configuration from YAML file
        if (auditConfig == null) {
            Object obj = null;
            try {
                Yaml yaml = new Yaml();
                obj = yaml.load(new FileReader(configFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (obj instanceof HashMap) {
                auditConfig = (HashMap) obj;
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
        // Indicate if this request should be logged
        boolean log = false;
        // The current request and URI configuration
        HttpServletRequest request = null;
        Map uriConfig = null;

        if (enabled) {
            request = (HttpServletRequest) req;
            if (auditConfig.containsKey(request.getServletPath())) {
                // Get the configuration for this URI
                uriConfig = (HashMap) auditConfig.get(request.getServletPath());
                if (uriConfig != null) {
                    if (!hasRequiredParams(uriConfig, request)
                            || !dispatch(uriConfig, request)) {
                        // Do not log this request
                    } else if (logBefore(uriConfig)) {
                        // Log this request now
                        AuditLog.getInstance().log(false,
                                (String) uriConfig.get(KEY_TYPE), request);
                    } else {
                        // Log this request later
                        log = true;
                    }
                }
            }
        }

        // Finished for now
        chain.doFilter(req, resp);

        // Do the actual logging
        if (log) {
            // Check for errors
            if (request.getAttribute(Globals.ERROR_KEY) != null) {
                if (logFailures(uriConfig)) {
                    // Log a failure
                    AuditLog.getInstance().log(true,
                            (String) uriConfig.get(KEY_TYPE), request);
                }
            } else {
                AuditLog.getInstance().log(false,
                        (String) uriConfig.get(KEY_TYPE), request);
            }
        }
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
     * Check for a specific (internationalized!) value of 'dispatch'.
     *
     * @param uriConfig
     * @param request
     * @return true if dispatch has the expected value, else false
     */
    private boolean dispatch(Map uriConfig, HttpServletRequest request) {
        boolean ret = true;
        if (uriConfig.containsKey(KEY_DISPATCH)) {
            String value = LocalizationService.getInstance().getMessage(
                    (String) uriConfig.get(KEY_DISPATCH));
            String dispatch = request.getParameter("dispatch");
            if (dispatch == null || !value.equals(dispatch)) {
                ret = false;
            }
        }
        return ret;
    }
}
