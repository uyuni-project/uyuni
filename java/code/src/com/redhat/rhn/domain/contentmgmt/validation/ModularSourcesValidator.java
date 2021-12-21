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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;

import java.util.Collections;
import java.util.List;

/**
 * Validates the existence of modular sources and modular filters in a content project
 */
public class ModularSourcesValidator implements ContentValidator {

    private LocalizationService loc = LocalizationService.getInstance();

    @Override
    public List<ContentValidationMessage> validate(ContentProject project) {
        boolean hasModularSources = project.getActiveSources().stream()
                .flatMap(s -> s.asSoftwareSource().stream())
                .map(SoftwareProjectSource::getChannel)
                .anyMatch(Channel::isModular);

        boolean hasModuleFilters = project.getActiveFilters().stream()
                .anyMatch(f -> f.asModuleFilter().isPresent());

        ContentValidationMessage msg = null;
        if (hasModularSources && !hasModuleFilters) {
            msg = ContentValidationMessage
                    .softwareSourcesMessage(loc.getMessage("contentmanagement.validation.nomodulefilters"),
                            ContentValidationMessage.TYPE_INFO);
        }
        else if (hasModuleFilters && !hasModularSources) {
            msg = ContentValidationMessage
                    .softwareSourcesMessage(loc.getMessage("contentmanagement.validation.nomodularsources"),
                            ContentValidationMessage.TYPE_WARN);
        }

        return msg != null ? Collections.singletonList(msg) : Collections.emptyList();
    }
}
