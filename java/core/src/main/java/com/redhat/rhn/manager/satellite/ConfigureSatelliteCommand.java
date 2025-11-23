/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ConfigureSatelliteCommand
 */
public class ConfigureSatelliteCommand extends BaseConfigureCommand
    implements SatelliteConfigurator {

    /**
     * Logger for this class
     */
    private static Logger logger = LogManager.getLogger(ConfigureSatelliteCommand.class);

    protected final List<String> keysToBeUpdated;

    /**
     * Create a new ConfigureSatelliteCommand class with the
     * user requesting the config.
     * @param userIn who wants to config the sat.
     */
    public ConfigureSatelliteCommand(User userIn) {
        super(userIn);
        this.keysToBeUpdated = new LinkedList<>();
    }

    /**
     * Get the formatted String array of command line arguments to execute
     * when we call out to the system utility to store the config.
     * @param configFilePath path to config file to update
     * @param optionMap Map of key/value pairs to update local config with.
     * @param removals List of keys that will be removed
     *   Note that they have preference over the updated keys
     * @return String[] array of arguments.
     */
    public String[] getCommandArguments(String configFilePath,
                                        Map<String, String> optionMap, List<String> removals) {
        return getCommandArguments(false, configFilePath, optionMap, removals);
    }

    protected String[] getCommandArguments(boolean hasEnvironmentVariables, String configFilePath,
                                           Map<String, String> optionMap, List<String> removals) {
        boolean somethingChanged = false;

        if (logger.isDebugEnabled()) {
            logger.debug("getCommandArguments(String configFilePath={}, Iterator keyIterator={}) - start",
                    configFilePath, optionMap);
        }

        List<String> argList = new LinkedList<>();
        argList.add("/usr/bin/sudo");
        if (hasEnvironmentVariables) {
            argList.add("-E");
        }
        argList.add("/usr/bin/rhn-config-satellite.pl");
        argList.add("--target=" + configFilePath);
        for (Map.Entry<String, String> entry : optionMap.entrySet()) {
            // We don't want to put the actual string 'null' in rhn.conf.  See bz: 189600
            argList.add("--option=%s=%s".formatted(entry.getKey(),
                    Optional.ofNullable(entry.getValue()).orElse("")));
            somethingChanged = true;
        }

        for (String key : removals) {
            argList.add("--remove=" + key);
            somethingChanged = true;
        }

        argList.add("2>&1");
        argList.add(">");
        argList.add("/dev/null");
        String[] returnStringArray = argList.toArray(new String[0]);
        if (logger.isDebugEnabled()) {
            logger.debug("getCommandArguments(String, Iterator) - end - return value={}", (Object) returnStringArray);
        }
        return (somethingChanged ? returnStringArray : null);
    }

    /**
     * Get the formatted String array of command line arguments to execute
     * when we call out to the system utility to store the config.
     * @return String[] array of arguments.
     */
    public String[] getCommandArguments() {
        Map<String, String> optionMap = new HashMap<>();
        List<String> removals = new LinkedList<>();

        for (String key : getKeysToBeUpdated()) {
            if (Config.get().containsKey(key)) {
                optionMap.put(key, Config.get().getString(key));
            }
            else {
                removals.add(key);
            }
        }
        return getCommandArguments(Config.getDefaultConfigFilePath(),
                optionMap, removals);
    }

    /**
     * Store the Configuration to the filesystem
     * @return ValidatorError
     */
    @Override
    public ValidatorError[] storeConfiguration() {
        if (logger.isDebugEnabled()) {
            logger.debug("storeConfiguration() - start");
        }

        if (keysToBeUpdated.isEmpty()) {
            return null;
        }

        if (keysToBeUpdated.contains(ConfigDefaults.MOUNT_POINT)) {
            this.updateString(ConfigDefaults.KICKSTART_MOUNT_POINT,
                    Config.get().getString(ConfigDefaults.MOUNT_POINT));
        }

        if (keysToBeUpdated.contains(ConfigDefaults.DISCONNECTED) &&
                !Config.get().getBoolean(ConfigDefaults.DISCONNECTED)) {
            //if there isn't already a value set for the satellite parent (We don't want to
            // overwrite a custom value.)
            if (Config.get().getString(ConfigDefaults.SATELLITE_PARENT) == null ||
                    Config.get().getString(ConfigDefaults.SATELLITE_PARENT).isEmpty()) {
                this.updateString(ConfigDefaults.SATELLITE_PARENT,
                        ConfigDefaults.DEFAULT_SAT_PARENT);
            }
        }

        ValidatorError[] retVal = executeStore();
        if (retVal != null) {
            return retVal;
        }

        this.keysToBeUpdated.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("storeConfiguration() - end - return value=null");
        }
        return null;

    }

    protected ValidatorError[] executeStore() {
        String[] commandArguments = getCommandArguments();
        if (commandArguments != null) {
            Executor e = getExecutor();
            int exitcode = e.execute(commandArguments);
            if (exitcode != 0) {
                ValidatorError[] retval = new ValidatorError[1];
                retval[0] = new ValidatorError("config.storeconfig.error",
                        Integer.toString(exitcode));

                if (logger.isDebugEnabled()) {
                    logger.debug("storeConfiguration() - end - return value={}", (Object)retval);
                }
                return retval;
            }
        }
        return null;
    }

    /**
     * Update a configuration value for this satellite.  This tracks
     * the values that are actually changed.
     * @param configKey key to the Config value you want to set
     * @param newValue you want to set
     */
    public void updateBoolean(String configKey, Boolean newValue) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateBoolean(String configKey={}, Boolean newValue={}) - start", configKey, newValue);
        }

        if (newValue == null) {
            // If its true we are changing the value
            if (Config.get().getBoolean(configKey)) {
                Config.get().setBoolean(configKey, Boolean.FALSE.toString());
                keysToBeUpdated.add(configKey);
            }
        }
        else {
            if (Config.get().getBoolean(configKey) != newValue) {
                Config.get().setBoolean(configKey, newValue.toString());
                keysToBeUpdated.add(configKey);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateBoolean(String, Boolean) - end");
        }
    }

    /**
     * Update a String config
     * @param configKey to the value
     * @param newValue to set
     */
    public void updateString(String configKey, String newValue) {
        String newValueToLog = newValue;
        if (configKey.equals(ConfigDefaults.HTTP_PROXY_PASSWORD)) {
            newValueToLog = "*****";
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateString(String configKey={}, String newValue={}) - start",
                    configKey, newValueToLog);
        }

        if (Config.get().getString(configKey) == null ||
                !Config.get().getString(configKey).equals(newValue)) {
            keysToBeUpdated.add(configKey);
            Config.get().setString(configKey, newValue);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateString(String, String) - end");
        }
    }

    /**
     * Remove a configuration entry
     * @param configKey to the value
     */
    public void remove(String configKey) {
        if (logger.isDebugEnabled()) {
            logger.debug("remove(String configKey={}) - start", configKey);
        }

        if (Config.get().getString(configKey) != null) {
            keysToBeUpdated.add(configKey);
            Config.get().remove(configKey);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("remove(String) - end");
        }
    }

    /**
     * Get the list of configuration values that need to be written out
     * to the persistence mechanism.
     * @return List of String values of to the keys to be written.
     */
    public List<String> getKeysToBeUpdated() {
        return keysToBeUpdated;
    }

    /**
     * Clear the set of configuration changes from the Command.
     */
    public void clearUpdates() {
        if (logger.isDebugEnabled()) {
            logger.debug("clearUpdates() - start");
        }

        this.keysToBeUpdated.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("clearUpdates() - end");
        }
    }

}
