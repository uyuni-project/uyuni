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

import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_ERROR;
import static com.redhat.rhn.domain.contentmgmt.validation.ContentValidationMessage.TYPE_INFO;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ConflictingStreamsException;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModuleNotFoundException;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionException;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionResult;
import com.redhat.rhn.manager.contentmgmt.DependencyResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates resolution of modular dependencies in a content project
 */
public class ModularDependencyValidator implements ContentValidator {

    private ModulemdApi modulemdApi;
    private LocalizationService loc = LocalizationService.getInstance();

    /**
     * Initialize a modular dependency validator with {@link ModulemdApi} as the default modulemd API
     */
    public ModularDependencyValidator() {
        this.modulemdApi = new ModulemdApi();
    }

    /**
     * Initialzie a modular dependency validator with a modulemd API instance
     *
     * @param modulemdApiIn the modulemd API instance
     */
    public ModularDependencyValidator(ModulemdApi modulemdApiIn) {
        this.modulemdApi = modulemdApiIn;
    }

    @Override
    public List<ContentValidationMessage> validate(ContentProject project) {
        boolean hasModularSources = project.getActiveSources().stream()
                .flatMap(s -> s.asSoftwareSource().stream())
                .map(SoftwareProjectSource::getChannel)
                .anyMatch(Channel::isModular);

        boolean hasModuleFilters = project.getActiveFilters().stream()
                .anyMatch(f -> f.asModuleFilter().isPresent());

        if (!(hasModularSources && hasModuleFilters)) {
            return Collections.emptyList();
        }

        DependencyResolver resolver = new DependencyResolver(project, modulemdApi);

        List<ContentValidationMessage> messages = new ArrayList<>();
        try {
            DependencyResolutionResult result = resolver.resolveFilters(project.getActiveFilters());

            // Add a message with the list of resolved modules
            String selectedModules =
                    result.getModules().stream().map(Module::getFullName).distinct().collect(Collectors.joining(", "));
            messages.add(ContentValidationMessage.contentFiltersMessage(
                    loc.getMessage("contentmanagement.validation.selectedmodules", selectedModules), TYPE_INFO));
        }
        catch (DependencyResolutionException e) {
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
