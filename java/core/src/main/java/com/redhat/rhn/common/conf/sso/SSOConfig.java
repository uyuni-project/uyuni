/*
 * Copyright (c) 2019--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.common.conf.sso;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class to handle the configuration for Single Sign-On (SSO)
 * The associated file is /usr/share/rhn/config-defaults/rhn_java_sso.conf (options can be overridden by copying
 * the associated option in /etc/rhn/rhn.conf)
 */
public final class SSOConfig {

    private static final Logger LOG = LogManager.getLogger(SSOConfig.class);

    private SSOConfig() {
        // Prevent instantiation
    }

    /**
     * A singleton to return the parsed configuration
     * @return the configuration for SSO in Saml2Settings object format
     */
    public static Optional<Saml2Settings> getSSOSettings() {
        return Optional.ofNullable(Saml2SettingsHolder.INSTANCE);
    }

    // Using initialization-on-demand holder idiom to ensure thread safety
    private static class Saml2SettingsHolder {

        private static final Saml2Settings INSTANCE = getSingleSignOnConfiguration();

        private static Saml2Settings getSingleSignOnConfiguration() {
            if (!ConfigDefaults.get().isSingleSignOnEnabled()) {
                return null;
            }

            Map<String, Object> samlData = new HashMap<>();

            try {
                Config.get().getNamespaceProperties(ConfigDefaults.SINGLE_SIGN_ON_ENABLED).forEach((k, v) -> {
                    if (k.toString().startsWith(ConfigDefaults.SINGLE_SIGN_ON_ENABLED + ".")) {
                        LOG.info("putting {} into SAML configuration", k);
                        samlData.put(
                                k.toString().replace(ConfigDefaults.SINGLE_SIGN_ON_ENABLED + ".", ""),
                                Config.get().getString((String) k)
                        );
                    }
                });

                SettingsBuilder builder = new SettingsBuilder();
                return builder.fromValues(samlData).build();
            }
            catch (RuntimeException ex) {
                LOG.error("Unable to initialize SSO configuration", ex);
                return null;
            }
        }
    }
}
