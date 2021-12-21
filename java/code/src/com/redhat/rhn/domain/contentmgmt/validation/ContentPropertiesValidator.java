/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.CreateChannelCommand;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Validation for Content Lifecycle Management object properties
 */
public class ContentPropertiesValidator {

    /**
     * Standard constructor
     */
    private ContentPropertiesValidator() { }


    /**
     * Validate ContentProject properties
     * @param label the label
     * @param name the name
     * @param user the user
     * @throws ValidatorException when the parameters do not pass the validation
     */
    public static void validateProjectProperties(String label, String name, User user) throws ValidatorException {
        ValidatorResult result = new ValidatorResult();

        if (StringUtils.isEmpty(label)) {
            result.addFieldError("label", "contentmanagement.label_required");
        }

        if (!isLabelValid(label)) {
            result.addFieldError("label", "contentmanagement.label_invalid");
        }

        if (label.length() > 24) {
            result.addFieldError("label", "contentmanagement.project_label_too_long");
        }

        if (StringUtils.isEmpty(name)) {
            result.addFieldError("name", "contentmanagement.name_required");
        }

        if (name.length() > 128) {
            result.addFieldError("name", "contentmanagement.project_name_too_long");
        }

        ContentManager.lookupProjectByNameAndOrg(name, user).ifPresent(cp -> {
            if (!cp.getLabel().equals(label)) {
                result.addFieldError("name", "contentmanagement.name_already_exists");
            }
        });

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * Validate ContentEnvironment properties
     *
     * @param name the name
     * @param label the label
     * @throws ValidatorException when the parameters do not pass the validation
     */
    public static void validateEnvironmentProperties(String name, String label) {
        ValidatorResult result = new ValidatorResult();

        if (StringUtils.isEmpty(label)) {
            result.addFieldError("label", "contentmanagement.label_required");
        }

        if (StringUtils.isEmpty(name)) {
            result.addFieldError("name", "contentmanagement.name_required");
        }

        if (!isLabelValid(label)) {
            result.addFieldError("label", "contentmanagement.label_invalid");
        }

        if (label.length() > 16) {
            result.addFieldError("label", "contentmanagement.environment_lbl_too_long");
        }

        if (name.length() > 128) {
            result.addFieldError("name", "contentmanagement.environment_name_too_long");
        }

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * Validate ContentFilter properties
     *
     * @param name the name
     * @throws ValidatorException when the parameters do not pass the validation
     */
    public static void validateFilterProperties(String name) {
        ValidatorResult result = new ValidatorResult();

        if (StringUtils.isEmpty(name)) {
            result.addFieldError("filter_name", "contentmanagement.name_required");
        }

        if (name.length() > 128) {
            result.addFieldError("filter_name", "contentmanagement.filter_name_too_long");
        }

        if (result.hasErrors()) {
            throw new ValidatorException(result);
        }
    }

    /**
     * validate label pattern
     * @param label label to validate
     * @return true if label is valid
     */
    public static Boolean isLabelValid(String label) {
        return Pattern.compile(CreateChannelCommand.CHANNEL_LABEL_REGEX).matcher(label).find() &&
                Pattern.compile(CreateChannelCommand.CHANNEL_NAME_REGEX).matcher(label).find();
    }
}
