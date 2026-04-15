/*
 * Copyright (c) 2025 SUSE LLC
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

package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class ProxySettingsConfigureSatelliteCommand extends ConfigureSatelliteCommand {

    private static Logger logger = LogManager.getLogger(ProxySettingsConfigureSatelliteCommand.class);

    private static final String RHN_CONFIG_SATELLITE_ENV_PREFIX = "UYUNICFG_";
    private static final String PWD_PLACEHOLDER = "PWD_PLACEHOLDER";
    protected String[] environmentVars;

    /**
     * Create a new ProxySettingsConfigureSatelliteCommand class with the
     * user requesting the config.
     *
     * @param userIn who wants to config the sat.
     */
    public ProxySettingsConfigureSatelliteCommand(User userIn) {
        super(userIn);
        environmentVars = null;
    }

    /**
     * Get the formatted String array of command line arguments to execute
     * when we call out to the system utility to store the config.
     *
     * @param configFilePath path to config file to update
     * @param optionMap      Map of key/value pairs to update local config with.
     * @param removals       List of keys that will be removed
     *                       Note that they have preference over the updated keys
     * @return String[] array of arguments.
     */
    @Override
    public String[] getCommandArguments(String configFilePath,
                                        Map<String, String> optionMap, List<String> removals) {

        if (optionMap.containsKey(ConfigDefaults.HTTP_PROXY_PASSWORD)) {
            String passwordEnvVariable = RHN_CONFIG_SATELLITE_ENV_PREFIX + PWD_PLACEHOLDER;
            String passwordValue = optionMap.get(ConfigDefaults.HTTP_PROXY_PASSWORD);
            if (null == passwordValue) {
                passwordValue = "";
            }

            optionMap.put(ConfigDefaults.HTTP_PROXY_PASSWORD, PWD_PLACEHOLDER);

            environmentVars = new String[]{"%s=%s".formatted(passwordEnvVariable, passwordValue)};

            return getCommandArguments(true, configFilePath, optionMap, removals);
        }
        else {
            return getCommandArguments(false, configFilePath, optionMap, removals);
        }
    }

    @Override
    protected ValidatorError[] executeStore() {
        String[] commandArguments = getCommandArguments();
        if (commandArguments != null) {
            SystemCommandExecutor e = new SystemCommandExecutor();
            int exitcode = e.execute(commandArguments, environmentVars);
            if (exitcode != 0) {
                ValidatorError[] retval = new ValidatorError[1];
                retval[0] = new ValidatorError("config.storeconfig.error",
                        Integer.toString(exitcode));

                if (logger.isDebugEnabled()) {
                    logger.debug("storeConfiguration() - end - return value={}", (Object) retval);
                }
                return retval;
            }
        }
        return null;
    }
}
