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
package com.redhat.rhn.frontend.action.renderers.setupwizard;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.ProxySettingsManager;
import com.redhat.rhn.manager.setup.SetupWizardSessionCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Exposes AJAX methods to work with proxy settings.
 */
public class ProxySettingsRenderer {

    /** Logger instance */
    private static Logger log = LogManager.getLogger(ProxySettingsRenderer.class);

    /**
     * Save the given proxy settings to the configuration.
     * @param request the request
     * @param settings map with the keys hostname, username, password
     * @return saved settings
     */
    public ProxySettingsDto saveProxySettings(HttpServletRequest request, ProxySettingsDto settings) {
        ProxySettingsDto oldSettings = retrieveProxySettings();
        if (oldSettings.equals(settings)) {
            return settings;
        }

        // Find the current user
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        if (log.isDebugEnabled()) {
            log.debug("Saving proxy settings: {}", settings);
        }

        // TODO: Handle errors
        ValidatorError[] errors =
                ProxySettingsManager.storeProxySettings(settings, webUser, request);
        if (errors != null) {
            for (ValidatorError error : errors) {
                log.error("error: {}", error);
            }
        }
        return settings;
    }

    /**
     * Retrieve the proxy settings from configuration.
     * @return The current configured proxy settings
     */
    public ProxySettingsDto retrieveProxySettings() {
        return ProxySettingsManager.getProxySettings();
    }

    /**
     * Verify the configured proxy settings with SUSE Customer Center.
     * @param request the request
     * @param refreshCache used to force a cache refresh
     * @return true if the proxy works, false otherwise.
     */
    public boolean verifyProxySettings(HttpServletRequest request, boolean refreshCache) {
        return SetupWizardSessionCache.getProxyStatus(refreshCache, request);
    }
}
