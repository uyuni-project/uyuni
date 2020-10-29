/**
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

package com.redhat.rhn.domain.contentmgmt.validation.strict;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.CreateChannelCommand;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 *
 */
public class ContentStrictValidator {

    /**
     * Standard constructor
     */
    private ContentStrictValidator() { }


    // todo internationalize everything
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
            result.addError("Label is required");
        }

        if (!isLabelValid(label)) {
            result.addError(
                    "Label must begin with a letter and must contain only lowercase letters, hyphens ('-')," +
                            " periods ('.'), underscores ('_'), and numerals."
            );
        }

        if (label.length() > 24) {
            result.addError("Label must not exceed 24 characters");
        }

        if (StringUtils.isEmpty(name)) {
            result.addError("Name is required");
        }

        if (name.length() > 128) {
            result.addError("Name must not exceed 128 characters");
        }

        ContentManager.lookupProjectByNameAndOrg(name, user).ifPresent(cp -> {
            if (!cp.getLabel().equals(label)) {
                result.addError("Name already exists");
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

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(label)) {
            result.addError("Name and label are required");
        }

        if (!isLabelValid(label)) {
            result.addError(
                    "Label must begin with a letter and must contain only lowercase letters, hyphens ('-')," +
                            " periods ('.'), underscores ('_'), and numerals."
            );
        }

        if (label.length() > 16) {
            result.addError("Label must not exceed 16 characters");
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
