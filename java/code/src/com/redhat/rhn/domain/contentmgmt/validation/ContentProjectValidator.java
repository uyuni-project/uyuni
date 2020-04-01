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

package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.domain.contentmgmt.ContentProject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates a content project instance using a specified list of validators
 */
public class ContentProjectValidator {

    private ContentProject project;
    private List<ContentValidator> validators;

    /**
     * Initialize a new content project validator with a list of validators
     *
     * @param projectIn the content project to be validated
     * @param validatorsIn the validators to be used to validate the project
     */
    public ContentProjectValidator(ContentProject projectIn, List<ContentValidator> validatorsIn) {
        this.project = projectIn;
        this.validators = validatorsIn;
    }

    /**
     * Validate a content project
     *
     * @return a list of validation messages
     */
    public List<ContentValidationMessage> validate() {
        return validators.stream()
                .flatMap(v -> v.validate(project).stream())
                .collect(Collectors.toList());
    }
}
