/**
 * Copyright (c) 2014 SUSE
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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.setup.ProxySettingsDto;
import com.redhat.rhn.manager.setup.SetupWizardManager;
import com.redhat.rhn.manager.setup.SetupWizardSessionCache;

public class HttpProxyRenderer {
    // The logger for this class
    private static final Logger logger = Logger.getLogger(HttpProxyRenderer.class);

    /**
     * Add a new pair of credentials and re-render the whole list.
     * @param settings map with the keys hostname, username, password
     * @return saved settings
     */
    public ProxySettingsDto saveProxySettings(ProxySettingsDto settings) {
        ProxySettingsDto oldSettings = retrieveProxySettings();
        if (oldSettings.equals(settings)) {
            return settings;
        }

        // Find the current user
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        RequestContext rhnContext = new RequestContext(request);
        User webUser = rhnContext.getCurrentUser();

        if (logger.isDebugEnabled()) {
            logger.debug("Saving proxy settings: " + settings.toString());
        }

        // TODO: Handle errors
        ValidatorError[] errors = SetupWizardManager.storeProxySettings(settings, webUser, request);
        if (errors != null) {
            for (ValidatorError error : errors) {
                logger.error("error: " + error.toString());
            }
        }
        return settings;
    }

    /**
     * Retrieve the proxy settings from configuration.
     * @return The current configured proxy settings
     */
    public ProxySettingsDto retrieveProxySettings() {
        return SetupWizardManager.getProxySettings();
    }

    /**
     * Verify the configured proxy settings with NCC.
     * @return true if the proxy works, false otherwise.
     */
    public boolean verifyProxySettings(boolean refreshCache) {
        WebContext webContext = WebContextFactory.get();
        HttpServletRequest request = webContext.getHttpServletRequest();
        return SetupWizardSessionCache.getProxyStatus(refreshCache, request);
    }
}
