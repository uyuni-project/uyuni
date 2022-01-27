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

package com.redhat.rhn.manager.contentmgmt;

import static com.suse.utils.Opt.stream;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulePackagesResponse;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves dependencies in a content management project
 *
 * At the moment, dependency resolution only works with module filters and modular dependencies. This class can be
 * enhanced to resolve package dependencies as well.
 *
 * The resolution process takes the complete list of filters as input and queries the libmodulemd API with all the
 * module streams in filters. A module filter represents a selected module in one of the modular sources (filter rules
 * have no effect for module filters). The API returns all the related packages that must be allowed/denied as per
 * current module selection. In return, all module filters are translated into a collection of package filters by the
 * following rules:
 *
 * 1. All modular packages are denied (deny by nevra)
 * 2. For each package publicly provided by a module, all the packages from other sources with the same package name
 *    are denied. As a result, a specific package is only provided exclusively by the module to prevent any conflicts
 *    (deny by name).
 * 3. Modular packages from selected modules are overridden to be allowed (allow by nevra)
 *
 * This algorithm only runs if there are any module filters in the input list. Otherwise the process is bypassed and no
 * filter transformation is done.
 *
 * The resolve deny-allow override problem:
 *
 * One problem with this approach is that the allow filters will override any further deny filters defined by the user
 * on those packages. We need to figure out if this is an important case and if so, come up with a different solution.
 *
 * @see com.redhat.rhn.manager.contentmgmt.test.DependencyResolverTest#testModuleFiltersForeignPackagesSelected
 * @see com.redhat.rhn.manager.contentmgmt.test.DependencyResolverTest#testModuleFiltersForeignPackagesConflicting
 *
 */
public class DependencyResolver {

    private ContentProject project;
    private ModulemdApi modulemdApi;

    /**
     * Initialize a new instance with a content project and a {@link ModulemdApi} instance
     *
     * @param projectIn the content project
     * @param modulemdApiIn the libmodulemd API instance
     */
    public DependencyResolver(ContentProject projectIn, ModulemdApi modulemdApiIn) {
        this.project = projectIn;
        this.modulemdApi = Objects.requireNonNullElseGet(modulemdApiIn, ModulemdApi::new);
    }

    /**
     * Enhances the list of filters with dependency filters by looking up the dependencies in the sources
     *
     * @param filters the complete list of filters to be included in the project
     * @return an instance of {@link DependencyResolutionResult} with the resolved filters and selected modules
     * including dependencies
     * @throws DependencyResolutionException if dependency resolution fails for some reason
     */
    public DependencyResolutionResult resolveFilters(List<ContentFilter> filters) throws DependencyResolutionException {

        List<ModuleFilter> moduleFilters = filters.stream()
                .flatMap(f -> stream((Optional<ModuleFilter>) f.asModuleFilter()))
                .collect(toList());

        List<ContentFilter> updatedFilters = new ArrayList<>(filters);

        // Transform module filters to package filters
        // If no module filters are attached, no modular package should be filtered out
        DependencyResolutionResult resolved = null;
        if (moduleFilters.size() > 0) {
            resolved = resolveModularDependencies(moduleFilters);
            updatedFilters.addAll(resolved.getFilters());
            updatedFilters.removeAll(moduleFilters);
        }
        // Any other dependency filters (e.g. package dependencies) can be appended here

        return new DependencyResolutionResult(updatedFilters, resolved != null ?
                resolved.getModules() : Collections.emptyList());
    }

    /**
     * Resolves modular dependencies and converts all module filters to package filters
     *
     * @param filters the list of module filters to be included in the project
     * @return an instance of {@link DependencyResolutionResult} with the resolved filters and selected modules
     * including dependencies
     */
    private DependencyResolutionResult resolveModularDependencies(List<ModuleFilter> filters)
            throws DependencyResolutionException {
        List<Channel> sources = this.getActiveSources();
        List<Module> modules = filters.stream().map(ModuleFilter::getModule).collect(toList());
        ModulePackagesResponse modPkgList;
        try {
            modPkgList = modulemdApi.getPackagesForModules(sources, modules);
        }
        catch (ModulemdApiException e) {
            throw new DependencyResolutionException("Failed to resolve modular dependencies.", e);
        }

        List<Module> resolvedModules = modPkgList.getSelected().stream().map(Module::new).collect(Collectors.toList());

        // 1. Modular packages to be denied
        Stream<PackageFilter> pkgDenyFilters;
        try {
            pkgDenyFilters = modulemdApi.getAllPackages(sources).stream()
                    .map(nevra -> initFilterFromPackageNevra(nevra, ContentFilter.Rule.DENY));
        }
        catch (ModulemdApiException e) {
            throw new DependencyResolutionException("Failed to resolve modular dependencies.", e);
        }

        // 2. Non-modular packages to be denied by name
        Stream<PackageFilter> providedRpmApiFilters = modPkgList.getRpmApis().stream()
                .map(DependencyResolver::initFilterFromPackageName);

        // 3. Modular packages to be allowed
        Stream<PackageFilter> pkgAllowFilters = modPkgList.getRpmPackages().stream()
                .map(nevra -> initFilterFromPackageNevra(nevra, ContentFilter.Rule.ALLOW));

        // Concatenate filter streams into the list
        return new DependencyResolutionResult(Stream.of(pkgDenyFilters, providedRpmApiFilters, pkgAllowFilters)
                .flatMap(s -> s).collect(toList()), resolvedModules);
    }

    private static PackageFilter initFilterFromPackageNevra(String nevra, ContentFilter.Rule rule) {
        return initFilter(FilterCriteria.Matcher.EQUALS, rule, "nevra", nevra);
    }

    private static PackageFilter initFilterFromPackageName(String name) {
        return initFilter(FilterCriteria.Matcher.MATCHES, ContentFilter.Rule.DENY, "name", name);
    }

    private static PackageFilter initFilter(FilterCriteria.Matcher matcher, ContentFilter.Rule rule, String field,
            String value) {
        FilterCriteria criteria = new FilterCriteria(matcher, field, value);
        PackageFilter filter = new PackageFilter();
        filter.setRule(rule);
        filter.setCriteria(criteria);
        return filter;
    }

    /**
     * Get a list of modular channels among the sources
     *
     * @return the list of modular channels
     */
    private List<Channel> getActiveSources() {
        return project.getActiveSources().stream()
                .map(ProjectSource::asSoftwareSource)
                .filter(Optional::isPresent)
                .map(s -> s.get().getChannel())
                .filter(Channel::isModular)
                .collect(toList());
    }

}
