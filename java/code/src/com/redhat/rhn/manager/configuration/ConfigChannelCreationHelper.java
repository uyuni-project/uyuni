/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.configuration;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.user.User;

import com.redhat.rhn.manager.configuration.file.ConfigFileData;
import com.redhat.rhn.manager.configuration.file.SLSFileData;
import org.apache.struts.action.DynaActionForm;

import java.io.IOException;



/**
 * ConfigChannelValidator
 * @version $Rev$
 */
public class ConfigChannelCreationHelper {
    private static final String LABEL = "cofLabel";
    private static final String NAME = "cofName";
    private static final String DESCRIPTION = "cofDescription";
    private static final String ERROR_REQUIRED = "errors.required";
    private static final String ERROR_MAXSIZE = "errors.maxsize";
    private static final String ERROR_CONFIG_CHANNEL_LABEL = "errors.config_channel_label";

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_LABEL_LENGTH = 64;
    /**
     * Basically validates the passed in data and returns
     * a validator result for bad data..
     * @param label  the UNIQUE label of the config channel
     * @param name   the name of the config channel
     * @param description the description of the config channel
     * @throws ValidatorException on bad data
     */
    public void validate(String label,
                                String name,
                                String description) {
        validate(label, name, description, null);
    }

    /**
     * Basically validates the passed in form and returns
     * a validator result for bad data..
     * @param form  the DynaForm that may be used
     *              for validating through the UI
     * @throws ValidatorException on bad data
     */
    public void validate(DynaActionForm form) {
        validate(form.getString(LABEL),
                                form.getString(NAME),
                                form.getString(DESCRIPTION), form);
    }

    /**
     * Basically validates the passed in data and returns
     * a validator result for bad data..
     * @param label  the UNIQUE label of the config channel
     * @param name   the name of the config channel
     * @param description the description of the config channel
     * @param form  the DynaForm that may be used
     *              for validating through the UI
     * @throws ValidatorException on bad data
     */
    private void validate(String label,
                        String name,
                        String description,
                        DynaActionForm form) {
        LocalizationService ls = LocalizationService.getInstance();
        ValidatorResult result = new ValidatorResult();

        // Check label
        if (label == null || label.trim().length() == 0) {
            result.addError(new ValidatorError(ERROR_REQUIRED,
                                            ls.getMessage(LABEL)));
        }
        else if (label.trim().length() > MAX_LABEL_LENGTH) {
            result.addError(new ValidatorError(ERROR_MAXSIZE,
                                            ls.getMessage(LABEL),
                                            MAX_LABEL_LENGTH));
        }
        else if (label.startsWith("-") || !label.matches("^[a-zA-Z0-9\\-_]+$")) {
            result.addError(new ValidatorError(ERROR_CONFIG_CHANNEL_LABEL,
                                                ls.getMessage(LABEL)));
        }
        else if (form != null) {
            form.set(LABEL, label);
        }
        // Check name
        if (name == null || name.trim().length() == 0) {
            result.addError(new ValidatorError(ERROR_REQUIRED,
                                            ls.getMessage(NAME)));
        }
        else if (name.trim().length() > MAX_NAME_LENGTH) {
            result.addError(new ValidatorError(ERROR_MAXSIZE,
                                                ls.getMessage(NAME),
                                                MAX_NAME_LENGTH));
        }
        else if (form != null) {
            form.set(NAME, name);
        }
        // Check description
        if (description == null || description.trim().length() == 0) {
            result.addError(new ValidatorError(ERROR_REQUIRED,
                                                ls.getMessage(DESCRIPTION)));
        }
        else if (form != null) {
            form.set(DESCRIPTION, description);
        }
        if (!result.isEmpty()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * Get the channel type object based on it's label
     * @param channelType channel type label
     * @return ConfigChannelType ConfigChannelType
     */
    public ConfigChannelType getGlobalChannelType(String channelType) {
        ValidatorResult result = new ValidatorResult();
        ConfigChannelType ct = ConfigChannelType.lookup(channelType);
        if (!(ct.getLabel().equals(ConfigChannelType.NORMAL) || ct.getLabel().equals(ConfigChannelType.STATE))) {
            String validValues = String.join(",", ConfigChannelType.NORMAL, ConfigChannelType.STATE);
            result.addError(new ValidatorError("errors.invalid.value",
                    "'Configuration Channel Type'",  validValues));
        }
        if (!result.isEmpty()) {
            throw new ValidatorException(result);
        }
        return ct;
    }

    /**
     * Creates a new config channel
     * @param user needed for authentication.
     * @return the created channel
     */
    public ConfigChannel create(User user) {
        ConfigChannelType t = ConfigChannelType.normal();
        ConfigChannel cc = ConfigurationFactory.newConfigChannel();
        cc.setOrg(user.getOrg());
        cc.setConfigChannelType(t);
        return cc;
    }

    /**
     * Creates a new config channel of specific type
     * @param user needed for authentication.
     * @param type type of the channel.
     * @return the created channel
     */
    public ConfigChannel create(User user, ConfigChannelType type) {
        ConfigChannel cc = ConfigurationFactory.newConfigChannel();
        cc.setOrg(user.getOrg());
        cc.setConfigChannelType(type);
        return cc;
    }
    /**
     * populates the config channel readinng the fields from a
     * dyna action form
     * @param cc the channel to populate
     * @param form the form to retrieve the data from.
     * @throws ValidatorException  on bad data
     */
    public void update(ConfigChannel cc, DynaActionForm form) {
        update(cc, form.getString(NAME),
                    form.getString(LABEL), form.getString(DESCRIPTION));
    }

    /**
     * populates the config channel readinng the fields from a
     * dyna action form
     * @param cc the channel to populate
     * @param name name of the channel
     * @param label channel label
     * @param description channel description
     * @throws ValidatorException  on bad data
     */
    public void update(ConfigChannel cc, String name,
                            String label, String description) {
        ConfigurationManager cm = ConfigurationManager.getInstance();
        if (!label.equals(cc.getLabel()) &&
                cm.conflictingChannelExists(label, cc.getConfigChannelType(), cc.getOrg())) {
            ValidatorException.raiseException("channelOverview.error.labelexists",
                    label);
        }
        cc.setLabel(label);
        cc.setName(name);
        cc.setDescription(description);
    }

    /**
     * Helper method create the init.sls file for state channel
     * @param user needed for authorization
     * @param channel config channel
     * @param contents contents for init.sls file
     */
    public void createInitSlsFile(User user, ConfigChannel channel, String contents) {
        if (channel.isStateChannel()) {
            ConfigFileData data = new SLSFileData(contents);
            try {
                ConfigFileBuilder.getInstance().create(data, user, channel);
            }
            catch (IOException e) {
                String msg = "Error creating init.sls file .\n" +  e.getMessage();
                throw new FaultException(1021, "ConfigChannelCreationException", msg);
            }
        }

    }
}
