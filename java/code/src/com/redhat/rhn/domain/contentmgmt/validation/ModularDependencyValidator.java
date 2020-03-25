package com.redhat.rhn.domain.contentmgmt.validation;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ConflictingStreamsException;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleNotFoundException;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionException;
import com.redhat.rhn.manager.contentmgmt.DependencyResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_ERROR;

/**
 * Validates resolution of modular dependencies in a content project
 */
public class ModularDependencyValidator implements ContentValidator {

    private LocalizationService loc = LocalizationService.getInstance();

    @Override
    public List<ContentValidationMessage> validate(ContentProject project) {
        boolean hasModularSources = project.getActiveSources().stream()
                .map(ProjectSource::asSoftwareSource)
                .anyMatch(s -> s.isPresent() && s.get().getChannel().isModular());

        boolean hasModuleFilters = project.getActiveFilters().stream()
                .anyMatch(f -> f.asModuleFilter().isPresent());

        if (!(hasModularSources && hasModuleFilters)) {
            return Collections.emptyList();
        }

        DependencyResolver resolver = new DependencyResolver(project, new ModulemdApi());

        List<ContentValidationMessage> messages = new ArrayList<>();
        try {
            resolver.resolveFilters(project.getActiveFilters());
        } catch (DependencyResolutionException e) {
            if (e.getCause() instanceof ModuleNotFoundException) {
                List<Module> modules = ((ModuleNotFoundException) e.getCause()).getModules();
                messages = modules.stream()
                        .map(m -> ContentValidationMessage.contentFiltersMessage(
                                loc.getMessage("contentmanagement.validation.modulenotfound", m.getFullName()),
                                TYPE_ERROR))
                        .collect(Collectors.toList());
            }
            else if (e.getCause() instanceof ConflictingStreamsException) {
                ConflictingStreamsException cause = (ConflictingStreamsException) e.getCause();
                Module module = cause.getModule();
                Module other = cause.getOther();
                messages.add(ContentValidationMessage.contentFiltersMessage(
                        loc.getMessage("contentmanagement.validation.moduleconflict",
                                module.getFullName(), other.getFullName()), TYPE_ERROR));
            }
            else if (e.getModule().isPresent()) {
                messages.add(ContentValidationMessage.contentFiltersMessage(
                        loc.getMessage("contentmanagement.validation.dependencyerror.formodule",
                                e.getModule().get().getFullName()), TYPE_ERROR));
            }
            else {
                messages.add(ContentValidationMessage.contentFiltersMessage(
                        loc.getMessage("contentmanagement.validation.dependencyerror"), TYPE_ERROR));
            }
        }

        return messages;
    }
}
