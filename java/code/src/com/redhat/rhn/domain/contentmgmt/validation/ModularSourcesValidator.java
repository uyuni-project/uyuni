package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;

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
                .map(ProjectSource::asSoftwareSource)
                .anyMatch(s -> s.isPresent() && s.get().getChannel().isModular());

        boolean hasModuleFilters = project.getActiveFilters().stream()
                .anyMatch(f -> f.asModuleFilter().isPresent());

        ContentValidationMessage msg = null;
        if (hasModularSources && !hasModuleFilters) {
            msg = ContentValidationMessage
                    .info(loc.getMessage("contentmanagement.validation.nomodulefilters"));
        }
        else if (hasModuleFilters && !hasModularSources) {
            msg = ContentValidationMessage.warn(loc.getMessage("contentmanagement.validation.nomodularsources"));
        }

        return msg != null ? Collections.singletonList(msg) : Collections.emptyList();
    }
}
