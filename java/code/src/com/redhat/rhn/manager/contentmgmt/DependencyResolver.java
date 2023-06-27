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

import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.ModularPackageFilter;
import com.redhat.rhn.domain.contentmgmt.ModuleFilter;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModularityDisabledException;
import com.redhat.rhn.domain.contentmgmt.modulemd.Module;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulePackagesResponse;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApi;
import com.redhat.rhn.domain.contentmgmt.modulemd.ModulemdApiException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves dependencies in a content management project
 * <p>
 * At the moment, dependency resolution only works with module filters and modular dependencies. This class can be
 * enhanced to resolve package dependencies as well.
 * <p>
 * The resolution process takes the complete list of filters as input and queries the libmodulemd API with all the
 * module streams in filters. A module filter represents a selected module in one of the modular sources (filter rules
 * have no effect for module filters). The API returns all the related packages that must be allowed/denied as per
 * current module selection. In return, all module filters are translated into a collection of package filters by the
 * following rules:
 * <p>
 * <ol>
 * <li>All modular packages are denied (deny by nevra)
 * <li>For each package publicly provided by a module, all the packages from other sources with the same package name
 *    are denied. As a result, a specific package is provided exclusively by the module to prevent any conflicts (deny
 *    by name).
 * <li>Modular packages from selected modules are overridden to be allowed (allow by nevra)
 * </ol>
 * <p>
 * This algorithm only runs if there are any module filters in the input list. Otherwise, the process is bypassed and no
 * filter transformation is done.
 *
 * <h3>The resolve deny-allow override problem</h3>
 * One problem with this approach is that the ALLOW filters will override any further deny filters defined by the user
 * on those packages. We need to figure out if this is an important case and if so, come up with a different solution.
 *
 * @see com.redhat.rhn.manager.contentmgmt.test.DependencyResolverTest#testModuleFiltersForeignPackagesSelected
 * @see com.redhat.rhn.manager.contentmgmt.test.DependencyResolverTest#testModuleFiltersForeignPackagesConflicting
 *
 */
public class DependencyResolver {

    private final ContentProject project;
    private final ModulemdApi modulemdApi;

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
                .filter(f -> f instanceof ModuleFilter)
                .map(ModuleFilter.class::cast)
                .filter(f -> FilterCriteria.Matcher.EQUALS.equals(f.getCriteria().getMatcher()))
                .collect(toList());

        if (isModulesDisabled(filters) && !moduleFilters.isEmpty()) {
            throw new DependencyResolutionException("Modularity is disabled.", new ModularityDisabledException());
        }

        List<ContentFilter> updatedFilters = new ArrayList<>(filters);

        // Transform module filters to package filters
        DependencyResolutionResult resolved = null;
        if (isModulesDisabled(filters)) {
            // If modularity is disabled, no modules should be included
            updatedFilters.add(new ModularPackageFilter());
        }
        else if (!moduleFilters.isEmpty()) {
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
        PackageFilter pkgDenyFilter = new ModularPackageFilter();

        // 2. Non-modular packages to be denied by name
        Stream<PackageFilter> providedRpmApiFilters = modPkgList.getRpmApis().stream()
                .map(DependencyResolver::initFilterFromPackageName);

        // 3. Modular packages to be allowed
        Stream<PackageFilter> pkgAllowFilters = modPkgList.getRpmPackages().stream()
                .map(nevra -> initFilterFromPackageNevra(nevra, ContentFilter.Rule.ALLOW));

        // Concatenate filter streams into the list
        return new DependencyResolutionResult(Stream.of(providedRpmApiFilters, pkgAllowFilters,
                Stream.of(pkgDenyFilter)).flatMap(s -> s).collect(toList()), resolvedModules);
    }

    /**
     * Returns true if modularity is disabled via the 'Module(Stream): None' filter
     * @param filters the list of enabled filters
     * @return true if modularity is disabled
     */
    public static boolean isModulesDisabled(Collection<ContentFilter> filters) {
        return filters.stream()
                .filter(f -> f instanceof ModuleFilter)
                .anyMatch(f -> FilterCriteria.Matcher.MODULE_NONE.equals(f.getCriteria().getMatcher()));
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
