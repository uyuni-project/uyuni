/*
 * Copyright (c) 2014--2025 SUSE LLC
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
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.ProxySettingsConfigureSatelliteCommand;

import javax.servlet.http.HttpServletRequest;

/**
 * Domain logic for the Setup Wizard proxy settings page.
 */
public final class ProxySettingsManager {

    private ProxySettingsManager() { }

    /**
     * Get the current proxy settings from configuration.
     * @return proxy settings
     */
    public static ProxySettingsDto getProxySettings() {
        ProxySettingsDto settings = new ProxySettingsDto();
        settings.setHostname(Config.get().getString(ConfigDefaults.HTTP_PROXY));
        settings.setUsername(Config.get().getString(ConfigDefaults.HTTP_PROXY_USERNAME));
        return settings;
    }

    /**
     * Store the proxy settings in the Spacewalk configuration
     * @param settings DTO with the settings
     * @param userIn current logged in user
     * @param request the HTTP request
     * @return a list of validation errors
     */
    public static ValidatorError[] storeProxySettings(ProxySettingsDto settings,
            User userIn, HttpServletRequest request) {
        ProxySettingsConfigureSatelliteCommand configCommand = new ProxySettingsConfigureSatelliteCommand(userIn);
        configCommand.updateString(ConfigDefaults.HTTP_PROXY, settings.getHostname());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_USERNAME, settings.getUsername());
        configCommand.updateString(ConfigDefaults.HTTP_PROXY_PASSWORD, settings.getPassword());
        ValidatorError[] ret = configCommand.storeConfiguration();
        if (ret == null) {
            // Settings have changed, remove the cached subscriptions as proxy settings
            // might not be valid anymore
            SetupWizardSessionCache.clearAllSubscriptions(request);
        }
        return ret;
    }
}
