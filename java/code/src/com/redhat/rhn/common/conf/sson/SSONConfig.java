/**
 * Copyright (c) 2019 SUSE LLC
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
package com.redhat.rhn.common.conf.sson;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.suse.manager.webui.controllers.SAMLController;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A class to handle the configuration for Single Sign On (SSON)
 * The associated file is /usr/share/rhn/config-defaults/rhn_java_sson.conf (options can be overridden by copying
 * the associated option in /etc/rhn/rhn.conf)
 */
public final class SSONConfig {

    private static final Logger LOG = Logger.getLogger(SAMLController.class);

    private static Saml2Settings singletonConfig;

    private SSONConfig() {

        final Map<String, Object> samlData = new HashMap<>();
        Config.get().getNamespaceProperties(ConfigDefaults.get().SINGLE_SIGN_ON_ENABLED).forEach((k, v) -> {
            if (k.toString().startsWith(ConfigDefaults.get().SINGLE_SIGN_ON_ENABLED + ".")) {
                LOG.info("putting " + k.toString() + " into SAML configuration");
                samlData.put(k.toString().replace(
                        ConfigDefaults.get().SINGLE_SIGN_ON_ENABLED + ".", ""),
                        Config.get().getString((String) k));
            }
            final SettingsBuilder builder = new SettingsBuilder();
            singletonConfig = builder.fromValues(samlData).build();
        });
    }

    /**
     * A singleton to return the parsed configuration
     * @return the configuration for SSON in Saml2Settings object format
     */
    public static Optional<Saml2Settings> getSSONSettings() {
        if (ConfigDefaults.get().isSingleSignOnEnabled() && singletonConfig == null) {
            new SSONConfig();
        }
        return Optional.ofNullable(singletonConfig);
    }
}
