/*
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.manager.setup;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.manager.content.MgrSyncUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utility methods for caching data in the HttpSession object.
 */
public class SetupWizardSessionCache {

    // Logger for this class
    private static Logger logger = LogManager.getLogger(SetupWizardSessionCache.class);

    // Session attribute keys
    private static final String SUBSCRIPTIONS_KEY = "SETUP_WIZARD_SUBSCRIPTIONS";
    private static final String PROXY_STATUS_KEY = "SETUP_WIZARD_PROXY_STATUS";

    /**
     * Private constructor for utility class.
     */
    private SetupWizardSessionCache() {
    }

    /**
     * Retrieve subscriptions from cache for the given credentials.
     * @param creds credentials
     * @param request request
     * @return list of subscriptions
     */
    @SuppressWarnings("unchecked")
    public static List<SubscriptionDto> getSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request) {
        List<SubscriptionDto> ret = null;
        HttpSession session = request.getSession();
        Map<String, List<SubscriptionDto>> subsMap =
                (Map<String, List<SubscriptionDto>>) session
                        .getAttribute(SUBSCRIPTIONS_KEY);
        if (subsMap != null) {
            ret = subsMap.get(creds.getUser());
        }
        return ret;
    }

    /**
     * Check if the verification status of any given credentials is unknown.
     * @param creds credentials
     * @param request request
     * @return true if verification status is unknown for the given creds, otherwise false.
     */
    @SuppressWarnings("unchecked")
    public static boolean credentialsStatusUnknown(MirrorCredentialsDto creds,
            HttpServletRequest request) {
        boolean ret = true;
        HttpSession session = request.getSession();
        Map<String, List<SubscriptionDto>> subsMap = (Map<String, List<SubscriptionDto>>)
                session.getAttribute(SUBSCRIPTIONS_KEY);
        if (subsMap != null && subsMap.containsKey(creds.getUser())) {
            ret = false;
        }
        return ret;
    }

    /**
     * Put a list of subscriptions in the session cache, while "null" is stored whenever the
     * verification status is "failed" for a given pair of credentials.
     * @param subscriptions subscriptions
     * @param creds the credentials
     * @param request request
     */
    @SuppressWarnings("unchecked")
    public static void storeSubscriptions(List<SubscriptionDto> subscriptions,
            MirrorCredentialsDto creds, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, List<SubscriptionDto>> subsMap =
                (Map<String, List<SubscriptionDto>>) session
                        .getAttribute(SUBSCRIPTIONS_KEY);

        // Create the map for caching if it doesn't exist
        if (subsMap == null) {
            subsMap = new HashMap<>();
            session.setAttribute(SUBSCRIPTIONS_KEY, subsMap);
        }

        // Store or update the subscriptions
        logger.debug("Storing subscriptions for " + creds.getUser());
        subsMap.put(creds.getUser(), subscriptions);
    }

    /**
     * Delete cached subscriptions for a given pair of credentials.
     * @param creds credentials
     * @param request the HTTP request object
     */
    @SuppressWarnings("unchecked")
    public static void clearSubscriptions(MirrorCredentialsDto creds,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        Map<String, List<SubscriptionDto>> subsMap =
                (Map<String, List<SubscriptionDto>>) session
                        .getAttribute(SUBSCRIPTIONS_KEY);
        if (subsMap != null) {
            subsMap.remove(creds.getUser());
            if (logger.isDebugEnabled()) {
                logger.debug("Removed subscriptions for: " + creds.getUser());
            }
        }
    }

    /**
     * Delete all cached subscriptions
     * @param request the HTTP request object
     */
    public static void clearAllSubscriptions(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.removeAttribute(SUBSCRIPTIONS_KEY);
    }

    /**
     * Get the proxy status (with caching).
     * @param forceRefresh to force cache refresh
     * @param request the HTTP request object
     * @return true if validation successful, false otherwise.
     */
    public static boolean getProxyStatus(boolean forceRefresh, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Boolean ret = (Boolean) session.getAttribute(PROXY_STATUS_KEY);

        // Ping SCC if refresh is enforced or status is unknown
        if (forceRefresh || ret == null) {
            try {
                String url = Config.get().getString(ConfigDefaults.SCC_URL) + "/login";
                ret = MgrSyncUtils.verifyProxy(url);
            }
            catch (IOException e) {
                ret = false;
            }

            // Put validation status in cache
            if (logger.isDebugEnabled()) {
                logger.debug("Proxy verification is " + ret);
            }
            storeProxyStatus(ret, request);
        }
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieved proxy status from cache: " + ret);
            }
        }

        return ret;
    }

    /**
     * Put the proxy validation status in session cache.
     * @param proxyStatus validation status
     * @param request request
     */
    public static void storeProxyStatus(boolean proxyStatus,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute(PROXY_STATUS_KEY, proxyStatus);
        if (logger.isDebugEnabled()) {
            logger.debug("Proxy status stored in session: " + proxyStatus);
        }
    }
}
