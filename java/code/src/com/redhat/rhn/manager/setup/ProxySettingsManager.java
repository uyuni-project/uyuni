/**
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
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.satellite.ConfigureSatelliteCommand;

import javax.servlet.http.HttpServletRequest;

/**
 * Domain logic for the Setup Wizard proxy settings page.
 */
public final class ProxySettingsManager {

    /** Configuration key for proxy hostname */
    public static final String KEY_PROXY_HOSTNAME = "server.satellite.http_proxy";
    /** Configuration key for proxy username */
    public static final String KEY_PROXY_USERNAME = "server.satellite.http_proxy_username";
    /** Configuration key for proxy password */
    public static final String KEY_PROXY_PASSWORD = "server.satellite.http_proxy_password";

    private ProxySettingsManager() { }

    /**
     * Get the current proxy settings from configuration.
     * @return proxy settings
     */
    public static ProxySettingsDto getProxySettings() {
        ProxySettingsDto settings = new ProxySettingsDto();
        settings.setHostname(Config.get().getString(KEY_PROXY_HOSTNAME));
        settings.setUsername(Config.get().getString(KEY_PROXY_USERNAME));
        settings.setPassword(Config.get().getString(KEY_PROXY_PASSWORD));
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
        ConfigureSatelliteCommand configCommand = new ConfigureSatelliteCommand(userIn);
        configCommand.updateString(KEY_PROXY_HOSTNAME, settings.getHostname());
        configCommand.updateString(KEY_PROXY_USERNAME, settings.getUsername());
        configCommand.updateString(KEY_PROXY_PASSWORD, settings.getPassword());
        ValidatorError[] ret = configCommand.storeConfiguration();
        if (ret == null) {
            // Settings have changed, remove the cached subscriptions as proxy settings
            // might not be valid anymore
            SetupWizardSessionCache.clearAllSubscriptions(request);
        }
        return ret;
    }
}
